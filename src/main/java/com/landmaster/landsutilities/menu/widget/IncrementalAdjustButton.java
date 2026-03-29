package com.landmaster.landsutilities.menu.widget;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nonnull;

public class IncrementalAdjustButton extends Button {
    public IncrementalAdjustButton(int x, int y, BaseBlockEntity blockEntity, int id, boolean minus) {
        super(x, y, 12, 12, Component.literal(minus ? "-" : "+"), (button) -> {
            if (blockEntity == null) return;
            ClientPacketDistributor.sendToServer(blockEntity.syncMap().generatePacket(
                    id, blockEntity.getBlockPos(), (byte) ((byte)blockEntity.syncMap().get(id) + (minus ? -1 : 1))
            ));
        }, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
