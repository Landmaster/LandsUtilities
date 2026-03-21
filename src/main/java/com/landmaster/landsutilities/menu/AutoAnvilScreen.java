package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.menu.widget.IOConfigButton;
import com.landmaster.landsutilities.menu.widget.RedstoneConfigButton;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class AutoAnvilScreen extends AbstractContainerScreen<AutoAnvilMenu> {
    private static final ResourceLocation BACKGROUND = Util.loc("textures/gui/auto_anvil.png");

    public AutoAnvilScreen(AutoAnvilMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IOConfigButton(leftPos+80, topPos+6, 90, 14, menu.blockEntity(), "external_tank", false));
        addRenderableWidget(new RedstoneConfigButton(leftPos+156, topPos+20, menu.blockEntity()));
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }
}
