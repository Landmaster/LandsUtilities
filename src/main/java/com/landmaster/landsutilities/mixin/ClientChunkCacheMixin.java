package com.landmaster.landsutilities.mixin;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
    @Unique
    private final ConcurrentMap<Long, LevelChunk> landsUtilities$extraChunks = new ConcurrentHashMap<>();

    @Shadow
    ClientChunkCache.Storage storage;

    @Shadow @Final
    ClientLevel level;

    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", at = @At("RETURN"), cancellable = true)
    private void getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk, CallbackInfoReturnable<LevelChunk> cir) {
        long chunkPos = ChunkPos.asLong(x, z);
        if (this.landsUtilities$extraChunks.containsKey(chunkPos)) {
            cir.setReturnValue(this.landsUtilities$extraChunks.get(chunkPos));
        }
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    private void replaceWithPacketData(int x, int z, FriendlyByteBuf buffer, CompoundTag tag, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> cir) {
        LevelChunk chunk = null;
        var chunkPos = new ChunkPos(x, z);
        if (!storage.inRange(x, z)) {
            chunk = new LevelChunk(level, chunkPos);
            landsUtilities$extraChunks.put(chunkPos.toLong(), chunk);
        }
        if (landsUtilities$extraChunks.containsKey(chunkPos.toLong())) {
            chunk = landsUtilities$extraChunks.get(chunkPos.toLong());
            chunk.replaceWithPacketData(buffer, tag, consumer);
        }
        if (chunk != null) {
            level.onChunkLoaded(chunkPos);
            NeoForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
            cir.setReturnValue(chunk);
        }
    }

    @Inject(method = "drop", at = @At("HEAD"))
    private void drop(ChunkPos chunkPos, CallbackInfo ci) {
        var chunk = landsUtilities$extraChunks.get(chunkPos.toLong());
        if (chunk != null) {
            NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
            landsUtilities$extraChunks.remove(chunkPos.toLong());
        }
    }

    @Inject(method = "replaceBiomes", at = @At("HEAD"), cancellable = true)
    private void replaceBiomes(int x, int z, FriendlyByteBuf buffer, CallbackInfo ci) {
        var chunk = landsUtilities$extraChunks.get(ChunkPos.asLong(x, z));
        if (chunk != null) {
            chunk.replaceBiomes(buffer);
            ci.cancel();
        }
    }
}
