package com.hivesandcolonies.characters.character.polen.entity.ai.world.comfort;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record PolenComfortReport(
        BlockPos origin,
        int totalScore,
        PolenComfortRank rank,
        List<PolenComfortSignal> signals
) {
    public PolenComfortReport {
        if (origin != null) {
            origin = origin.immutable();
        }
        if (rank == null) {
            rank = PolenComfortRank.fromScore(totalScore);
        }
        signals = signals == null ? List.of() : List.copyOf(signals);
    }

    public static PolenComfortReport empty(BlockPos origin) {
        return new PolenComfortReport(origin, 0, PolenComfortRank.POOR, List.of());
    }

    public String summary() {
        PolenComfortSignal dominant = dominantSignal();
        if (dominant == null) {
            return rank + ":" + totalScore;
        }

        return rank + ":" + totalScore + ":" + dominant.category() + ":" + dominant.key();
    }

    public PolenComfortSignal dominantSignal() {
        return signals.stream()
                .filter(signal -> signal.value() != 0)
                .max(Comparator.comparingInt(signal -> Math.abs(signal.value())))
                .orElse(null);
    }

    public List<PolenComfortSignal> positiveSignals() {
        List<PolenComfortSignal> result = new ArrayList<>();
        for (PolenComfortSignal signal : signals) {
            if (signal.value() > 0) {
                result.add(signal);
            }
        }
        return result;
    }

    public List<PolenComfortSignal> negativeSignals() {
        List<PolenComfortSignal> result = new ArrayList<>();
        for (PolenComfortSignal signal : signals) {
            if (signal.value() < 0) {
                result.add(signal);
            }
        }
        return result;
    }
}
