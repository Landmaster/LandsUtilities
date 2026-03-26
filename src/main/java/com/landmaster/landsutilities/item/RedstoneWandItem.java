package com.landmaster.landsutilities.item;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import javax.annotation.Nonnull;

public class RedstoneWandItem extends Item {
    public RedstoneWandItem(Properties properties) {
        super(properties);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var player = context.getPlayer();
        var blockState = level.getBlockState(pos);
        if (context.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                var chunk = level.getChunkAt(pos);
                var onBlocks = chunk.getData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
                boolean shouldRemove = onBlocks.contains(pos.asLong());
                if (shouldRemove) {
                    onBlocks.remove(pos.asLong());
                } else {
                    onBlocks.add(pos.asLong());
                }
                chunk.setUnsaved(true);
                chunk.syncData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
                level.updateNeighborsAt(pos, blockState.getBlock());
                if (player != null) {
                    var message = shouldRemove
                            ? "message.landsutilities.redstone_wand.removed"
                            : "message.landsutilities.redstone_wand.added";
                    player.sendSystemMessage(Component.translatable(message, pos.toShortString()));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }
}
