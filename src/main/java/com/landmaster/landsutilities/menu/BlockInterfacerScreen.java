package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.menu.widget.CycleValueButton;
import com.landmaster.landsutilities.menu.widget.IncrementalAdjustButton;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class BlockInterfacerScreen extends ModContainerScreen<BlockInterfacerMenu> {
    private static final Identifier BACKGROUND = Util.loc("textures/gui/block_interfacer.png");

    public BlockInterfacerScreen(BlockInterfacerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new CycleValueButton(leftPos + 7, topPos + 16, 50, 14, menu.blockEntity(), 5,
                dir -> Util.directionToComponent((Direction) dir)));
        addRenderableWidget(new CycleValueButton(leftPos + 7, topPos + 30, 64, 14, menu.blockEntity(), 3,
                val -> Component.translatable("gui.landsutilities.left_click." + val)));
        addRenderableWidget(new CycleValueButton(leftPos + 7, topPos + 44, 60, 14, menu.blockEntity(), 4,
                val -> Component.translatable("gui.landsutilities.sneak." + val)));

        addRenderableWidget(new IncrementalAdjustButton(leftPos + 112, topPos + 30, menu.blockEntity(), 0, true));
        addRenderableWidget(new IncrementalAdjustButton(leftPos + 142, topPos + 30, menu.blockEntity(), 0, false));
        addRenderableWidget(new IncrementalAdjustButton(leftPos + 112, topPos + 44, menu.blockEntity(), 1, true));
        addRenderableWidget(new IncrementalAdjustButton(leftPos + 142, topPos + 44, menu.blockEntity(), 1, false));
        addRenderableWidget(new IncrementalAdjustButton(leftPos + 112, topPos + 58, menu.blockEntity(), 2, true));
        addRenderableWidget(new IncrementalAdjustButton(leftPos + 142, topPos + 58, menu.blockEntity(), 2, false));
    }

    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        var blockEntity = menu.blockEntity();
        if (blockEntity != null) {
            Component str;
            str = Component.translatable("gui.landsutilities.offset");
            graphics.text(font, str, leftPos+134-font.width(str)/2, topPos+16, 0xFF000000, false);
            str = Component.literal(blockEntity.syncMap().get(0).toString());
            graphics.text(font, str,leftPos+134-font.width(str)/2, topPos+32, 0xFF000000, false);
            str = Component.literal(blockEntity.syncMap().get(1).toString());
            graphics.text(font, str,leftPos+134-font.width(str)/2, topPos+46, 0xFF000000, false);
            str = Component.literal(blockEntity.syncMap().get(2).toString());
            graphics.text(font, str,leftPos+134-font.width(str)/2, topPos+60, 0xFF000000, false);
        }
    }
}
