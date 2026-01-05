package com.nonolife;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class NoNoLifeSavedData extends SavedData {

    private Map<UUID, PlaytimeTracker> playerMap = new HashMap<>();

    private static final Codec<NoNoLifeSavedData> CODEC = Codec
            .unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), PlaytimeTracker.CODEC)
            .xmap(NoNoLifeSavedData::new, NoNoLifeSavedData::getPlayerMap);

    private static final SavedDataType<NoNoLifeSavedData> TYPE = new SavedDataType<>(
            "no_no_life_saved_data",
            NoNoLifeSavedData::new,
            CODEC,
            null);

    public NoNoLifeSavedData() {
    }

    public NoNoLifeSavedData(Map<UUID, PlaytimeTracker> playerMap) {
        this.playerMap = new HashMap<>(playerMap);
    }

    public static NoNoLifeSavedData get(MinecraftServer server) {
        ServerLevel level = server.getLevel(ServerLevel.OVERWORLD);

        if (level == null) {
            return new NoNoLifeSavedData();
        }
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public Map<UUID, PlaytimeTracker> getPlayerMap() {
        return playerMap;
    }

    public PlaytimeTracker playtimeTrackerForPlayer(UUID uuid) {
        setDirty();

        return playerMap.computeIfAbsent(uuid, k -> new PlaytimeTracker());
    }
}
