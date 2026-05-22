package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.LandsUtilities;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.*;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin {
    @Shadow
    @Final
    private BlockStateModelSet blockModelSet;

    @Shadow
    @Final
    private FluidStateModelSet fluidModelSet;

    @Shadow
    @Final
    private boolean cutoutLeaves;

    @WrapOperation(
            method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState getBlockStateToRender(RenderSectionRegion instance, BlockPos pos, Operation<BlockState> original,
                                             @Local(name = "visGraph") VisGraph visGraph,
                                             @Local(name = "fluidRenderer") FluidRenderer fluidRenderer,
                                             @Local(argsOnly = true, name = "region") RenderSectionRegion region,
                                             @Local(name = "fluidOutput") FluidRenderer.Output fluidOutput,
                                             @Local(name = "blockRenderer") ModelBlockRenderer blockRenderer,
                                             @Local(name = "opaqueQuadOutput") BlockQuadOutput opaqueQuadOutput,
                                             @Local(name = "quadOutput") BlockQuadOutput quadOutput) {
        var chunkPos = ChunkPos.containing(pos);
        var chunk = Minecraft.getInstance().level.getChunk(chunkPos.x(), chunkPos.z());
        var facadeToRender = chunk.getData(LandsUtilities.FACADE_STATES).get(pos.asLong());
        if (facadeToRender != null && facadeToRender.getRenderShape() == RenderShape.MODEL && !facadeToRender.isAir()) {
            if (facadeToRender.isSolidRender()) {
                visGraph.setOpaque(pos);
            }

            FluidState fluidState = facadeToRender.getFluidState();
            if (!fluidState.isEmpty()) {
                var customRenderer = this.fluidModelSet.get(fluidState).customRenderer();
                if (customRenderer == null || !customRenderer.renderFluid(fluidRenderer, fluidState, region, pos, fluidOutput, facadeToRender))
                    fluidRenderer.tesselate(region, pos, fluidOutput, facadeToRender, fluidState);
            }

            if (facadeToRender.getRenderShape() == RenderShape.MODEL) {
                blockRenderer.tesselateBlock(
                        ModelBlockRenderer.forceOpaque(this.cutoutLeaves, facadeToRender) ? opaqueQuadOutput : quadOutput,
                        SectionPos.sectionRelative(pos.getX()),
                        SectionPos.sectionRelative(pos.getY()),
                        SectionPos.sectionRelative(pos.getZ()),
                        region,
                        pos,
                        facadeToRender,
                        this.blockModelSet.get(facadeToRender),
                        facadeToRender.getSeed(pos)
                );
            }
            return Blocks.AIR.defaultBlockState();
        }
        return original.call(instance, pos);
    }
}
