package com.hivesandcolonies.polen.entity.ai.world.comfort;

import com.hivesandcolonies.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PolenComfortEvaluator {
    private PolenComfortEvaluator() {
    }

    public static PolenComfortReport evaluate(PolenEntity polen, BlockPos origin) {
        return evaluate(polen, origin, PolenComfortProfile.SHELTER);
    }

    public static PolenComfortReport evaluate(PolenEntity polen, BlockPos origin, PolenComfortProfile profile) {
        if (polen == null || origin == null) {
            return PolenComfortReport.empty(origin);
        }

        Level level = polen.level();
        List<PolenComfortSignal> signals = new ArrayList<>();
        PolenComfortProfile resolvedProfile = profile == null ? PolenComfortProfile.SHELTER : profile;
        for (PolenComfortRule rule : PolenComfortRules.DEFAULT) {
            rule.collect(polen, level, origin, resolvedProfile, signals);
        }

        int score = signals.stream().mapToInt(PolenComfortSignal::value).sum();
        signals.sort(Comparator.comparingInt((PolenComfortSignal signal) -> Math.abs(signal.value())).reversed());
        return new PolenComfortReport(origin, score, PolenComfortRank.fromScore(score), signals);
    }

    public static double comfortAdjustedDistanceScore(PolenEntity polen, BlockPos origin, BlockPos candidate, PolenComfortProfile profile) {
        if (origin == null || candidate == null) {
            return Double.MAX_VALUE;
        }
        PolenComfortReport report = evaluate(polen, candidate, profile);
        return candidate.distSqr(origin) - report.totalScore();
    }
}
