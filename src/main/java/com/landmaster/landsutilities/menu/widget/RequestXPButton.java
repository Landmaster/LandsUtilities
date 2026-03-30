package com.landmaster.landsutilities.menu.widget;

import com.landmaster.landsutilities.block.entity.XPInterfaceBlockEntity;
import com.landmaster.landsutilities.network.RequestXPInterfacePacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nonnull;

public class RequestXPButton extends Button {
    private static Component levelsToTooltip(int levels) {
        if (levels > 0) {
            return Component.translatable("gui.landsutilities.grant_xp", levels);
        } else {
            return Component.translatable("gui.landsutilities.store_xp", levels);
        }
    }

    private static Component levelsToMessage(int levels) {
        return Component.literal(String.format("%+d", levels));
    }

    public RequestXPButton(int x, int y, int levels, XPInterfaceBlockEntity blockEntity) {
        super(x, y, 30, 14, levelsToMessage(levels), btn -> {
            if (blockEntity == null) return;
            ClientPacketDistributor.sendToServer(new RequestXPInterfacePacket(blockEntity.getBlockPos(), levels));
        }, DEFAULT_NARRATION);
        setTooltip(Tooltip.create(levelsToTooltip(levels)));
    }

    @Override
    protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
