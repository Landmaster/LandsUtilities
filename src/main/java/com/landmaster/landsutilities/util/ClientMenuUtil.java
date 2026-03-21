package com.landmaster.landsutilities.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Optional;

public class ClientMenuUtil {
    public static  <T extends BlockEntity> T getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        return Minecraft.getInstance().level.getBlockEntity(pos, type).orElse(null);
    }
}
