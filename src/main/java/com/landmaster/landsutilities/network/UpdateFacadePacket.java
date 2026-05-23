package com.landmaster.landsutilities.network;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.Util;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record UpdateFacadePacket(BlockPos pos, BlockState state) implements CustomPacketPayload {
    public static final Type<UpdateFacadePacket> TYPE = new Type<>(Util.loc("update_facade"));

    public static final StreamCodec<ByteBuf, UpdateFacadePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateFacadePacket::pos,
            ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), UpdateFacadePacket::state,
            UpdateFacadePacket::new
    );

    public void handle(IPayloadContext context) {
        var level = context.player().level();
        if (level.isLoaded(pos)) {
            var chunk = level.getChunk(pos);
            if (state.isAir()) {
                chunk.getData(LandsUtilities.FACADE_STATES).remove(pos.asLong());
            } else {
                chunk.getData(LandsUtilities.FACADE_STATES).put(pos.asLong(), state);
            }
            Minecraft.getInstance().levelRenderer.setBlockDirty(pos, true);
        }
    }

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}