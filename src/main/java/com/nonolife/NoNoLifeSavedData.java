package com.nonolife;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class NoNoLifeSavedData extends SavedData {
    private LocalDate lastPlaytimeAddition;

    private Map<UUID, PlaytimeTracker> playerMap;

    private static final Codec<LocalDate> LOCAL_DATE_CODEC = Codec.STRING.xmap(LocalDate::parse, LocalDate::toString);
    private static final Codec<Map<UUID, PlaytimeTracker>> PLAYER_MAP_CODEC = Codec
            .unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), PlaytimeTracker.CODEC);

    public static final Codec<NoNoLifeSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PLAYER_MAP_CODEC.fieldOf("playerMap").forGetter(NoNoLifeSavedData::getPlayerMap),
            LOCAL_DATE_CODEC.fieldOf("lastPlaytimeAddition").forGetter(NoNoLifeSavedData::getLastPlaytimeAddition))
            .apply(instance, NoNoLifeSavedData::new));

    private static final SavedDataType<NoNoLifeSavedData> TYPE = new SavedDataType<>(
            "no_no_life_saved_data",
            NoNoLifeSavedData::new,
            CODEC,
            null);

    public NoNoLifeSavedData() {
        lastPlaytimeAddition = LocalDate.now();
        playerMap = new HashMap<>();
    }

    public NoNoLifeSavedData(Map<UUID, PlaytimeTracker> playerMap, LocalDate lastPlaytimeAddition) {
        this.playerMap = new HashMap<>(playerMap);
        this.lastPlaytimeAddition = lastPlaytimeAddition;
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

    public LocalDate getLastPlaytimeAddition() {
        return lastPlaytimeAddition;
    }

    public void setLastPlaytimeAddition(LocalDate lastPlaytimeAddition) {
        this.lastPlaytimeAddition = lastPlaytimeAddition;
    }

    public PlaytimeTracker getOrCreatePlaytimeTrackerForPlayer(UUID uuid, int initialPlaytime) {
        setDirty();
        return playerMap.computeIfAbsent(uuid, k -> new PlaytimeTracker(initialPlaytime));
    }

    public PlaytimeTracker getPlaytimeTrackerForPlayer(UUID uuid) {
        return playerMap.get(uuid);
    }

}
