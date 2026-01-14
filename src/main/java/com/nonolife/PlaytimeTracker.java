package com.nonolife;

import java.time.LocalDate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlaytimeTracker {
    private LocalDate firstJoin;
    private int totalPlaytime;
    private int availablePlaytime;

    boolean showBossBar;

    public static final Codec<PlaytimeTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(string -> LocalDate.parse(string), date -> date.toString()).fieldOf("first_join")
                    .forGetter(PlaytimeTracker::getFirstJoin),
            Codec.INT.fieldOf("total_playtime").forGetter(PlaytimeTracker::getTotalPlaytime),
            Codec.INT.fieldOf("available_playtime").forGetter(PlaytimeTracker::getAvailablePlaytime),
            Codec.BOOL.fieldOf("show_boss_bar").forGetter(PlaytimeTracker::isShowBossBar))
            .apply(instance,
                    PlaytimeTracker::new));

    public PlaytimeTracker(int availablePlaytime) {
        firstJoin = LocalDate.now();
        totalPlaytime = 0;

        this.availablePlaytime = availablePlaytime;
        showBossBar = true;
    }

    public PlaytimeTracker(LocalDate firstJoin, int totalPlaytime, int availablePlaytime, boolean showBossBar) {
        this.firstJoin = firstJoin;
        this.totalPlaytime = totalPlaytime;

        this.availablePlaytime = availablePlaytime;
        this.showBossBar = showBossBar;
    }

    public int getTotalPlaytime() {
        return totalPlaytime;
    }

    public LocalDate getFirstJoin() {
        return firstJoin;
    }

    public int getAvailablePlaytime() {
        return availablePlaytime;
    }

    public boolean isShowBossBar() {
        return showBossBar;
    }

    public void addPlaytime(int seconds, int maximumPlaytime) {
        availablePlaytime = Math.min(availablePlaytime + seconds, maximumPlaytime);
    }

    public void playedFor(int seconds) {
        totalPlaytime += seconds;
        availablePlaytime -= seconds;
    }

}
