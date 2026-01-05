package com.nonolife;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlaytimeTracker {
    LocalDate firstJoin;
    long playtime;

    LocalDate currentWeek;
    long currentWeekPlaytime;

    public static final Codec<PlaytimeTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(string -> LocalDate.parse(string), date -> date.toString()).fieldOf("first_join")
                    .forGetter(PlaytimeTracker::getFirstJoin),
            Codec.LONG.fieldOf("playtime").forGetter(PlaytimeTracker::getPlaytime),
            Codec.STRING.xmap(string -> LocalDate.parse(string), date -> date.toString()).fieldOf("current_week")
                    .forGetter(PlaytimeTracker::getCurrentWeek),
            Codec.LONG.fieldOf("current_week_playtime").forGetter(PlaytimeTracker::getCurrentWeekPlaytime))
            .apply(instance,
                    PlaytimeTracker::new));

    public PlaytimeTracker() {
        firstJoin = LocalDate.now();
        playtime = 0;

        currentWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        currentWeekPlaytime = 0;
    }

    public PlaytimeTracker(LocalDate firstJoin, long playtime, LocalDate currentWeek, long currentWeekPlaytime) {
        this.firstJoin = firstJoin;
        this.playtime = playtime;

        this.currentWeek = currentWeek;
        this.currentWeekPlaytime = currentWeekPlaytime;
    }

    public long getPlaytime() {
        return playtime;
    }

    public LocalDate getFirstJoin() {
        return firstJoin;
    }

    public LocalDate getCurrentWeek() {
        return currentWeek;
    }

    public long getCurrentWeekPlaytime() {
        return currentWeekPlaytime;
    }

    public void addPlaytime(int seconds) {
        playtime += seconds;

        if (hasPlayedThisWeek()) {
            currentWeekPlaytime += seconds;
        } else {
            currentWeek = beginningOfCurrentWeek();
            currentWeekPlaytime = seconds;
        }
    }

    public long allowedPlaytime() {
        return daysSinceBeginningOfWeek() * 3600;
    }

    public long remainingPlaytime() {
        if (!hasPlayedThisWeek()) {
            return allowedPlaytime();
        }

        return allowedPlaytime() - currentWeekPlaytime;
    }

    private LocalDate beginningOfCurrentWeek() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    private boolean hasPlayedThisWeek() {
        return currentWeek.equals(beginningOfCurrentWeek());
    }

    private long daysSinceBeginningOfWeek() {
        LocalDate now = LocalDate.now();
        return ChronoUnit.DAYS.between(now.with(DayOfWeek.MONDAY), now) + 1;
    }

}
