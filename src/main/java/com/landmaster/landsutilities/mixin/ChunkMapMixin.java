package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Arrays;
import java.util.function.Consumer;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @Inject(method = "isChunkTracked(Lnet/minecraft/server/level/ServerPlayer;II)Z", at = @At("HEAD"), cancellable = true)
    private void isChunkTracked(ServerPlayer player, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (Arrays.stream(InteractionHand.values())
                .map(player::getItemInHand)
                .flatMap(stack -> RemoteControlItem.linked(stack).stream())
                .anyMatch(link -> ChunkPos.asLong(link.pos()) == ChunkPos.asLong(x, z) && link.dimension() == player.level().dimension())) {
            cir.setReturnValue(true);
        }
    }

    @ModifyArgs(method = "applyChunkTrackingView", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkTrackingView;difference(Lnet/minecraft/server/level/ChunkTrackingView;Lnet/minecraft/server/level/ChunkTrackingView;Ljava/util/function/Consumer;Ljava/util/function/Consumer;)V"))
    private void applyChunkTrackingView(Args args, ServerPlayer player, ChunkTrackingView chunkTrackingView) {
        Consumer<ChunkPos> oldDropFn = args.get(3);
        args.set(3, (Consumer<ChunkPos>) (ChunkPos chunkPos) -> {
            if (Arrays.stream(InteractionHand.values())
                    .map(player::getItemInHand)
                    .flatMap(stack -> RemoteControlItem.linked(stack).stream())
                    .noneMatch(link -> ChunkPos.asLong(link.pos()) == chunkPos.toLong() && link.dimension() == player.level().dimension())) {
                oldDropFn.accept(chunkPos);
            }
        });
    }
}
