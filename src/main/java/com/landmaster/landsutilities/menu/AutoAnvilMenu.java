package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.AutoAnvilBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;

public class AutoAnvilMenu extends ModContainerMenu<AutoAnvilBlockEntity> {
    public AutoAnvilMenu(int containerId, Inventory inventory, AutoAnvilBlockEntity blockEntity) {
        super(LandsUtilities.AUTO_ANVIL_MENU.get(), containerId, blockEntity);
        if (blockEntity != null) {
            var resultHandler = blockEntity.resultHandler();
            addSlot(blockEntity.getInputSlot(0, 27, 47));
            addSlot(blockEntity.getInputSlot(1, 76, 47));
            addSlot(new ResourceHandlerSlot(resultHandler, resultHandler::set, 0, 134, 47));
        }
        initInventory(inventory, 84);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int i) {
        if (blockEntity() == null) return ItemStack.EMPTY;

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
}
