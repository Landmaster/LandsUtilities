package com.landmaster.landsutilities.block.entity;

import com.landmaster.landsutilities.Config;
import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.menu.XPInterfaceMenu;
import com.landmaster.landsutilities.network.SyncXPInterfacePacket;
import com.landmaster.landsutilities.util.UpgradeItemHandler;
import com.landmaster.landsutilities.util.Util;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class XPInterfaceBlockEntity extends BaseBlockEntity implements MenuProvider {
    @Getter
    private final UpgradeItemHandler upgradeHandler = new UpgradeItemHandler(1, "capacity") {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
        }
    };

    private int oldFluidXp;
    @Getter @Setter
    private int fluidXp;

    public class FluidHandler implements ResourceHandler<FluidResource> {
        private class Journal extends SnapshotJournal<Integer> {
            @Override
            protected Integer createSnapshot() {
                return fluidXp;
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                fluidXp = snapshot;
            }

            @Override
            protected void onRootCommit(Integer originalState) {
                setChanged();
            }
        }

        private final Journal journal = new Journal();

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int index) {
            return FluidResource.of(LandsUtilities.FLUID_XP_STILL);
        }

        @Override
        public long getAmountAsLong(int index) {
            return fluidXp;
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return capacity();
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.is(Util.XP);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, @Nonnull TransactionContext transaction) {
            if (!resource.is(Util.XP)) return 0;
            int inserted = (int) Math.min(getCapacityAsLong(index, resource) - getAmountAsLong(index), amount);
            if (inserted > 0) {
                journal.updateSnapshots(transaction);
                fluidXp += amount;
            }
            return inserted;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, @Nonnull TransactionContext transaction) {
            if (!resource.is(Util.XP)) return 0;
            int extracted = Math.min(fluidXp, amount);
            if (extracted > 0) {
                journal.updateSnapshots(transaction);
                fluidXp -= amount;
            }
            return extracted;
        }
    }

    @Getter
    private final FluidHandler fluidHandler = new FluidHandler();

    public XPInterfaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandsUtilities.XP_INTERFACE_TE.get(), pos, blockState);
    }

    public int capacity() {
        return Config.levelToValue(Config.XP_INTERFACE_STORAGE, upgradeHandler.getUpgradeLevel("capacity"));
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        input.readChild("upgradeHandler", upgradeHandler);
        oldFluidXp = fluidXp = input.getIntOr("fluidXp", 0);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("upgradeHandler", upgradeHandler);
        output.putInt("fluidXp", fluidXp);
    }

    @Override
    public void tick() {
        super.tick();
        if (fluidXp != oldFluidXp) {
            PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, ChunkPos.containing(worldPosition), new SyncXPInterfacePacket(worldPosition, fluidXp));
            oldFluidXp = fluidXp;
        }
    }


    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new XPInterfaceMenu(containerId, inventory, this);
    }
}
