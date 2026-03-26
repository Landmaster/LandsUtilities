package com.landmaster.landsutilities;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_LINKED_BLOCKS = BUILDER
            .comment("The maximum number of blocks that can be linked to a remote control at base. Can be increased by the Remote Control Capacity enchantment.")
            .defineInRange("maxLinkedBlocks", 5, 1, Integer.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}
