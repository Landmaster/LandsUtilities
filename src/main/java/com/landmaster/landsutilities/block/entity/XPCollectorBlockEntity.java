package com.landmaster.landsutilities.block.entity;

import com.landmaster.landsutilities.Config;
import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.menu.XPCollectorMenu;
import com.landmaster.landsutilities.util.SyncInfo;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class XPCollectorBlockEntity extends BaseBlockEntity implements MenuProvider {
    public XPCollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandsUtilities.XP_COLLECTOR_TE.get(), pos, blockState,
                new SyncInfo<>("radius", Codec.BYTE, ByteBufCodecs.BYTE, (byte)Config.XP_COLLECTOR_RADIUS.getAsInt(), v -> 1 <= v && v <= Config.XP_COLLECTOR_RADIUS.getAsInt()),
                new SyncInfo<>("offsetX", Codec.BYTE, ByteBufCodecs.BYTE, (byte)0, Config::offsetInRange),
                new SyncInfo<>("offsetY", Codec.BYTE, ByteBufCodecs.BYTE, (byte)0, Config::offsetInRange),
                new SyncInfo<>("offsetZ", Codec.BYTE, ByteBufCodecs.BYTE, (byte)0, Config::offsetInRange)
        );
    }

    public AABB getRange() {
        return AABB.encapsulatingFullBlocks(worldPosition, worldPosition)
                .move((byte) syncMap().get(1), (byte) syncMap().get(2), (byte) syncMap().get(3))
                .inflate((byte) syncMap().get(0));
    }

    @Override
    public void tick() {
        super.tick();
        var xpOrbs = level.getEntities(
                EntityTypeTest.forClass(ExperienceOrb.class),
                getRange(),
                v -> true);
        var dir = getBlockState().getValue(FACING);
        var cap = level.getCapability(Capabilities.Fluid.BLOCK, worldPosition.relative(dir.getOpposite()), dir);
        var resource = FluidResource.of(LandsUtilities.FLUID_XP_STILL.get());
        if (cap != null) {
            for (var orb : xpOrbs) {
                int valueToInsert;
                try (var txn = Transaction.openRoot()) {
                    valueToInsert = cap.insert(resource, 20 * orb.getValue(), txn) / 20;
                }
                try (var txn = Transaction.openRoot()) {
                    if (cap.insert(resource, valueToInsert * 20, txn) >= valueToInsert) {
                        orb.setValue(orb.getValue() - valueToInsert);
                        if (orb.getValue() <= 0) {
                            orb.discard();
                        }
                        txn.commit();
                    }
                }
            }
        }
    }

    @Override
    @Nonnull
    public Component getDisplayName() {
        return LandsUtilities.XP_COLLECTOR.get().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new XPCollectorMenu(containerId, inventory, this);
    }
}
