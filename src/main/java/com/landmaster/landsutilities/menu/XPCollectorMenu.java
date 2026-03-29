package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.XPCollectorBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class XPCollectorMenu extends ModContainerMenu<XPCollectorBlockEntity> {
    public XPCollectorMenu(int containerId, Inventory inventory, XPCollectorBlockEntity blockEntity) {
        super(LandsUtilities.XP_COLLECTOR_MENU.get(), containerId, blockEntity);
        initInventory(inventory, 84);
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(@Nonnull Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }
}
