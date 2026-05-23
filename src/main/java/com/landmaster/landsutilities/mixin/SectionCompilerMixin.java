package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.LandsUtilities;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Shadow
    protected abstract BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> bufferLayers, SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType);

    @WrapOperation(
            method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    )
    private BlockState getBlockStateToRender(RenderChunkRegion instance, BlockPos pos, Operation<BlockState> original,
                                             @Local(name = "visgraph") VisGraph visgraph,
                                             @Local(name = "map") Map<RenderType, BufferBuilder> map,
                                             @Local(name = "sectionBufferBuilderPack") SectionBufferBuilderPack sectionBufferBuilderPack,
                                             @Local(name = "randomsource") RandomSource randomsource,
                                             @Local(name = "posestack") PoseStack posestack) {
        var chunk = Minecraft.getInstance().level.getChunk(pos);
        var facadeToRender = chunk.getData(LandsUtilities.FACADE_STATES).get(pos.asLong());
        if (facadeToRender != null && facadeToRender.getRenderShape() == RenderShape.MODEL && !facadeToRender.isAir()) {
            if (facadeToRender.isSolidRender(instance, pos)) {
                visgraph.setOpaque(pos);
            }

            FluidState fluidstate = facadeToRender.getFluidState();
            if (!fluidstate.isEmpty()) {
                RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
                BufferBuilder bufferbuilder = this.getOrBeginLayer(map, sectionBufferBuilderPack, rendertype);
                this.blockRenderer.renderLiquid(pos, instance, bufferbuilder, facadeToRender, fluidstate);
            }

            if (facadeToRender.getRenderShape() == RenderShape.MODEL) {
                BakedModel model = this.blockRenderer.getBlockModel(facadeToRender);
                ModelData modelData = instance.getModelData(pos);
                modelData = model.getModelData(instance, pos, facadeToRender, modelData);
                randomsource.setSeed(facadeToRender.getSeed(pos));

                for(RenderType rendertype2 : model.getRenderTypes(facadeToRender, randomsource, modelData)) {
                    BufferBuilder bufferbuilder1 = this.getOrBeginLayer(map, sectionBufferBuilderPack, rendertype2);
                    posestack.pushPose();
                    posestack.translate(
                            (float)SectionPos.sectionRelative(pos.getX()),
                            (float)SectionPos.sectionRelative(pos.getY()),
                            (float)SectionPos.sectionRelative(pos.getZ())
                    );
                    this.blockRenderer.renderBatched(facadeToRender, pos, instance, posestack, bufferbuilder1, true, randomsource, modelData, rendertype2);
                    posestack.popPose();
                }
            }

            return Blocks.AIR.defaultBlockState();
        }
        return original.call(instance, pos);
    }
}
