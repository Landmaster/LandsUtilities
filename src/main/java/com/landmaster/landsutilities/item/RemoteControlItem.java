package com.landmaster.landsutilities.item;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Streams;
import com.landmaster.landsutilities.Config;
import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.RemoteControlLink;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.apache.commons.lang3.mutable.MutableFloat;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class RemoteControlItem extends Item implements MouseWheelItem {

    public static final ConcurrentMap<AbstractContainerMenu, Double> MENU_TO_REMOTE_RANGE = new MapMaker()
            .weakKeys()
            .makeMap();
    public static final ThreadLocal<Double> DESIRED_RANGE = ThreadLocal.withInitial(() -> 0.0);

    public RemoteControlItem(Properties properties) {
        super(properties);
    }

    public static int getMaxLinkedBlocks(ItemStack stack, ServerLevel level) {
        int maxLinked = Config.MAX_LINKED_BLOCKS.get();
        MutableFloat enchantmentBonus = new MutableFloat(0);
        EnchantmentHelper.runIterationOnItem(stack, (enchant, enchantLevel) -> {
            enchant.value().modifyItemFilteredCount(
                    LandsUtilities.REMOTE_CONTROL_CAPACITY.get(),
                    level,
                    enchantLevel,
                    stack,
                    enchantmentBonus
            );
        });
        return maxLinked + (int) enchantmentBonus.floatValue();
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
        if (player != null && context.isSecondaryUseActive()) {
            if (!level.isClientSide) {
                var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
                if (linkedBlocks.size() < getMaxLinkedBlocks(stack, (ServerLevel) level)) {
                    var blockState = level.getBlockState(context.getClickedPos());
                    if (!Config.isRemoteBlacklisted(blockState.getBlock())) {
                        var cloneStack = blockState.getCloneItemStack(context.getHitResult(), level, context.getClickedPos(), player);
                        var newEntry = new RemoteControlLink(
                                cloneStack.isEmpty() ? blockState.getBlock().getName() : cloneStack.getHoverName(),
                                context.getClickedPos(),
                                level.dimension(),
                                context.getClickedFace()
                        );
                        if (!linkedBlocks.contains(newEntry)) {
                            stack.set(LandsUtilities.LINKED_MENU_BLOCKS, Streams.concat(
                                    linkedBlocks.stream(), Stream.of(newEntry)
                            ).toList());

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
            var result = linked(stack).map(link -> {
                if (level.isClientSide) {
                    return InteractionResultHolder.success(stack);
                } else {
                    if (level.dimension() != link.dimension()) {
                        return InteractionResultHolder.fail(stack);
                    }
                    var state = level.getBlockState(link.pos());
                    if (Config.isRemoteBlacklisted(state.getBlock())) {
                        return InteractionResultHolder.fail(stack);
                    }
                    var desiredRange = new MutableFloat(1);
                    EnchantmentHelper.runIterationOnItem(stack, (enchant, enchantLevel) -> {
                        enchant.value().modifyItemFilteredCount(
                                LandsUtilities.REMOTE_RANGE.get(),
                                (ServerLevel) level,
                                enchantLevel,
                                stack,
                                desiredRange
                        );
                    });
                    try {
                        DESIRED_RANGE.set(desiredRange.doubleValue());
                        return new InteractionResultHolder<>(state.useWithoutItem(level, player, new BlockHitResult(
                                Vec3.atCenterOf(link.pos()).relative(link.face(), 0.5),
                                link.face(),
                                link.pos(),
                                false
                        )), stack);
                    } finally {
                        DESIRED_RANGE.set(0.0);
                    }
                }
            });
            if (result.isPresent()) return result.get();
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void onMouseWheel(Player player, InteractionHand hand, boolean up) {
        var stack = player.getItemInHand(hand);
        var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
        if (!linkedBlocks.isEmpty()) {
            stack.update(LandsUtilities.LINKED_MENU_INDEX, 0, v -> (v + (up ? 1 : linkedBlocks.size()-1)) % linkedBlocks.size());
        }
    }

    @Override
    public boolean isEnchantable(@Nonnull ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(@Nonnull ItemStack stack) {
        return 10;
    }

    @SubscribeEvent
    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        double desiredRange = DESIRED_RANGE.get();
        if (desiredRange > 1.0) {
            MENU_TO_REMOTE_RANGE.put(event.getContainer(), desiredRange);
        }
    }
}
