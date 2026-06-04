package com.hivesandcolonies.characters.progression;

import com.hivesandcolonies.characters.progression.world.PolenWorldStorySavedData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

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

    private PolenChapterManager() {}

    public static int getMaxChapterIndex() {
        return NEW_BEGINNING;
    }

    public static int getCurrentChapter(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return PROLOGUE;
        }

        return getCurrentChapter(serverLevel);
    }

    public static int getCurrentChapter(ServerLevel level) {
        return PolenWorldStorySavedData.get(level).getData().getCurrentChapter();
    }

    public static void setCurrentChapter(Player player, int chapter) {
        if (player.level() instanceof ServerLevel serverLevel) {
            setCurrentChapter(serverLevel, chapter);
        }
    }

    public static void setCurrentChapter(ServerLevel level, int chapter) {
        PolenWorldStorySavedData savedData = PolenWorldStorySavedData.get(level);
        int clampedChapter = clampChapter(chapter);

        if (savedData.getData().getCurrentChapter() != clampedChapter) {
            savedData.getData().setCurrentChapter(clampedChapter);
            savedData.setDirty();
        }
    }

    public static void advanceToChapter(Player player, int chapter) {
        if (player.level() instanceof ServerLevel serverLevel) {
            advanceToChapter(serverLevel, chapter);
        }
    }

    public static void advanceToChapter(ServerLevel level, int chapter) {
        int current = getCurrentChapter(level);
        if (chapter > current) {
            setCurrentChapter(level, chapter);
        }
    }

    public static void resetChapter(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            resetChapter(serverLevel);
        }
    }

    public static void resetChapter(ServerLevel level) {
        setCurrentChapter(level, PROLOGUE);
    }

    private static int clampChapter(int chapter) {
        return Math.max(PROLOGUE, Math.min(NEW_BEGINNING, chapter));
    }
}
