package com.landmaster.landsutilities.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Adapted from IntegratedTunnels ExtendedFakePlayer
 */
public class ExtendedFakePlayer extends FakePlayer {

    private static final GameProfile PROFILE = new GameProfile(UUID.fromString("22232a03-1a7a-447d-975c-45e638cce840"), "lands_utilities_interfacer");

    private long lastUpdateTick = 0;
    private long lastSwingUpdateTick = 0;
    private int ticksSinceLastTick = 0;

    public ExtendedFakePlayer(ServerLevel world) {
        super(world, PROFILE);
        this.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        this.connection = new FakeNetHandlerPlayServer(world.getServer(), this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean canBeAffected(@Nonnull MobEffectInstance potioneffectIn) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        Level level = this.level();
        int toTick = (int) (level.getGameTime() - this.lastUpdateTick);
        if (toTick > 0) {
            this.ticksSinceLastTick = toTick;
        }
        this.lastUpdateTick = level.getGameTime();

        this.attackStrengthTicker = (int) (level.getGameTime() - lastSwingUpdateTick);
        this.getInventory().tick();
        this.getCooldowns().tick();
    }

    @Override
    public void resetAttackStrengthTicker() {
        super.resetAttackStrengthTicker();
        lastSwingUpdateTick = level().getGameTime();
    }

    public void updateActiveHandSimulated() {
        if (this.isUsingItem()) {
            for (int i = 0; i < this.ticksSinceLastTick; i++) {
                if (this.isUsingItem()) {
                    ItemStack itemstack = this.getItemInHand(this.getUsedItemHand());
                    if (CommonHooks.canContinueUsing(this.useItem, itemstack)) {
                        this.useItem = itemstack;
                    }
                    // Based on LivingEntity#updateActiveHand
                    if (itemstack == this.useItem) {
                        if (!this.useItem.isEmpty()) {
                            useItemRemaining = EventHooks.onItemUseTick(this, useItem, useItemRemaining);
                            if (useItemRemaining > 0)
                                useItem.getItem().onUseTick(this.level(), this, useItem, useItemRemaining);
                        }

                        if (--this.useItemRemaining <= 0 && !this.level().isClientSide() && !this.useItem.useOnRelease()) {
                            this.completeUsingItem();
                            break;
                        }
                    } else {
                        this.stopUsingItem();
                        break;
                    }
                }
            }
        } else {
            this.stopUsingItem();
        }
    }

    @Override
    public void startSleeping(@Nonnull BlockPos blockPos) {
        // Do nothing
    }

    public void setPlayerState(InteractionHand hand, BlockPos pos, Direction side, boolean sneaking) {
        setPos(Vec3.atCenterOf(pos));
        xo = getX();
        yo = getY();
        zo = getZ();
        setYRot(side.getOpposite().toYRot());
        setXRot(side == Direction.UP ? 90F : (side == Direction.DOWN ? -90F : 0F));
        eyeHeight = 0F;
        setShiftKeyDown(sneaking);
        setHeldItemSilent(hand, ItemStack.EMPTY);
        tick();
        setOnGround(true);
    }

    public void setHeldItemSilent(InteractionHand hand, ItemStack itemStack) {
        if (hand == InteractionHand.MAIN_HAND) {
            getInventory().setSelectedItem(itemStack);
        } else if (hand == InteractionHand.OFF_HAND) {
            getInventory().setItem(Inventory.SLOT_OFFHAND, itemStack);
        } else {
            // Could happen if some mod messes with the hand types.
            setItemInHand(hand, itemStack);
        }
    }
}
