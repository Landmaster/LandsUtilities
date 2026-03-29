package com.landmaster.landsutilities.level;

import com.landmaster.landsutilities.LandsUtilities;
import com.landmaster.landsutilities.util.Util;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LandsUtilities.MODID)
public class RedstoneWandTickets extends SavedData {
    private static final Codec<Long2LongMap> POS_TO_TIME_CODEC = Codec.pair(Codec.LONG.fieldOf("pos").codec(), Codec.LONG.fieldOf("time").codec())
            .listOf().xmap(
                    list -> list.stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a, Long2LongOpenHashMap::new)),
                    map -> map.long2LongEntrySet().stream().map(pair -> Pair.of(pair.getLongKey(), pair.getLongValue())).toList()
            );

    public static final SavedDataType<RedstoneWandTickets> ID = new SavedDataType<>(
            Util.loc("redstone_wand_tickets"),
            RedstoneWandTickets::new,
            RecordCodecBuilder.create(instance -> instance.group(
                    POS_TO_TIME_CODEC.fieldOf("posToTime").forGetter(RedstoneWandTickets::posToTime)
            ).apply(instance, RedstoneWandTickets::new))
    );

    @Getter
    private final Long2LongMap posToTime;

    public RedstoneWandTickets() {
        this(new Long2LongOpenHashMap());
    }

    public RedstoneWandTickets(Long2LongMap posToTime) {
        this.posToTime = posToTime;
    }

    public void submitTicket(BlockPos pos, long time) {
        posToTime.put(pos.asLong(), time);
        setDirty();
    }

    public static RedstoneWandTickets getTickets(ServerLevel level) {
        var dataStorage = level.getDataStorage();
        return dataStorage.computeIfAbsent(ID);
    }

    @SubscribeEvent
    private static void tickLevel(LevelTickEvent.Pre event) {
        var level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            var tickets = getTickets(serverLevel);
            var it = tickets.posToTime.long2LongEntrySet().iterator();
            var pos = new BlockPos.MutableBlockPos();
            while (it.hasNext()) {
                var entry = it.next();
                if (level.getGameTime() >= entry.getLongValue()) {
                    pos.set(entry.getLongKey());
                    level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
                    it.remove();
                    tickets.setDirty();
                }
            }
        }
    }
}
