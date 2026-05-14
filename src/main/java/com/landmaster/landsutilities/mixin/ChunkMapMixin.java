package com.landmaster.landsutilities.mixin;

import com.google.common.collect.MapMaker;
import com.landmaster.landsutilities.item.RemoteControlItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {
    @Shadow
    protected abstract void markChunkPendingToSend(ServerPlayer player, ChunkPos pos);

    @Shadow
    private static void dropChunk(ServerPlayer player, ChunkPos pos) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "isChunkTracked(Lnet/minecraft/server/level/ServerPlayer;II)Z", at = @At("HEAD"), cancellable = true)
    private void isChunkTracked(ServerPlayer player, int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (RemoteControlItem.activeLinks(player)
                .anyMatch(link -> ChunkPos.pack(link.pos()) == ChunkPos.pack(chunkX, chunkZ))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "dropChunk", at = @At("HEAD"), cancellable = true)
    private static void dropChunk(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        if (RemoteControlItem.activeLinks(player)
                .anyMatch(link -> ChunkPos.pack(link.pos()) == pos.pack())) {
            ci.cancel();
        }
    }

    @Unique
    private final ConcurrentMap<ServerPlayer, Set<ChunkPos>> landsutilities$remoteLinks = new MapMaker().weakKeys().makeMap();

    @Inject(method = "updateChunkTracking", at = @At("HEAD"))
    private void updateChunkTracking(ServerPlayer player, CallbackInfo ci) {
        var oldRemoteLinks = landsutilities$remoteLinks.getOrDefault(player, Set.of());
        var newRemoteLinks = RemoteControlItem.activeLinks(player)
                .map(link -> ChunkPos.containing(link.pos()))
                .collect(Collectors.toSet());
        for (var newLink: newRemoteLinks) {
            if (!oldRemoteLinks.contains(newLink)) {
                markChunkPendingToSend(player, newLink);
            }
        }
        for (var oldLink: oldRemoteLinks) {
            if (!newRemoteLinks.contains(oldLink) && !player.getChunkTrackingView().contains(oldLink)) {
                dropChunk(player, oldLink);
            }
        }
        landsutilities$remoteLinks.put(player, newRemoteLinks);
    }

    @Inject(method = "onChunkReadyToSend", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getChunkTrackingView()Lnet/minecraft/server/level/ChunkTrackingView;"))
    private void onChunkReadyToSend(ChunkHolder chunkHolder, LevelChunk chunk, CallbackInfo ci, @Local(name = "player") ServerPlayer player) {
        if (RemoteControlItem.activeLinks(player)
                .anyMatch(link -> ChunkPos.pack(link.pos()) == chunk.getPos().pack())) {
            markChunkPendingToSend(player, chunk.getPos());
        }
    }
}
