package com.landmaster.landsutilities.menu;

import com.landmaster.landsutilities.util.UpgradeItemHandler;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

import java.util.ArrayList;
import java.util.List;

public class UpgradeHandlerSlot extends ResourceHandlerSlot {
    public UpgradeHandlerSlot(UpgradeItemHandler handler, int index, int xPosition, int yPosition) {
        super(handler, handler::set, index, xPosition, yPosition);
    }

    public List<Component> tooltip() {
        var result = new ArrayList<Component>();
        result.add(Component.translatable("gui.landsutilities.supported_upgrades"));
        for (var type: ((UpgradeItemHandler) getResourceHandler()).validTypes()) {
            result.add(Component.translatable("upgrade.landsutilities." + type));
        }
        return result;
    }
}
