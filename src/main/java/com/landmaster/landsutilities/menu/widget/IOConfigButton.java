package com.landmaster.landsutilities.menu.widget;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import javax.annotation.Nonnull;

public class IOConfigButton extends Button {
    private final BaseBlockEntity blockEntity;
    private final int id;
    private final String langKey;

    public IOConfigButton(int x, int y, int width, int height, BaseBlockEntity blockEntity, int id, boolean allowNone, String langKey) {
        super(x, y, width, height, Component.literal(""), btn -> {
            if (blockEntity == null) {
                return;
            }
            var newConfig = Util.cycleDirection((Direction) blockEntity.syncMap().get(id), allowNone);
            ClientPacketDistributor.sendToServer(blockEntity.syncMap().generatePacket(id, blockEntity.getBlockPos(), newConfig));
        }, DEFAULT_NARRATION);
        this.id = id;
        this.langKey = langKey;
        this.blockEntity = blockEntity;
    }

    @Override
    protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (blockEntity != null) {
            setMessage(Component.translatable(langKey,
                    Util.directionToComponent((Direction) blockEntity.syncMap().get(id))));
        }
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
