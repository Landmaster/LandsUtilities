package com.landmaster.landsutilities.util;

import com.landmaster.landsutilities.LandsUtilities;
import lombok.Getter;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.Set;

public class UpgradeItemHandler extends ItemStacksResourceHandler {
    @Getter
    private final Set<String> validTypes;

    public UpgradeItemHandler(int size, String...validTypes) {
        super(size);
        this.validTypes = Set.of(validTypes);
    }

    public int getUpgradeLevel(String type) {
        return stacks.stream()
                .map(stack -> stack.get(LandsUtilities.UPGRADE_INFO))
                .filter(upgradeInfo -> upgradeInfo != null && upgradeInfo.type().equals(type))
                .mapToInt(UpgradeInfo::level)
                .max()
                .orElse(0);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        var upgradeInfo = resource.get(LandsUtilities.UPGRADE_INFO);
        if (upgradeInfo == null || !validTypes.contains(upgradeInfo.type())) {
            return false;
        }
        return getUpgradeLevel(upgradeInfo.type()) == 0;
    }
}
