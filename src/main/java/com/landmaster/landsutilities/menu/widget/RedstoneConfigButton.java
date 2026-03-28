package com.landmaster.landsutilities.menu.widget;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.network.RedstoneConfigPacket;
import com.landmaster.landsutilities.util.RedstoneConfig;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import javax.annotation.Nonnull;

public class RedstoneConfigButton extends Button {
    private static final Identifier ICONS = Util.loc("textures/gui/redstone_mode.png");

    private final BaseBlockEntity blockEntity;

    public RedstoneConfigButton(int x, int y, BaseBlockEntity blockEntity) {
        super(x, y, 14, 14, Component.literal(""), btn -> {
            if (blockEntity != null) {
                ClientPacketDistributor.sendToServer(new RedstoneConfigPacket(blockEntity.getBlockPos(), Util.cycleEnum(blockEntity.redstoneConfig())));
            }
        }, DEFAULT_NARRATION);
        this.blockEntity = blockEntity;
    }

    @Override
    protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        var redstoneConfig = RedstoneConfig.IGNORE;
        if (blockEntity != null) {
            redstoneConfig = blockEntity.redstoneConfig();
        }
        setTooltip(Tooltip.create(redstoneConfig.getTranslatedName()));
        this.extractDefaultSprite(graphics);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, getX()+2, getY()+2, redstoneConfig.ordinal() * 10, 0, 10, 10, 32, 32);
    }
}
