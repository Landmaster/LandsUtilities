package com.landmaster.landsutilities.mixin;

import com.landmaster.landsutilities.LandsUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin {
    @Inject(method = "getSignal", at = @At("HEAD"), cancellable = true)
    private void injectGetSignal(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        if (level instanceof LevelReader levelReader) {
            var chunk = levelReader.getChunk(pos);
            var onBlocks = chunk.getData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
            if (onBlocks.contains(pos.asLong())) {
                cir.setReturnValue(15);
            }
        }
    }

    @Inject(method = "getDirectSignal", at = @At("HEAD"), cancellable = true)
    private void injectGetDirectSignal(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        if (level instanceof LevelReader levelReader) {
            var chunk = levelReader.getChunk(pos);
            var onBlocks = chunk.getData(LandsUtilities.REDSTONE_WAND_ON_BLOCKS);
            if (onBlocks.contains(pos.asLong())) {
                cir.setReturnValue(15);
            }
        }
    }
}
