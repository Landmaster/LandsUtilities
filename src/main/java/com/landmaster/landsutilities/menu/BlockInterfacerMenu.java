package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.BlockInterfacerBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import javax.annotation.Nonnull;

public class BlockInterfacerMenu extends ModContainerMenu<BlockInterfacerBlockEntity> {
    public BlockInterfacerMenu(int containerId, Inventory inventory, BlockInterfacerBlockEntity blockEntity) {
        super(LandsUtilities.BLOCK_INTERFACER_MENU.get(), containerId, blockEntity);
        if (blockEntity != null) {
            addSlot(new ResourceHandlerSlot(blockEntity.itemHandler(), blockEntity.itemHandler()::set, 0, 80, 36));
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
