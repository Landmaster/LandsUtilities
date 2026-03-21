package com.landmaster.landsutilities.menu.widget;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.network.IOConfigPacket;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;

public class IOConfigButton extends Button {
    private final String key;
    private final BaseBlockEntity blockEntity;

    public IOConfigButton(int x, int y, int width, int height, BaseBlockEntity blockEntity, String key, boolean allowNone) {
        super(x, y, width, height, Component.literal(""), btn -> {
            if (blockEntity == null) {
                return;
            }
            var newConfig = Util.cycleConfiguration(blockEntity.getConfiguration(key).orElse(null), allowNone);
            PacketDistributor.sendToServer(new IOConfigPacket(blockEntity.getBlockPos(), key, newConfig));
        }, DEFAULT_NARRATION);
        this.key = key;
        this.blockEntity = blockEntity;
    }

    @Override
    protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (blockEntity != null) {
            setMessage(Component.translatable("gui.landsutilities.config." + key,
                    Util.configToComponent(blockEntity.getConfiguration(key).orElse(null))));
        }
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
}
