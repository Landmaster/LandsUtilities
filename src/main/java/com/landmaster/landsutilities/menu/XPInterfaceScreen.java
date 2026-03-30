package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.menu.widget.RequestXPButton;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class XPInterfaceScreen extends ModContainerScreen<XPInterfaceMenu> {
    private static final Identifier BACKGROUND = Util.loc("textures/gui/xp_interface.png");

    public XPInterfaceScreen(XPInterfaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(new RequestXPButton(leftPos + 8, topPos + 32, -1, menu.blockEntity()));
        this.addRenderableWidget(new RequestXPButton(leftPos + 38, topPos + 32, -10, menu.blockEntity()));
        this.addRenderableWidget(new RequestXPButton(leftPos + 68, topPos + 32, -100, menu.blockEntity()));
        this.addRenderableWidget(new RequestXPButton(leftPos + 8, topPos + 46, 1, menu.blockEntity()));
        this.addRenderableWidget(new RequestXPButton(leftPos + 38, topPos + 46, 10, menu.blockEntity()));
        this.addRenderableWidget(new RequestXPButton(leftPos + 68, topPos + 46, 100, menu.blockEntity()));
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        if (menu.blockEntity() != null) {
            graphics.text(
                    font,
                    Component.translatable(
                            "gui.landsutilities.xp_amount",
                            Util.formatBucketValue(menu.blockEntity().fluidXp()), Util.formatBucketValue(menu.blockEntity().capacity())
                    ),
                    leftPos + 8, topPos + 20, 0xFF000000, false);
        }
    }
}
