package com.landmaster.landsutilities.block.entity;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.menu.AutoAnvilMenu;
import com.landmaster.landsutilities.util.Util;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class AutoAnvilBlockEntity extends BaseBlockEntity implements MenuProvider {
    protected final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(
            UUID.fromString("d34c873d-ad34-448e-9279-bfedb6037191"), "landsutilities_auto_anvil"
    );

    private @Nullable List<ItemStack> loadedItems;

    @Getter(lazy = true)
    private final AnvilMenu resultGenerator = computeResultGenerator();
    @Getter(lazy = true)
    private final IItemHandlerModifiable inputItems = computeInputItems();
    @Getter
    private final IItemHandlerModifiable resultHandler = new ItemStackHandler(1);
    @Getter(lazy = true)
    private final IItemHandlerModifiable guiItemHandler = computeItemHandler(false);
    @Getter(lazy = true)
    private final IItemHandlerModifiable automationItemHandler = computeItemHandler(true);

    public static final Map<String, Optional<Direction>> INITIAL_IO_CONFIG = Map.of(
            "external_tank", Optional.of(Direction.UP)
    );

    public AutoAnvilBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandsUtilities.AUTO_ANVIL_TE.get(), pos, blockState, INITIAL_IO_CONFIG);
    }

    protected IItemHandlerModifiable computeInputItems() {
        return level != null && level.isClientSide ? new ItemStackHandler(2) : new InvWrapper(resultGenerator().inputSlots);
    }

    protected AnvilMenu computeResultGenerator() {
        var result = new AnvilMenu(0, new Inventory(FakePlayerFactory.get((ServerLevel) level, FAKE_PLAYER_PROFILE))) {
            @Override
            public void createResult() {
                itemName = inputItems().getStackInSlot(0).getHoverName().getString();
                super.createResult();
            }
        };
        result.suppressRemoteUpdates();
        return result;
    }

    protected Optional<IFluidHandler> externalTankHandler() {
        return getConfiguration("external_tank").map(dir ->
                level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(dir), dir.getOpposite()));
    }

    private IItemHandlerModifiable computeItemHandler(boolean automation) {
        var result = new CombinedInvWrapper(inputItems(), resultHandler) {
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                if (automation && slot < inputItems().getSlots()) {
                    return ItemStack.EMPTY;
                }
                return super.extractItem(slot, amount, simulate);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return slot < inputItems().getSlots() && super.isItemValid(slot, stack);
            }
        };
        if (loadedItems != null) {
            for (int i=0; i<Math.min(loadedItems.size(), 3); ++i) {
                result.setStackInSlot(i, loadedItems.get(i));
            }
            loadedItems = null;
        }
        return result;
    }

    protected boolean tryExtractLiquidXP(int amount, boolean simulate) {
        return externalTankHandler().map(cap -> {
            for (var fluidHolder: BuiltInRegistries.FLUID.getOrCreateTag(Util.XP)) {
                var desiredStack = new FluidStack(fluidHolder.value(), amount);
                var drained = cap.drain(desiredStack, IFluidHandler.FluidAction.SIMULATE);
                if (drained.getAmount() >= amount) {
                    if (!simulate) {
                        cap.drain(desiredStack, IFluidHandler.FluidAction.EXECUTE);
                    }
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    private static final Method ON_TAKE;

    static {
        try {
            ON_TAKE = AnvilMenu.class.getDeclaredMethod("onTake", Player.class, ItemStack.class);
            ON_TAKE.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (active) {
            var resultGenerator = resultGenerator();
            var resultHandler = resultHandler();
            int requiredXP = Math.clamp(Util.levelToFluidXp(resultGenerator.getCost()), 0, Integer.MAX_VALUE);
            var resultItem = resultGenerator.resultSlots.getItem(0);
            if (!resultItem.isEmpty()
                    && resultHandler.insertItem(0, resultItem, true).isEmpty()
                    && tryExtractLiquidXP(requiredXP, true)) {
                resultHandler.insertItem(0, resultItem, false);
                tryExtractLiquidXP(requiredXP, false);
                resultGenerator.resultSlots.setItem(0, ItemStack.EMPTY);
                try {
                    ON_TAKE.invoke(resultGenerator, resultGenerator.player, resultItem);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private final Codec<List<ItemStack>> INVENTORY_CODEC = ItemStack.OPTIONAL_CODEC.listOf(3, 3);

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadedItems = INVENTORY_CODEC.parse(registryOps(registries), tag.get("items")).getOrThrow();
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        var items = new ItemStack[automationItemHandler().getSlots()];
        for (int i=0; i<items.length; ++i) {
            items[i] = automationItemHandler().getStackInSlot(i);
        }
        tag.put("items", INVENTORY_CODEC.encodeStart(registryOps(registries), Arrays.asList(items)).getOrThrow());
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return LandsUtilities.AUTO_ANVIL.get().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new AutoAnvilMenu(i, inventory, this);
    }
}
