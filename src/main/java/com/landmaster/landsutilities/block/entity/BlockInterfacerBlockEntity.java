package com.landmaster.landsutilities.block.entity;

import com.google.common.collect.HashMultimap;
import com.landmaster.landsutilities.Config;
import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.menu.BlockInterfacerMenu;
import com.landmaster.landsutilities.util.ExtendedFakePlayer;
import com.landmaster.landsutilities.util.SyncInfo;
import com.mojang.serialization.Codec;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.TriState;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class BlockInterfacerBlockEntity extends BaseBlockEntity implements MenuProvider {
    @Getter(lazy = true)
    private final ExtendedFakePlayer extendedFakePlayer = generateFakePlayer();

    @Getter
    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(1) {
        @Override
        protected void onContentsChanged(int index, @Nonnull ItemStack previousContents) {
            super.onContentsChanged(index, previousContents);
            setChanged();
        }
    };

    public BlockInterfacerBlockEntity(BlockPos pos, BlockState blockState) {
        super(LandsUtilities.BLOCK_INTERFACER_TE.get(), pos, blockState,
                new SyncInfo<>("offsetX", Codec.BYTE, ByteBufCodecs.BYTE, (byte)0, Config::interfacerOffsetInRange),
                new SyncInfo<>("offsetY", Codec.BYTE, ByteBufCodecs.BYTE, (byte)0, Config::interfacerOffsetInRange),
                new SyncInfo<>("offsetZ", Codec.BYTE, ByteBufCodecs.BYTE, (byte)0, Config::interfacerOffsetInRange),
                new SyncInfo<>("leftClick", Codec.BOOL, ByteBufCodecs.BOOL, false, b -> true),
                new SyncInfo<>("sneak", Codec.BOOL, ByteBufCodecs.BOOL, false, b -> true),
                new SyncInfo<>("direction", Direction.CODEC, Direction.STREAM_CODEC, Direction.UP, dir -> true));
    }

    protected ExtendedFakePlayer generateFakePlayer() {
        return new ExtendedFakePlayer((ServerLevel) level);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @Nonnull Inventory inventory, @Nonnull Player player) {
        return new BlockInterfacerMenu(i, inventory, this);
    }

    @Override
    public void tick() {
        super.tick();
        var result = doInteraction(itemHandler.getResource(0).toStack(itemHandler.getAmountAsInt(0)));
        itemHandler.set(0, ItemResource.of(result), result.getCount());
    }

    public BlockPos targetLocation() {
        var offsetX = (byte) syncMap().get(0);
        var offsetY = (byte) syncMap().get(1);
        var offsetZ = (byte) syncMap().get(2);
        return worldPosition.offset(offsetX, offsetY, offsetZ);
    }

    protected ItemStack returnPlayerInventory(Player player) {
        var result = player.getMainHandItem();
        player.getInventory().setSelectedItem(ItemStack.EMPTY);
        Containers.dropContents(level, worldPosition, player.getInventory());
        player.getInventory().clearContent();
        return result;
    }

    protected void cancelDestroyingBlock(ServerPlayer player) {
        player.gameMode.isDestroyingBlock = false;
        player.gameMode.lastSentState = -1;
        player.level().destroyBlockProgress(player.getId(), player.gameMode.destroyPos, -1);
    }

    protected ItemStack doInteraction(ItemStack stack) {
        var player = extendedFakePlayer();
        var leftClick = (boolean) syncMap().get(3);
        var sneak = (boolean) syncMap().get(4);
        var direction = (Direction) syncMap().get(5);
        var pos = targetLocation();
        if (!pos.equals(player.gameMode.destroyPos)) {
            cancelDestroyingBlock(player);
        }
        player.setPlayerState(InteractionHand.MAIN_HAND, pos, direction, sneak);
        player.getInventory().clearContent();
        player.setHeldItemSilent(InteractionHand.MAIN_HAND, stack);

        if (active) {
            if (leftClick) {
                PlayerInteractEvent.LeftClickBlock event = CommonHooks.onLeftClickBlock(player, pos, direction, ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK);
                BlockState blockState = level.getBlockState(pos);
                if (event.isCanceled() || (event.getUseItem() == TriState.FALSE)) { // Restore block and te data
                    level.sendBlockUpdated(pos, blockState, level.getBlockState(pos), 3);
                    cancelDestroyingBlock(player);
                    return stack;
                }

                if (!level.isEmptyBlock(pos)) {
                    // Break block
                    int durabilityRemaining = player.gameMode.lastSentState;
                    if (durabilityRemaining < 0) {
                        level.getBlockState(pos).attack(level, pos, player);
                        float relativeBlockHardness = blockState.getDestroyProgress(player, player.level(), pos);
                        if (relativeBlockHardness >= 1.0F) {
                            // Insta-mine
                            player.gameMode.destroyBlock(pos);
                        } else {
                            // Initiate break progress
                            player.gameMode.destroyProgressStart = player.gameMode.gameTicks;
                            player.gameMode.isDestroyingBlock = true;
                            player.gameMode.destroyPos = pos.immutable();
                            player.gameMode.lastSentState = (int) (relativeBlockHardness * 10.0F);
                        }
                    } else if (durabilityRemaining >= 9) {
                        player.gameMode.destroyBlock(pos);
                        cancelDestroyingBlock(player);
                    } else {
                        player.gameMode.tick();
                    }
                    return returnPlayerInventory(player);
                } else {
                    // Attack entity
                    cancelDestroyingBlock(player);

                    // Interact with entity
                    var entities = level.getEntitiesOfClass(Entity.class, new AABB(pos), Entity::isAttackable);
                    if (!entities.isEmpty()) {
                        Entity entity = entities.getFirst();
                        HashMultimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
                        stack.getAttributeModifiers().forEach(EquipmentSlot.MAINHAND, modifiers::put);
                        player.getAttributes().addTransientAttributeModifiers(modifiers);
                        player.attack(entity);
                        player.getAttributes().removeAttributeModifiers(modifiers);
                        return returnPlayerInventory(player);
                    } else {
                        return stack;
                    }
                }
            } else {
                cancelDestroyingBlock(player);

                // Send block right click event
                var blockRayTraceResult = new BlockHitResult(
                        Vec3.atCenterOf(pos).relative(direction, 0.5), direction, pos, false);
                PlayerInteractEvent.RightClickBlock rightClickBlockActionResult
                        = CommonHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, worldPosition, blockRayTraceResult);
                if (rightClickBlockActionResult.isCanceled()) {
                    return stack;
                }

                // Use item first
                if (rightClickBlockActionResult.getUseItem() != TriState.FALSE) {
                    if (!stack.isEmpty()) {
                        var itemUseContext = new UseOnContext(player, InteractionHand.MAIN_HAND, blockRayTraceResult);
                        var actionResult = stack.getItem().onItemUseFirst(stack, itemUseContext);
                        stack = itemUseContext.getItemInHand();
                        if (actionResult == InteractionResult.FAIL) {
                            return stack;
                        } else if (actionResult.consumesAction()) {
                            return returnPlayerInventory(player);
                        }
                        // Otherwise, PASS the logic
                    }
                }

                // Interact with entity
                var entities = level.getEntitiesOfClass(Entity.class, new AABB(pos));
                if (!entities.isEmpty()) {
                    var entity = entities.getFirst();
                    InteractionResult actionResult = player.interactOn(entity, InteractionHand.MAIN_HAND, entity.position());

                    // Remove simulated player again from villager, to avoid locked villagers.
                    if (entity instanceof Villager villager) {
                        villager.setTradingPlayer(null);
                    }

                    if (actionResult == InteractionResult.FAIL) {
                        return stack;
                    } else if (actionResult.consumesAction()) {
                        return returnPlayerInventory(player);
                    }
                }

                // Use itemstack
                if (rightClickBlockActionResult.getUseItem() != TriState.FALSE && !stack.isEmpty()) {
                    InteractionResult cancelResult = CommonHooks.onItemRightClick(player, InteractionHand.MAIN_HAND);
                    if (cancelResult != null)  {
                        if (cancelResult == InteractionResult.FAIL) {
                            return stack;
                        } else if (cancelResult.consumesAction()) {
                            return returnPlayerInventory(player);
                        }
                        // Otherwise, PASS the logic
                    } else {
                        ItemStack copyBeforeUse = stack.copy();
                        InteractionResult actionresult = stack.use(level, player, InteractionHand.MAIN_HAND);
                        if (actionresult == InteractionResult.FAIL) {
                            return stack;
                        }
                        if (actionresult instanceof InteractionResult.Success success) {
                            if (success.itemContext().heldItemTransformedTo().isEmpty()) {
                                player.setHeldItemSilent(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                                EventHooks.onPlayerDestroyItem(player, copyBeforeUse, InteractionHand.MAIN_HAND);
                            } else {
                                player.setHeldItemSilent(InteractionHand.MAIN_HAND, success.itemContext().heldItemTransformedTo());
                            }
                        }
                        if (actionresult.consumesAction()) {
                            // If the hand was activated, simulate the activated hand for a number of ticks, and deactivate.
                            if (player.isUsingItem()) {
                                player.updateActiveHandSimulated();
                                player.releaseUsingItem();
                            }
                            return returnPlayerInventory(player);
                        }
                    }
                }

                // Use item
                if (rightClickBlockActionResult.getUseItem() != TriState.FALSE && !stack.isEmpty()) {
                    // Increase reach position.
                    BlockPos targetPos = pos;
                    double reachDistance = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).getValue() + 3;
                    int i = 0;
                    while (i++ < reachDistance && level.isEmptyBlock(targetPos)) {
                        targetPos = targetPos.relative(direction.getOpposite());
                    }

                    UseOnContext itemUseContextReach = new UseOnContext(player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(targetPos).relative(direction, 0.5), direction, targetPos, false));
                    InteractionResult actionResult = stack.useOn(itemUseContextReach);
                    stack = itemUseContextReach.getItemInHand();
                    if (actionResult == InteractionResult.FAIL) {
                        return stack;
                    } else if (actionResult.consumesAction()) {
                        // If the hand was activated, simulate the activated hand for a number of ticks, and deactivate.
                        if (player.isUsingItem()) {
                            player.updateActiveHandSimulated();
                            player.releaseUsingItem();
                        }
                        return returnPlayerInventory(player);
                    }
                    // Otherwise, PASS the logic
                }
            }
        } else {
            cancelDestroyingBlock(player);
        }
        return stack;
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        input.readChild("itemHandler", itemHandler);
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("itemHandler", itemHandler);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide()) {
            cancelDestroyingBlock(extendedFakePlayer());
        }
    }
}
