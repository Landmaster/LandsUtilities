package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.block.entity.BaseBlockEntity;
import com.landmaster.landsutilities.util.ClientMenuUtil;
import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ModContainerMenu<T extends BaseBlockEntity> extends AbstractContainerMenu {
    @Getter
    @Nullable
    private final T blockEntity;

    protected ModContainerMenu(@Nullable MenuType<?> menuType, int containerId, T blockEntity) {
        super(menuType, containerId);
        this.blockEntity = blockEntity;
    }

    @FunctionalInterface
    public interface Factory<T extends BaseBlockEntity, U extends ModContainerMenu<T>> {
        U create(int containerId, Inventory inventory, T blockEntity);
    }

    public static <T extends BaseBlockEntity, U extends ModContainerMenu<T>> MenuType<U> createMenuType(ModContainerMenu.Factory<T, U> ctor, BlockEntityType<T> teType) {
        return IMenuTypeExtension.create((containerId, inventory, byteBuf)
                -> ctor.create(containerId, inventory, ClientMenuUtil.getBlockEntity(byteBuf.readBlockPos(), teType)));
    }


    protected void initInventory(Inventory inventory, int offset) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, offset + i * 18));
            }
        }

        for (int i=0; i<9; ++i) {
            addSlot(new Slot(inventory, i, 8 + i * 18, offset + 58));
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return blockEntity != null && Container.stillValidBlockEntity(blockEntity, player);
    }
}
