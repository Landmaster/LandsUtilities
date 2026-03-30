package com.landmaster.landsutilities.block;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.block.entity.XPInterfaceBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;

import javax.annotation.Nonnull;

public class XPInterfaceBlock extends FunctionalBlock<XPInterfaceBlockEntity> {
    private static final MapCodec<XPInterfaceBlock> CODEC = simpleCodec(XPInterfaceBlock::new);

    public XPInterfaceBlock(Properties properties) {
        super(properties, LandsUtilities.XP_INTERFACE_TE);
    }

    @Override
    @Nonnull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
