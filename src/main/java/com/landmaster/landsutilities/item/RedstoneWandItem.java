package com.landmaster.landsutilities.item;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.RedstoneWandState;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class RedstoneWandItem extends Item implements MouseWheelItem {
    public RedstoneWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.landsutilities.redstone_wand").withStyle(ChatFormatting.AQUA));
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        return Component.translatable(
                this.getDescriptionId(stack),
                stack.getOrDefault(LandsUtilities.REDSTONE_WAND_MODE, RedstoneWandState.Type.ALWAYS_ON).getTranslatedName()
        );
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        if (context.isSecondaryUseActive()) {
            var level = context.getLevel();
            var player = context.getPlayer();
            if (!level.isClientSide && player != null) {
                var pos = context.getClickedPos();
                var blockState = level.getBlockState(pos);
                var chunk = level.getChunkAt(pos);
                var onBlocks = chunk.getData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
                var stack = player.getItemInHand(context.getHand());
                var type = stack.getOrDefault(LandsUtilities.REDSTONE_WAND_MODE, RedstoneWandState.Type.ALWAYS_ON);

                var val = onBlocks.get(pos.asLong());

                boolean shouldRemove = val != null && val.type() == type;
                if (shouldRemove) {
                    onBlocks.remove(pos.asLong());
                } else {
                    onBlocks.put(pos.asLong(), new RedstoneWandState(type));
                }
                chunk.setUnsaved(true);
                chunk.syncData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
                level.updateNeighborsAt(pos, blockState.getBlock());

                var message = shouldRemove
                        ? "message.landsutilities.redstone_wand.removed"
                        : "message.landsutilities.redstone_wand.added";
                var args = new ArrayList<Object>(List.of(pos.toShortString()));
                if (!shouldRemove) {
                    args.add(type.getTranslatedName());
                }
                player.sendSystemMessage(Component.translatable(message, args.toArray()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public void onMouseWheel(Player player, InteractionHand hand, boolean up) {
        var stack = player.getItemInHand(hand);
        stack.update(LandsUtilities.REDSTONE_WAND_MODE, RedstoneWandState.Type.ALWAYS_ON, v -> Util.cycleEnum(v, !up));
    }

    @SubscribeEvent
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.isCanceled() && !event.getEntity().isSecondaryUseActive()) {
            var level = event.getLevel();
            var pos = event.getPos();
            var chunk = level.getChunkAt(pos);
            var onBlocks = chunk.getData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
            var val = onBlocks.get(pos.asLong());
            if (val != null && val.type().hasRightClickHandling()) {
                event.setCanceled(true);
                if (!level.isClientSide) {
                    val = val.handleRightClick((ServerLevel) level, pos);
                    onBlocks.put(pos.asLong(), val);
                    chunk.setUnsaved(true);
                    chunk.syncData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
                    level.updateNeighborsAt(pos, chunk.getBlockState(pos).getBlock());
                }
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
            }
        }
    }
}
