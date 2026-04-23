package com.landmaster.landsutilities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_LINKED_BLOCKS = BUILDER
            .comment("The maximum number of blocks that can be linked to a remote control at base. Can be increased by the Remote Control Capacity enchantment.")
            .defineInRange("maxLinkedBlocks", 5, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> REMOTE_CONTROL_BLACKLIST = BUILDER
            .comment("Block IDs of blocks that are blacklisted from working with the Remote Control")
            .defineList(
                    List.of("remoteControlBlacklist"),
                    List::of,
                    () -> "",
                    str -> BuiltInRegistries.BLOCK.containsKey(Identifier.parse(Objects.toString(str))),
                    null
            );

    public static final ModConfigSpec.IntValue XP_COLLECTOR_RADIUS = BUILDER
            .comment("Radius in blocks of XP Collector")
            .defineInRange("xpCollectorRadius", 3, 1, 200);
    public static final ModConfigSpec.IntValue XP_COLLECTOR_MAX_OFFSET = BUILDER
            .comment("Maximum offset in blocks of XP Collector")
            .defineInRange("xpCollectorMaxOffset", 5, 1, 200);
    public static final ModConfigSpec.IntValue XP_COLLECTOR_TICK_RATE = BUILDER
            .comment("How many ticks to wait between XP collection for the XP Collector")
            .defineInRange("xpCollectorTickCount", 20, 1, 200);

    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> XP_INTERFACE_STORAGE = BUILDER
            .comment("XP interface capacity in mB. First element is base amount, subsequent elements are in increasing order of capacity upgrade level.")
            .defineList(
                    "xpInterfaceCapacity",
                    List.of(1000000, 10000000, 100000000, 1000000000),
                    () -> 1000000,
                    val -> val instanceof Integer intVal && intVal > 0
            );

    public static final ModConfigSpec.IntValue BLOCK_INTERFACER_MAX_OFFSET = BUILDER
            .comment("Maximum offset in blocks of Block Interfacer")
            .defineInRange("blockInterfacerMaxOffset", 5, 1, 200);

    public static int levelToValue(ModConfigSpec.ConfigValue<List<? extends Integer>> cfg, int level) {
        var list = cfg.get();
        return list.get(Math.clamp(level, 0, list.size() - 1));
    }

    public static boolean xpCollectorOffsetInRange(byte offset) {
        return offset >= -XP_COLLECTOR_MAX_OFFSET.get() && offset <= XP_COLLECTOR_MAX_OFFSET.get();
    }

    public static boolean interfacerOffsetInRange(byte offset) {
        return offset >= -BLOCK_INTERFACER_MAX_OFFSET.get() && offset <= BLOCK_INTERFACER_MAX_OFFSET.get();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    private static Set<String> remoteControlBlacklist;

    public static boolean isRemoteBlacklisted(Block block) {
        return remoteControlBlacklist.contains(BuiltInRegistries.BLOCK.getKey(block).toString());
    }

    private static void reloadValueCache() {
        remoteControlBlacklist = Set.copyOf(REMOTE_CONTROL_BLACKLIST.get());
    }

    @SubscribeEvent
    private static void onConfigLoad(ModConfigEvent.Loading event) {
        reloadValueCache();
    }

    @SubscribeEvent
    private static void onConfigReload(ModConfigEvent.Reloading event) {
        reloadValueCache();
    }
}
