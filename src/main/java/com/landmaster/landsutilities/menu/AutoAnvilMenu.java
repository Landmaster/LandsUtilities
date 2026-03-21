package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.AutoAnvilBlockEntity;
import com.landmaster.landsutilities.util.ClientMenuUtil;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class AutoAnvilMenu extends AbstractContainerMenu {
    @Getter
    private final AutoAnvilBlockEntity blockEntity;

    private class TESlot extends SlotItemHandler {

        public TESlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (blockEntity != null) {
                blockEntity.setChanged();
            }
        }
    }

    public AutoAnvilMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, ClientMenuUtil.getBlockEntity(buf.readBlockPos(), LandsUtilities.AUTO_ANVIL_TE.get()));
    }

    public AutoAnvilMenu(int containerId, Inventory inventory, AutoAnvilBlockEntity blockEntity) {
        super(LandsUtilities.AUTO_ANVIL_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        if (blockEntity != null) {
            var handler = blockEntity.guiItemHandler();
            addSlot(new TESlot(handler, 0, 27, 47));
            addSlot(new TESlot(handler, 1, 76, 47));
            addSlot(new TESlot(handler, 2, 134, 47));

            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 9; j++) {
                    addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
                }
            }

            for (int i=0; i<9; ++i) {
                addSlot(new Slot(inventory, i, 8 + i * 18, 142));
            }
        }
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (i < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return blockEntity != null && !blockEntity.isRemoved() && player.canInteractWithBlock(blockEntity.getBlockPos(), 4.0);
    }
}
