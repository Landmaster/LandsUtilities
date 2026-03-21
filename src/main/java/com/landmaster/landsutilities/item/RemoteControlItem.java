package com.landmaster.landsutilities.item;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.Location;
import com.landmaster.landsutilities.util.LocationAndFace;
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

public class RemoteControlItem extends Item {
    public RemoteControlItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        var linked = stack.get(LandsUtilities.LINKED_MENU_BLOCK);
        if (linked != null) {
            tooltipComponents.add(Component.translatable("tooltip.landsutilities.linked_remote",
                    linked.location().pos().toShortString(),
                    linked.location().dimension().location().getPath(),
                    Util.configToComponent(linked.face())).withStyle(ChatFormatting.YELLOW));
        }
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        var stack = context.getItemInHand();
        var level = context.getLevel();
        var player = context.getPlayer();
        if (context.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                stack.set(LandsUtilities.LINKED_MENU_BLOCK, new LocationAndFace(
                        new Location(context.getClickedPos(), level.dimension()),
                        context.getClickedFace()
                ));
                if (player != null) {
                    player.displayClientMessage(
                            Component.translatable("message.landsutilities.linked_remote",
                                    context.getClickedPos().toShortString(), level.dimension().location().toString(),
                                    Util.configToComponent(context.getClickedFace())),
                            false);
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
            var linked = stack.get(LandsUtilities.LINKED_MENU_BLOCK);
            if (linked != null) {
                if (level.isClientSide) {
                    return InteractionResultHolder.success(stack);
                } else {
                    var dimension = linked.location().dimension();
                    var targetLevel = ((ServerLevel) level).getServer().getLevel(dimension);
                    if (targetLevel == null) {
                        return InteractionResultHolder.fail(stack);
                    }
                    var state = targetLevel.getBlockState(linked.location().pos());
                    return new InteractionResultHolder<>(state.useWithoutItem(targetLevel, player, new BlockHitResult(
                            Vec3.atCenterOf(linked.location().pos()).relative(linked.face(), 0.5),
                            linked.face(),
                            linked.location().pos(),
                            false
                    )), stack);
                }
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return stack.has(LandsUtilities.LINKED_MENU_BLOCK);
    }
}
