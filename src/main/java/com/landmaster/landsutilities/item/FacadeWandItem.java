package com.landmaster.landsutilities.item;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.network.UpdateFacadePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FacadeWandItem extends Item {
    public FacadeWandItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nonnull Item.TooltipContext context, @Nonnull TooltipDisplay display, @Nonnull Consumer<Component> builder, @Nonnull TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.landsutilities.facade_wand").withStyle(ChatFormatting.AQUA));
        var facade = itemStack.get(LandsUtilities.CURRENT_FACADE);
        if (facade != null) {
            builder.accept(Component.translatable("tooltip.landsutilities.facade_wand.current_facade", facade.toString()).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Nonnull
    @Override
    public InteractionResult onItemUseFirst(@Nonnull ItemStack stack, UseOnContext context) {
        var clickedPos = context.getClickedPos();
        var level = context.getLevel();
        if (context.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                var blockState = level.getBlockState(clickedPos);
                if (!blockState.isAir() && blockState.getRenderShape() == RenderShape.MODEL) {
                    stack.set(LandsUtilities.CURRENT_FACADE, blockState);
                    var player = context.getPlayer();
                    if (player != null) {
                        player.sendSystemMessage(Component.translatable("message.landsutilities.facade_wand.set_wand_facade", blockState.toString()));
                    }
                }
            }
        } else {
            var currentFacade = stack.get(LandsUtilities.CURRENT_FACADE);
            if (!level.isClientSide() && currentFacade != null) {
                var chunk = level.getChunk(clickedPos);
                var facadeData = chunk.getData(LandsUtilities.FACADE_STATES);
                if (facadeData.get(clickedPos.asLong()) != currentFacade) {
                    chunk.getData(LandsUtilities.FACADE_STATES).put(clickedPos.asLong(), currentFacade);
                    chunk.markUnsaved();
                    PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunk.getPos(), new UpdateFacadePacket(clickedPos, currentFacade));
                } else {
                    chunk.getData(LandsUtilities.FACADE_STATES).remove(clickedPos.asLong());
                    chunk.markUnsaved();
                    PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) level, chunk.getPos(), new UpdateFacadePacket(clickedPos, Blocks.AIR.defaultBlockState()));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
