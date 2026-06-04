package com.hivesandcolonies.characters.entity.ai.brain.action;

public record PolenAutonomousActionPlan(
        PolenAutonomousActionType type,
        int quietActivityType,
        String dialogueSituation,
        int minDuration,
        int maxDuration
) {
}
