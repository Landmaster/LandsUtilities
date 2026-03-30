package com.landmaster.landsutilities.block.entity;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.menu.AutoAnvilMenu;
import com.landmaster.landsutilities.util.SyncInfo;
import com.landmaster.landsutilities.util.Util;
import com.mojang.authlib.GameProfile;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerEquipment;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.transfer.CombinedResourceHandler;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
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
    private final ResourceHandler<ItemResource> inputItems = computeInputItems();
    @Getter
    private final ItemStacksResourceHandler resultHandler = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
        }
    };
    @Getter(lazy = true)
    private final ResourceHandler<ItemResource> automationItemHandler = computeItemHandler();

    public AutoAnvilBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandsUtilities.AUTO_ANVIL_TE.get(), pos, blockState,
                new SyncInfo<>("external_tank", Direction.CODEC, Direction.STREAM_CODEC, Direction.UP, v -> true));
    }

    protected ResourceHandler<ItemResource> computeInputItems() {
        if (level != null && level.isClientSide()) {
            return new ItemStacksResourceHandler(2) {
                @Override
                protected void onContentsChanged(int index, ItemStack previousContents) {
                    super.onContentsChanged(index, previousContents);
                    setChanged();
                }
            };
        }
        return VanillaContainerWrapper.of(resultGenerator().inputSlots);
    }

    protected AnvilMenu computeResultGenerator() {
        var player = FakePlayerFactory.get((ServerLevel) level, FAKE_PLAYER_PROFILE);
        var result = new AnvilMenu(0, new Inventory(player, new PlayerEquipment(player))) {
            @Override
            public void createResultInternal() {
                itemName = inputSlots.getItem(0).getHoverName().getString();
                super.createResultInternal();
            }

            @Override
            public void slotsChanged(@Nonnull Container container) {
                super.slotsChanged(container);
                setChanged();
            }
        };
        result.suppressRemoteUpdates();
        if (loadedItems != null) {
            Util.initFromList(result.inputSlots, loadedItems);
            loadedItems = null;
        }
        return result;
    }

    public Slot getInputSlot(int index, int x, int y) {
        if (level.isClientSide()) {
            var handler = (ItemStacksResourceHandler) inputItems();
            return new ResourceHandlerSlot(handler, handler::set, index, x, y);
        }
        return new Slot(resultGenerator().inputSlots, index, x, y);
    }

    protected Optional<ResourceHandler<FluidResource>> externalTankHandler() {
        var dir = (Direction) syncMap().get(0);
        return Optional.ofNullable(level.getCapability(Capabilities.Fluid.BLOCK, worldPosition.relative(dir), dir.getOpposite()));
    }

    private ResourceHandler<ItemResource> computeItemHandler() {
        return new CombinedResourceHandler<>(new DelegatingResourceHandler<>(inputItems()) {
            @Override
            public int extract(ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
                return 0;
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
                return 0;
            }
        }, resultHandler) {
            @Override
            public boolean isValid(int index, ItemResource resource) {
                return index < inputItems().size();
            }
        };
    }

    protected boolean tryExtractLiquidXP(int amount, Transaction parentTxn) {
        return externalTankHandler().map(cap -> {
            var fluids = level.registryAccess().lookupOrThrow(Registries.FLUID).getOrThrow(Util.XP);
            for (var fluidHolder: fluids) {
                try (var txn = Transaction.open(parentTxn)) {
                    if (cap.extract(FluidResource.of(fluidHolder.value()), amount, txn) >= amount) {
                        txn.commit();
                        return true;
                    }
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
            try (var txn = Transaction.openRoot()) {
                if (!resultItem.isEmpty()
                        && resultHandler.insert(ItemResource.of(resultItem), resultItem.count(), txn) >= resultItem.count()
                        && tryExtractLiquidXP(requiredXP, txn)) {
                    txn.commit();
                    resultGenerator.resultSlots.setItem(0, ItemStack.EMPTY);
                    try {
                        ON_TAKE.invoke(resultGenerator, resultGenerator.player, resultItem);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        loadedItems = input.read("inputItems", ItemStack.OPTIONAL_CODEC.listOf(2, 2)).get();
        if (level instanceof ServerLevel) {
            Util.initFromList(resultGenerator().inputSlots, loadedItems);
            loadedItems = null;
        }
        input.readChild("resultItem", resultHandler);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        var toStore = Arrays.asList(level instanceof ServerLevel ? Util.toArray(resultGenerator().inputSlots) : Util.toArray(inputItems()));
        output.store("inputItems", ItemStack.OPTIONAL_CODEC.listOf(2, 2), toStore);
        output.putChild("resultItem", resultHandler);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new AutoAnvilMenu(i, inventory, this);
    }
}
