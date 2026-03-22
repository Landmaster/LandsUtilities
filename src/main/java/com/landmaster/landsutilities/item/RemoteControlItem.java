package com.landmaster.landsutilities.item;

import com.google.common.collect.Streams;
import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.RemoteControlLink;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RemoteControlItem extends Item implements MouseWheelItem {
    public static final int LIMIT = 5;

    public RemoteControlItem(Properties properties) {
        super(properties);
    }

    public static Optional<RemoteControlLink> linked(ItemStack stack) {
        var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
        int index = stack.getOrDefault(LandsUtilities.LINKED_MENU_INDEX, 0);
        if (index >= 0 && index < linkedBlocks.size()) {
            return Optional.of(linkedBlocks.get(index));
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Component getName(@Nonnull ItemStack stack) {
        var linked = linked(stack);
        return linked
                .map(remoteControlLink -> Component.translatable(
                        this.getDescriptionId(stack) + ".current",
                        stack.getOrDefault(LandsUtilities.LINKED_MENU_INDEX, 0),
                        remoteControlLink.name()))
                .orElseGet(() -> Component.translatable(this.getDescriptionId(stack)));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("tooltip.landsutilities.remote_control").withStyle(ChatFormatting.AQUA));

        var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
        tooltipComponents.add(Component.translatable("tooltip.landsutilities.linked_remote.amount", linkedBlocks.size()).withStyle(ChatFormatting.YELLOW));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        var stack = context.getItemInHand();
        var level = context.getLevel();
        var player = context.getPlayer();
        if (context.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
                if (linkedBlocks.size() < LIMIT) {
                    var blockState = level.getBlockState(context.getClickedPos());
                    var newEntry = new RemoteControlLink(
                            blockState.getBlock().getName(),
                            context.getClickedPos(),
                            level.dimension(),
                            context.getClickedFace()
                    );
                    if (!linkedBlocks.contains(newEntry)) {
                        stack.set(LandsUtilities.LINKED_MENU_BLOCKS, Streams.concat(
                                linkedBlocks.stream(), Stream.of(newEntry)
                        ).toList());

                        if (player != null) {
                            player.displayClientMessage(
                                    Component.translatable("message.landsutilities.linked_remote",
                                            newEntry.pos().toShortString(), newEntry.dimension().location().toString(),
                                            Util.configToComponent(newEntry.face())),
                                    false);
                        }
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useOn(context);
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand usedHand) {
        if (!player.isSecondaryUseActive()) {
            var stack = player.getItemInHand(usedHand);
            var result = linked(stack).map(l -> {
                if (level.isClientSide) {
                    return InteractionResultHolder.success(stack);
                } else {
                    var dimension = l.dimension();
                    var targetLevel = ((ServerLevel) level).getServer().getLevel(dimension);
                    if (targetLevel == null) {
                        return InteractionResultHolder.fail(stack);
                    }
                    var state = targetLevel.getBlockState(l.pos());
                    return new InteractionResultHolder<>(state.useWithoutItem(targetLevel, player, new BlockHitResult(
                            Vec3.atCenterOf(l.pos()).relative(l.face(), 0.5),
                            l.face(),
                            l.pos(),
                            false
                    )), stack);
                }
            });
            if (result.isPresent()) return result.get();
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return linked(stack).isPresent();
    }

    @Override
    public void onMouseWheel(Player player, InteractionHand hand, boolean up) {
        var stack = player.getItemInHand(hand);
        var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
        if (!linkedBlocks.isEmpty()) {
            stack.update(LandsUtilities.LINKED_MENU_INDEX, 0, v -> (v + (up ? 1 : linkedBlocks.size()-1)) % linkedBlocks.size());
        }
    }
}
