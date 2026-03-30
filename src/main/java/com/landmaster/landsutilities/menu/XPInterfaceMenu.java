package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.XPInterfaceBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;

public class XPInterfaceMenu extends ModContainerMenu<XPInterfaceBlockEntity> {
    public XPInterfaceMenu(int containerId, Inventory inventory, XPInterfaceBlockEntity blockEntity) {
        super(LandsUtilities.XP_INTERFACE_MENU.get(), containerId, blockEntity);
        if (blockEntity != null) {
            var upgradeHandler = blockEntity.upgradeHandler();
            addSlot(new UpgradeHandlerSlot(upgradeHandler, 0, 152, 37));
        }
        initInventory(inventory, 84);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int i) {
        if (blockEntity() == null) return ItemStack.EMPTY;

        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(i);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (i < 1) {
                if (!this.moveItemStackTo(itemstack1, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
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
