package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.menu.widget.IncrementalAdjustButton;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class XPCollectorScreen extends AbstractContainerScreen<XPCollectorMenu> {
    private static final Identifier BACKGROUND = Util.loc("textures/gui/xp_collector.png");

    public XPCollectorScreen(XPCollectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        for (int id = 0; id < 4; ++id) {
            addRenderableWidget(new IncrementalAdjustButton(leftPos + 70, topPos + 16 + id * 14, menu.blockEntity(), id, true));
            addRenderableWidget(new IncrementalAdjustButton(leftPos + 100, topPos + 16 + id * 14, menu.blockEntity(), id, false));
        }
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        graphics.text(font, Component.translatable("gui.landsutilities.radius"), leftPos+12, topPos+18, 0xFF000000, false);
        graphics.text(font, Component.translatable("gui.landsutilities.offset.x"), leftPos+12, topPos+32, 0xFF000000, false);
        graphics.text(font, Component.translatable("gui.landsutilities.offset.y"), leftPos+12, topPos+46, 0xFF000000, false);
        graphics.text(font, Component.translatable("gui.landsutilities.offset.z"), leftPos+12, topPos+60, 0xFF000000, false);
        if (menu.blockEntity() != null) {
            for (int id = 0; id < 4; ++id) {
                var str = menu.blockEntity().syncMap().get(id).toString();
                var width = font.width(str);
                graphics.text(font, str, leftPos+91-width/2, topPos + 18 + id * 14, 0xFF000000, false);
            }
        }
    }
}
