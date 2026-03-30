package com.landmaster.landsutilities.menu;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.Optional;

public abstract class ModContainerScreen<T extends ModContainerMenu<?>> extends AbstractContainerScreen<T> {
    public ModContainerScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void extractTooltip(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot instanceof UpgradeHandlerSlot upgradeHandlerSlot && !hoveredSlot.hasItem()) {
            graphics.setTooltipForNextFrame(font, upgradeHandlerSlot.tooltip(), Optional.empty(), mouseX, mouseY);
        }
    }
}
