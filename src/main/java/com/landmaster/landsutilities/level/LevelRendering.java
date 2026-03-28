package com.landmaster.landsutilities.level;

import com.landmaster.landsutilities.LandsUtilities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = LandsUtilities.MODID)
public class LevelRendering {
    @SubscribeEvent
    private static void onRenderLevel(RenderLevelStageEvent.AfterTranslucentBlocks event) {
        var level = Minecraft.getInstance().level;
        var player = Minecraft.getInstance().player;
        if (player.getMainHandItem().is(LandsUtilities.REDSTONE_WAND)
                || player.getOffhandItem().is(LandsUtilities.REDSTONE_WAND)) {
            var originChunkPos = player.chunkPosition();
            var cursor = new BlockPos.MutableBlockPos();
            var vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypes.LINES);
            var cameraPos = event.getLevelRenderState().cameraRenderState.pos;
            for (int i = -2; i <= 2; ++i) {
                for (int j = -2; j <= 2; ++j) {
                    var chunk = level.getChunk(originChunkPos.x() + i, originChunkPos.z() + j);
                    chunk.getData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS).forEach((pos, val) -> {
                        var color = val.type().getColor();
                        cursor.set(pos);
                        renderShape(event.getPoseStack(), vertexConsumer, Shapes.block(),
                                cursor.getX() - cameraPos.x, cursor.getY() - cameraPos.y, cursor.getZ() - cameraPos.z,
                                ARGB.red(color) / 256.0f, ARGB.green(color) / 256.0f,
                                ARGB.blue(color) / 256.0f, ARGB.alpha(color) / 256.0f);
                    });
                }
            }
        }
    }

    private static void renderShape(
            PoseStack poseStack,
            VertexConsumer consumer,
            VoxelShape shape,
            double x,
            double y,
            double z,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        PoseStack.Pose posestack$pose = poseStack.last();
        shape.forAllEdges(
                (p_323073_, p_323074_, p_323075_, p_323076_, p_323077_, p_323078_) -> {
                    float f = (float)(p_323076_ - p_323073_);
                    float f1 = (float)(p_323077_ - p_323074_);
                    float f2 = (float)(p_323078_ - p_323075_);
                    float f3 = Mth.sqrt(f * f + f1 * f1 + f2 * f2);
                    f /= f3;
                    f1 /= f3;
                    f2 /= f3;
                    consumer.addVertex(posestack$pose, (float)(p_323073_ + x), (float)(p_323074_ + y), (float)(p_323075_ + z))
                            .setColor(red, green, blue, alpha)
                            .setNormal(posestack$pose, f, f1, f2)
                            .setLineWidth(1.0f);
                    consumer.addVertex(posestack$pose, (float)(p_323076_ + x), (float)(p_323077_ + y), (float)(p_323078_ + z))
                            .setColor(red, green, blue, alpha)
                            .setNormal(posestack$pose, f, f1, f2)
                            .setLineWidth(1.0f);
                }
        );
    }
}
