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

    public static boolean offsetInRange(byte offset) {
        return offset >= -XP_COLLECTOR_MAX_OFFSET.get() && offset <= XP_COLLECTOR_MAX_OFFSET.get();
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
