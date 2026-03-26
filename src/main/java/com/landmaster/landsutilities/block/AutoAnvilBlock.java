package com.landmaster.landsutilities.block;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.AutoAnvilBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;

import java.util.List;

import net.minecraft.world.Containers;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class AutoAnvilBlock extends FunctionalBlock<AutoAnvilBlockEntity> {
    private static final MapCodec<AutoAnvilBlock> CODEC = simpleCodec(AutoAnvilBlock::new);

    public AutoAnvilBlock(Properties properties) {
        super(properties, LandsUtilities.AUTO_ANVIL_TE);
        this.registerDefaultState(this.stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected void onRemove(BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            var blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AutoAnvilBlockEntity autoAnvil) {
                var handler = autoAnvil.automationItemHandler();
                for (int i = 0; i < handler.getSlots(); i++) {
                    var stack = handler.getStackInSlot(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.translatable("tooltip.landsutilities.auto_anvil").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    @Nonnull
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    protected boolean isPathfindable(@Nonnull BlockState state, @Nonnull PathComputationType pathComputationType) {
        return false;
    }

    @Nonnull
    @Override
    protected VoxelShape getShape(BlockState state, @Nonnull BlockGetter level, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        return state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X ? AnvilBlock.X_AXIS_AABB : AnvilBlock.Z_AXIS_AABB;
    }

    @Nonnull
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
