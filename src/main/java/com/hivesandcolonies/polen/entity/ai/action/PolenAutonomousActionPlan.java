package com.hivesandcolonies.polen.entity.ai.action;

public record PolenAutonomousActionPlan(
        PolenAutonomousActionType type,
        int quietActivityType,
        String dialogueSituation,
        int minDuration,
        int maxDuration
) {
}
