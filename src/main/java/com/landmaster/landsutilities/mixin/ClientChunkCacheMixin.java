package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.item.RemoteControlItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {
    @Unique
    private final ConcurrentMap<Long, LevelChunk> landsUtilities$extraChunks = new ConcurrentHashMap<>();

    @Shadow
    private ClientChunkCache.Storage storage;

    @Shadow @Final
    private ClientLevel level;

    @Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/LevelChunk;", at = @At("TAIL"), cancellable = true)
    private void getChunk(int x, int z, ChunkStatus targetStatus, boolean loadOrGenerate, CallbackInfoReturnable<LevelChunk> cir) {
        if (!storage.inRange(x, z)) {
            long chunkPos = ChunkPos.pack(x, z);
            var chunk = landsUtilities$extraChunks.get(chunkPos);
            if (chunk != null) {
                cir.setReturnValue(chunk);
            }
        }
    }

    @Inject(method = "replaceWithPacketData", at = @At("HEAD"), cancellable = true)
    private void replaceWithPacketData(int chunkX, int chunkZ, FriendlyByteBuf readBuffer, Map<Heightmap.Types, long[]> heightmaps, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> blockEntities, CallbackInfoReturnable<LevelChunk> cir) {
        var chunkPos = new ChunkPos(chunkX, chunkZ);
        var chunk = landsUtilities$extraChunks.computeIfAbsent(chunkPos.pack(), k ->
            RemoteControlItem.activeLinks(Minecraft.getInstance().player)
                    .anyMatch(link -> ChunkPos.pack(link.pos()) == chunkPos.pack()) ? new LevelChunk(level, chunkPos) : null
        );
        if (chunk != null) {
            chunk.replaceWithPacketData(new FriendlyByteBuf(readBuffer.copy()), heightmaps, blockEntities);
            level.onChunkLoaded(chunkPos);
            NeoForge.EVENT_BUS.post(new ChunkEvent.Load(chunk, false));
            if (!storage.inRange(chunkX, chunkZ)) {
                cir.setReturnValue(chunk);
            }
        }
    }

    @Inject(method = "drop", at = @At("HEAD"))
    private void drop(ChunkPos pos, CallbackInfo ci) {
        var chunk = landsUtilities$extraChunks.remove(pos.pack());
        if (chunk != null) {
            NeoForge.EVENT_BUS.post(new ChunkEvent.Unload(chunk));
        }
    }

    @Inject(method = "replaceBiomes", at = @At("HEAD"), cancellable = true)
    private void replaceBiomes(int chunkX, int chunkZ, FriendlyByteBuf readBuffer, CallbackInfo ci) {
        var chunk = landsUtilities$extraChunks.get(ChunkPos.pack(chunkX, chunkZ));
        if (chunk != null) {
            chunk.replaceBiomes(readBuffer);
            ci.cancel();
        }
    }
}
