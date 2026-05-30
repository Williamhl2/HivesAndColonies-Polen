package com.hivesandcolonies.polen.progression;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PolenChapterManager {

    public static final int PROLOGUE = 0;
    public static final int FOUNDATION = 1;
    public static final int FIRST_FRIENDS = 2;
    public static final int SOURCE = 3;
    public static final int KINGDOM_ECHOES = 4;
    public static final int BEEKEEPING_INDUSTRY = 5;
    public static final int VOICES_OF_THE_PAST = 6;
    public static final int LOGISTICS = 7;
    public static final int DISTANT_PATHS = 8;
    public static final int SEARCHING_FOR_ANSWERS = 9;
    public static final int ARCANE_INDUSTRY = 10;
    public static final int LOST_COUNCIL = 11;
    public static final int LOST_KINGDOM = 12;
    public static final int THE_HEIR = 13;
    public static final int CORONATION = 14;
    public static final int NEW_BEGINNING = 15;

    public static final int TOTAL_CHAPTERS = NEW_BEGINNING + 1;

    private static final Map<UUID, Integer> CHAPTERS = new HashMap<>();

    private PolenChapterManager() {}

    public static int getMaxChapterIndex() {
        return NEW_BEGINNING;
    }

    public static int getCurrentChapter(Player player) {
        return CHAPTERS.getOrDefault(player.getUUID(), PROLOGUE);
    }

    public static void setCurrentChapter(Player player, int chapter) {
        CHAPTERS.put(player.getUUID(), clampChapter(chapter));
    }

    public static void advanceToChapter(Player player, int chapter) {
        int current = getCurrentChapter(player);

        if (chapter > current) {
            setCurrentChapter(player, chapter);
        }
    }

    public static void resetChapter(Player player) {
        CHAPTERS.remove(player.getUUID());
    }

    private static int clampChapter(int chapter) {
        return Math.max(PROLOGUE, Math.min(NEW_BEGINNING, chapter));
    }
}