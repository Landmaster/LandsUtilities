package com.landmaster.landsutilities.menu.widget;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import javax.annotation.Nonnull;
import java.util.function.Function;

public class CycleValueButton extends Button {
    private final BaseBlockEntity blockEntity;
    private final int id;
    protected Function<Object, Component> componentFactory;

    public CycleValueButton(int x, int y, int width, int height, BaseBlockEntity blockEntity, int id, Function<Object, Component> componentFactory) {
        super(x, y, width, height, Component.literal(""), btn -> {
            if (blockEntity == null) {
                return;
            }
            var newConfig = Util.cycleValue(blockEntity.syncMap().get(id));
            ClientPacketDistributor.sendToServer(blockEntity.syncMap().generatePacket(id, blockEntity.getBlockPos(), newConfig));
        }, DEFAULT_NARRATION);
        this.id = id;
        this.componentFactory = componentFactory;
        this.blockEntity = blockEntity;
    }

    @Override
    protected void extractContents(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (blockEntity != null) {
            setMessage(componentFactory.apply(blockEntity.syncMap().get(id)));
//            setMessage(Component.translatable(langKey,
//                    Util.directionToComponent((Direction) blockEntity.syncMap().get(id))));
        }
        this.extractDefaultSprite(graphics);
        this.extractDefaultLabel(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
