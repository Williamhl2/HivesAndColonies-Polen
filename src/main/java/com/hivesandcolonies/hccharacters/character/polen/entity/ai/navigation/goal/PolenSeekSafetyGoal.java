package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.hccharacters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.PolenMovementHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.gesture.PolenGesture;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.gesture.PolenGestureController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenNightSafetyPlan;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenSeekSafetyGoal extends Goal {
    private static final double MOVE_SPEED = 1.05D;
    private static final double STOP_DISTANCE_SQR = 2.25D;
    private static final double REPEATED_TARGET_DISTANCE_SQR = 2.5D * 2.5D;
    private static final int REPATH_COOLDOWN_TICKS = 20;
    private static final int MAX_FAILED_REPATHS = 6;
    private static final int BLINK_COOLDOWN_TICKS = 30;
    private static final int STUCK_TICKS_BEFORE_BLINK = 18;
    private static final int IMMEDIATE_BLINK_SEARCH_RADIUS = 8;
    private static final int IMMEDIATE_BLINK_DISTANCE = 8;

    private final PolenEntity polen;

    private BlockPos targetSpot;
    private BlockPos navigationAnchorPos;
    private int repathCooldownTicks;
    private int failedRepathAttempts;
    private boolean unsafeDialoguePlayed;
    private boolean fallbackExplorationMode;
    private int blinkCooldownTicks;
    private int stuckTicks;
    private double lastDistanceSqr;
    private boolean resolved;
    private boolean failedGoal;
    private boolean replanningRoute;

    public PolenSeekSafetyGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!PolenSafetyNavigator.shouldSeekSafety(this.polen)) {
            return false;
        }

        PolenNightSafetyPlan nightLightPlan = PolenSafetyNavigator.planNightLightSafety(this.polen, 10);
        if (nightLightPlan != null
                && nightLightPlan.placeLightImmediately()
                && PolenMagicController.tryPlaceManagedLightImmediately(this.polen)) {
            this.resolved = true;
            this.polen.getAiState().setSearchState(
                    PolenSearchType.NIGHT_LIGHT,
                    PolenSearchStatus.ARRIVED,
                    this.polen.blockPosition(),
                    this.polen.blockPosition(),
                    nightLightPlan.note()
            );
            return false;
        }

        this.repathCooldownTicks = 0;
        this.failedRepathAttempts = 0;
        this.navigationAnchorPos = null;
        this.unsafeDialoguePlayed = false;
        this.fallbackExplorationMode = false;
        this.blinkCooldownTicks = 0;
        this.stuckTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.resolved = false;
        this.failedGoal = false;
        this.replanningRoute = false;

        if (planSafeEscapeRoute(20, null)) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.SCANNING,
                    this.targetSpot,
                    this.targetSpot,
                    "escape_route_planned"
            );
            return true;
        }

        if (canUseFallbackExploration() && planFallbackExploration(16, null)) {
            this.polen.getAiState().setSearchState(
                    PolenSearchType.SAFE_EXPLORATION,
                    PolenSearchStatus.SCANNING,
                    this.targetSpot,
                    this.targetSpot,
                    "fallback_exploration_planned"
            );
            return true;
        }

        this.polen.getAiState().setSearchState(
                getSearchType(),
                PolenSearchStatus.FAILED,
                null,
                null,
                "no_escape_route_found"
        );
        this.failedGoal = true;
        PolenTaskController.markFailed(this.polen, PolenTaskType.SEEK_SAFETY, "no_escape_route_found", 0L);
        playUnsafeDialogueIfNeeded();
        if (!PolenSafetyNavigator.shouldSeekRainShelter(this.polen)) {
            PolenSafetyNavigator.tryEmergencyRelocateToSafeSurface(this.polen);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetSpot != null
                && this.failedRepathAttempts < MAX_FAILED_REPATHS
                && PolenSafetyNavigator.shouldSeekSafety(this.polen);
    }

    @Override
    public void start() {
        this.polen.stopQuietActivity();
        PolenTaskController.markActive(this.polen, PolenTaskType.SEEK_SAFETY, "seeking_safety");
        PolenGestureController.triggerGesture(this.polen, PolenGesture.STARTLED);
        playUnsafeDialogueIfNeeded();
        if (PolenSafetyNavigator.hasImmediateHostileThreat(this.polen)
                && PolenSafetyNavigator.tryImmediateHostileBlink(this.polen, IMMEDIATE_BLINK_SEARCH_RADIUS, IMMEDIATE_BLINK_DISTANCE)) {
            this.blinkCooldownTicks = BLINK_COOLDOWN_TICKS;
            this.stuckTicks = 0;
            planSafeEscapeRoute(24, this.targetSpot);
        }
        this.polen.getAiState().setSearchState(
                getSearchType(),
                PolenSearchStatus.PATHING,
                this.targetSpot,
                this.targetSpot,
                contextualSearchNote(this.fallbackExplorationMode ? "fallback_pathing" : "safety_pathing")
        );
        this.lastDistanceSqr = this.targetSpot == null ? Double.MAX_VALUE : this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
        moveToTargetSpot();
    }

    @Override
    public void tick() {
        if (this.targetSpot == null) {
            return;
        }

        PolenMovementHelper.steerLookTowardMovement(this.polen, this.targetSpot, this.navigationAnchorPos, true);

        PolenShelterContextResolver.tryOpenNearbyDoor(this.polen.level(), this.polen.blockPosition(), 2);

        if (this.repathCooldownTicks > 0) {
            this.repathCooldownTicks--;
        }
        if (this.blinkCooldownTicks > 0) {
            this.blinkCooldownTicks--;
        }

        if (!PolenSafetyNavigator.shouldSeekSafety(this.polen)) {
            this.resolved = true;
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.ARRIVED,
                    this.targetSpot,
                    this.targetSpot,
                    contextualSearchNote("safety_need_resolved")
            );
            playShelterArrivalDialogue();
            return;
        }

        PolenNightSafetyPlan nightLightPlan = PolenSafetyNavigator.planNightLightSafety(this.polen, 10);
        if (nightLightPlan != null
                && nightLightPlan.placeLightImmediately()
                && PolenMagicController.tryPlaceManagedLightImmediately(this.polen)) {
            this.resolved = true;
            this.polen.getAiState().setSearchState(
                    PolenSearchType.NIGHT_LIGHT,
                    PolenSearchStatus.ARRIVED,
                    this.polen.blockPosition(),
                    this.polen.blockPosition(),
                    nightLightPlan.note()
            );
            return;
        }

        if (this.blinkCooldownTicks == 0
                && PolenSafetyNavigator.hasImmediateHostileThreat(this.polen)
                && PolenSafetyNavigator.tryImmediateHostileBlink(this.polen, IMMEDIATE_BLINK_SEARCH_RADIUS, IMMEDIATE_BLINK_DISTANCE)) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.BLINKING,
                    this.targetSpot,
                    this.targetSpot,
                    contextualSearchNote("immediate_hostile_blink")
            );
            this.blinkCooldownTicks = BLINK_COOLDOWN_TICKS;
            this.stuckTicks = 0;
            this.lastDistanceSqr = Double.MAX_VALUE;
            if (!planSafeEscapeRoute(24, this.targetSpot)) {
                planFallbackExploration(20, this.targetSpot);
            }
            moveToTargetSpot();
            return;
        }

        double distanceSqr = this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
        boolean reachedTarget = distanceSqr <= STOP_DISTANCE_SQR;
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(this.polen);
        if (!reachedTarget && environment.exposedToRangedThreat()) {
            PolenMovementHelper.applyRangedThreatWeave(this.polen, this.targetSpot);
        }
        updateStuckCounter(distanceSqr);

        if (!PolenSafetyNavigator.isEscapeTargetStillUseful(this.polen, this.targetSpot) && this.repathCooldownTicks == 0) {
            if (replanAwayFromCurrentTarget(24 + this.failedRepathAttempts * 6, "obsolete_escape_target")) {
                this.failedRepathAttempts = 0;
                this.repathCooldownTicks = REPATH_COOLDOWN_TICKS;
                return;
            }
        }

        if (!reachedTarget && this.blinkCooldownTicks == 0
                && (this.stuckTicks >= STUCK_TICKS_BEFORE_BLINK || this.polen.getNavigation().isDone())) {
            if (replanAwayFromCurrentTarget(24 + this.failedRepathAttempts * 6, "stuck_repath_safe_route")) {
                this.failedRepathAttempts = 0;
                this.repathCooldownTicks = REPATH_COOLDOWN_TICKS;
                return;
            }
            if (PolenSafetyNavigator.tryBlinkTowardSafeSpot(this.polen, this.targetSpot, 7)) {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.BLINKING,
                        this.targetSpot,
                        this.targetSpot,
                        contextualSearchNote("blink_toward_safety")
                );
                this.blinkCooldownTicks = BLINK_COOLDOWN_TICKS;
                this.stuckTicks = 0;
                this.lastDistanceSqr = this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
                moveToTargetSpot();
                return;
            }
        }

        if ((reachedTarget || this.polen.getNavigation().isDone()) && this.repathCooldownTicks == 0) {
            int nextRadius = reachedTarget ? 16 : 20 + this.failedRepathAttempts * 8;

            if (replanAwayFromCurrentTarget(nextRadius, "repath_safe_route")) {
                this.failedRepathAttempts = 0;
            } else {
                this.failedRepathAttempts++;
                this.failedGoal = true;
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.FAILED,
                        this.targetSpot,
                        this.targetSpot,
                        contextualSearchNote("repath_failed")
                );
                PolenTaskController.markFailed(this.polen, PolenTaskType.SEEK_SAFETY, "repath_failed", 0L);
                if (this.failedRepathAttempts >= MAX_FAILED_REPATHS) {
                    if (!PolenSafetyNavigator.shouldSeekRainShelter(this.polen)) {
                        PolenSafetyNavigator.tryEmergencyRelocateToSafeSurface(this.polen);
                    }
                    this.targetSpot = null;
                    return;
                }
            }

            this.repathCooldownTicks = REPATH_COOLDOWN_TICKS;
        }
    }

    @Override
    public void stop() {
        if (this.resolved) {
            PolenTaskController.markCompleted(this.polen, PolenTaskType.SEEK_SAFETY, "safety_resolved");
        } else if (this.failedGoal) {
            PolenTaskController.markFailed(this.polen, PolenTaskType.SEEK_SAFETY, "safety_goal_aborted", 0L);
        }

        this.polen.getNavigation().stop();
        this.polen.getAiState().clearSearchState();
        this.targetSpot = null;
        this.navigationAnchorPos = null;
        this.repathCooldownTicks = 0;
        this.failedRepathAttempts = 0;
        this.unsafeDialoguePlayed = false;
        this.fallbackExplorationMode = false;
        this.blinkCooldownTicks = 0;
        this.stuckTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.resolved = false;
        this.failedGoal = false;
        this.replanningRoute = false;
    }

    private boolean planSafeEscapeRoute(int radius, BlockPos avoidTarget) {
        BlockPos immediateEscape = PolenSafetyNavigator.findImmediateHostileEscapeSpot(
                this.polen,
                Math.max(8, radius),
                avoidTarget
        );
        if (immediateEscape != null) {
            this.targetSpot = immediateEscape;
        } else if (PolenSafetyNavigator.shouldSeekRainShelter(this.polen)) {
            this.targetSpot = PolenSafetyNavigator.findNearbyShelteredSpot(this.polen, Math.max(8, radius), avoidTarget);
        } else if (PolenSafetyNavigator.shouldSeekNightLight(this.polen)) {
            PolenNightSafetyPlan plan = PolenSafetyNavigator.planNightLightSafety(this.polen, Math.max(10, radius));
            this.targetSpot = plan == null ? null : plan.targetPos();
            if (isRejectedRepeatTarget(this.targetSpot, avoidTarget)) {
                this.targetSpot = null;
            }
        } else {
            this.targetSpot = PolenSafetyNavigator.findNearbySafeSurfaceSpot(this.polen, radius, avoidTarget);
        }
        this.targetSpot = PolenMovementHelper.resolveReachableTarget(this.polen, this.targetSpot, true);
        if (isRejectedRepeatTarget(this.targetSpot, avoidTarget)) {
            this.targetSpot = null;
        }
        this.navigationAnchorPos = null;
        this.fallbackExplorationMode = false;
        return this.targetSpot != null;
    }

    private boolean planFallbackExploration(int radius, BlockPos avoidTarget) {
        this.targetSpot = PolenMovementHelper.resolveReachableTarget(
                this.polen,
                PolenSafetyNavigator.findFallbackExplorationSpot(this.polen, radius, avoidTarget),
                false
        );
        if (isRejectedRepeatTarget(this.targetSpot, avoidTarget)) {
            this.targetSpot = null;
        }
        this.navigationAnchorPos = null;
        this.fallbackExplorationMode = this.targetSpot != null;
        return this.targetSpot != null;
    }

    private boolean canUseFallbackExploration() {
        return !PolenSafetyNavigator.shouldSeekRainShelter(this.polen);
    }

    private void moveToTargetSpot() {
        if (this.targetSpot == null) {
            return;
        }

        PolenShelterContextResolver.tryOpenNearbyDoor(this.polen.level(), this.targetSpot, 2);

        this.navigationAnchorPos = PolenMovementHelper.startAnchoredMove(
                this.polen,
                this.targetSpot,
                this.navigationAnchorPos,
                MOVE_SPEED,
                true
        );
        boolean pathStarted = this.navigationAnchorPos != null;

        if (!pathStarted && this.blinkCooldownTicks == 0) {
            BlockPos blockedTarget = this.targetSpot;
            this.navigationAnchorPos = null;
            PolenNightSafetyPlan nightLightPlan = PolenSafetyNavigator.planNightLightSafety(this.polen, 10);
            if (nightLightPlan != null
                    && nightLightPlan.placeLightImmediately()
                    && PolenMagicController.tryPlaceManagedLightImmediately(this.polen)) {
                this.resolved = true;
                this.polen.getAiState().setSearchState(
                        PolenSearchType.NIGHT_LIGHT,
                        PolenSearchStatus.ARRIVED,
                        this.polen.blockPosition(),
                        this.polen.blockPosition(),
                        nightLightPlan.note()
                );
                return;
            }
            if (PolenSafetyNavigator.tryBlinkTowardSafeSpot(this.polen, this.targetSpot, 7)) {
                PolenGestureController.triggerGesture(this.polen, PolenGesture.STARTLED, BLINK_COOLDOWN_TICKS);
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.BLINKING,
                        this.targetSpot,
                        this.targetSpot,
                        contextualSearchNote("blink_after_path_fail")
                );
                this.blinkCooldownTicks = BLINK_COOLDOWN_TICKS;
                this.stuckTicks = 0;
                this.lastDistanceSqr = this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
            } else {
                if (!this.replanningRoute
                        && replanAwayFrom(blockedTarget, 24 + this.failedRepathAttempts * 6, "path_failed_replan")) {
                    this.failedRepathAttempts++;
                    return;
                }
                this.failedGoal = true;
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.FAILED,
                        this.targetSpot,
                        this.targetSpot,
                        contextualSearchNote("path_and_blink_failed")
                );
                PolenTaskController.markFailed(this.polen, PolenTaskType.SEEK_SAFETY, "path_and_blink_failed", 0L);
            }
        } else if (pathStarted) {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.PATHING,
                        this.navigationAnchorPos == null ? this.targetSpot : this.navigationAnchorPos,
                        this.targetSpot,
                        contextualSearchNote(this.fallbackExplorationMode ? "following_fallback_path" : "following_safe_path")
                );
        }
    }

    private boolean replanAwayFromCurrentTarget(int radius, String note) {
        return replanAwayFrom(this.targetSpot, radius, note);
    }

    private boolean replanAwayFrom(BlockPos avoidTarget, int radius, String note) {
        if (planSafeEscapeRoute(radius, avoidTarget)) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.PATHING,
                    this.targetSpot,
                    this.targetSpot,
                    contextualSearchNote(note)
            );
            boolean previousReplanningRoute = this.replanningRoute;
            this.replanningRoute = true;
            try {
                moveToTargetSpot();
            } finally {
                this.replanningRoute = previousReplanningRoute;
            }
            return true;
        }

        if (canUseFallbackExploration() && planFallbackExploration(Math.max(16, radius - 4), avoidTarget)) {
            this.polen.getAiState().setSearchState(
                    PolenSearchType.SAFE_EXPLORATION,
                    PolenSearchStatus.PATHING,
                    this.targetSpot,
                    this.targetSpot,
                    "repath_fallback_route"
            );
            boolean previousReplanningRoute = this.replanningRoute;
            this.replanningRoute = true;
            try {
                moveToTargetSpot();
            } finally {
                this.replanningRoute = previousReplanningRoute;
            }
            return true;
        }

        return false;
    }

    private PolenSearchType getSearchType() {
        if (this.fallbackExplorationMode) {
            return PolenSearchType.SAFE_EXPLORATION;
        }
        if (PolenSafetyNavigator.shouldSeekRainShelter(this.polen)) {
            return PolenSearchType.RAIN_SHELTER;
        }
        if (PolenSafetyNavigator.shouldSeekNightLight(this.polen)) {
            return PolenSearchType.NIGHT_LIGHT;
        }
        return PolenSearchType.SAFE_SURFACE;
    }

    private void updateStuckCounter(double distanceSqr) {
        if (distanceSqr < this.lastDistanceSqr - 0.04D) {
            this.stuckTicks = 0;
        } else {
            this.stuckTicks++;
        }

        this.lastDistanceSqr = distanceSqr;
    }

    private void playUnsafeDialogueIfNeeded() {
        if (!this.unsafeDialoguePlayed && PolenSafetyNavigator.shouldUseUnsafeDialogue(this.polen)) {
            PolenAmbientDialogueController.tryPlay(this.polen, PolenDialogueManager.AMBIENT_UNSAFE);
            this.unsafeDialoguePlayed = true;
        }
    }

    private void playShelterArrivalDialogue() {
        String situation = PolenShelterContextResolver.resolveAmbientSituation(this.polen.level(), this.polen.blockPosition());
        if (situation != null) {
            PolenAmbientDialogueController.tryPlay(this.polen, situation);
        }
    }

    private String contextualSearchNote(String baseNote) {
        if (this.targetSpot == null) {
            return baseNote;
        }

        return PolenShelterContextResolver.appendShelterContext(baseNote, this.polen.level(), this.targetSpot);
    }

    private boolean isRejectedRepeatTarget(BlockPos candidate, BlockPos avoidedTarget) {
        return candidate != null
                && avoidedTarget != null
                && candidate.distSqr(avoidedTarget) <= REPEATED_TARGET_DISTANCE_SQR;
    }
}
