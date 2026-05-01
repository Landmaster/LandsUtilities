package com.landmaster.landsutilities.item;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Streams;
import com.landmaster.landsutilities.Config;
import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.RemoteControlLink;
import com.landmaster.landsutilities.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class RemoteControlItem extends Item implements MouseWheelItem {
    public record MenuData(Player player, float range) {}

    public static final ConcurrentMap<AbstractContainerMenu, MenuData> MENU_TO_REMOTE_RANGE = new MapMaker()
            .weakKeys()
            .makeMap();

    public static final ThreadLocal<Float> DESIRED_RANGE = ThreadLocal.withInitial(() -> 0.0f);
    public static final ThreadLocal<BlockPos> DESIRED_POS = ThreadLocal.withInitial(() -> BlockPos.ZERO);

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
                        "item.landsutilities.remote_control.current",
                        stack.getOrDefault(LandsUtilities.LINKED_MENU_INDEX, 0),
                        remoteControlLink.name()))
                .orElseGet(() -> Component.translatable("item.landsutilities.remote_control"));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(@Nonnull ItemStack itemStack, @Nonnull Item.TooltipContext context, @Nonnull TooltipDisplay display, @Nonnull Consumer<Component> builder, @Nonnull TooltipFlag tooltipFlag) {
        for (int i=0; i<6; ++i) {
            builder.accept(Component.translatable("tooltip.landsutilities.remote_control." + i).withStyle(ChatFormatting.AQUA));
        }

        var linkedBlocks = itemStack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
        builder.accept(Component.translatable("tooltip.landsutilities.linked_remote.amount", linkedBlocks.size()).withStyle(ChatFormatting.YELLOW));
    }

    @Nonnull
    @Override
    public InteractionResult useOn(@Nonnull UseOnContext context) {
        var stack = context.getItemInHand();
        var level = context.getLevel();
        var player = context.getPlayer();
        if (player != null && context.isSecondaryUseActive()) {
            if (!level.isClientSide()) {
                var linkedBlocks = stack.getOrDefault(LandsUtilities.LINKED_MENU_BLOCKS, List.<RemoteControlLink>of());
                if (linkedBlocks.size() < getMaxLinkedBlocks(stack, (ServerLevel) level)) {
                    var blockState = level.getBlockState(context.getClickedPos());
                    if (!Config.isRemoteBlacklisted(blockState.getBlock())) {
                        var cloneStack = blockState.getCloneItemStack(context.getClickedPos(), level, true, player);
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

                            player.sendSystemMessage(
                                    Component.translatable("message.landsutilities.linked_remote",
                                            newEntry.pos().toShortString(), newEntry.dimension().identifier().toString(),
                                            Util.directionToComponent(newEntry.face())));
                        }
                    } else {
                        player.sendSystemMessage(
                                Component.translatable("message.landsutilities.remote_block_blacklisted").withStyle(ChatFormatting.RED)
                        );
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand usedHand) {
        if (!player.isSecondaryUseActive()) {
            var stack = player.getItemInHand(usedHand);
            var result = linked(stack).map(link -> {
                if (level.isClientSide()) {
                    return InteractionResult.SUCCESS;
                } else {
                    if (level.dimension() != link.dimension()) {
                        return InteractionResult.FAIL;
                    }
                    var state = level.getBlockState(link.pos());
                    if (Config.isRemoteBlacklisted(state.getBlock())) {
                        return InteractionResult.FAIL;
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
                        DESIRED_RANGE.set(desiredRange.floatValue());
                        DESIRED_POS.set(link.pos());
                        return state.useWithoutItem(level, player, new BlockHitResult(
                                Vec3.atCenterOf(link.pos()).relative(link.face(), 0.5),
                                link.face(),
                                link.pos(),
                                false
                        ));
                    } finally {
                        DESIRED_RANGE.set(0.0f);
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

    @SubscribeEvent
    private static void onContainerOpen(PlayerContainerEvent.Open event) {
        float desiredRange = DESIRED_RANGE.get();
        if (desiredRange > 1.0) {
            MENU_TO_REMOTE_RANGE.put(event.getContainer(), new MenuData(event.getEntity(), desiredRange));
        }
    }
}
