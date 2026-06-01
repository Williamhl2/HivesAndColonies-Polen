package com.hivesandcolonies.polen.entity.ai.goal;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.gesture.PolenGesture;
import com.hivesandcolonies.polen.entity.ai.gesture.PolenGestureController;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSearchStatus;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSearchType;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenSeekSafetyGoal extends Goal {
    private static final double MOVE_SPEED = 1.05D;
    private static final double STOP_DISTANCE_SQR = 2.25D;
    private static final int REPATH_COOLDOWN_TICKS = 20;
    private static final int MAX_FAILED_REPATHS = 6;
    private static final int BLINK_COOLDOWN_TICKS = 30;
    private static final int STUCK_TICKS_BEFORE_BLINK = 18;

    private final PolenEntity polen;

    private BlockPos targetSpot;
    private int repathCooldownTicks;
    private int failedRepathAttempts;
    private boolean unsafeDialoguePlayed;
    private boolean fallbackExplorationMode;
    private int blinkCooldownTicks;
    private int stuckTicks;
    private double lastDistanceSqr;

    public PolenSeekSafetyGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!PolenSafetyNavigator.shouldSeekSafety(this.polen)) {
            return false;
        }

        this.repathCooldownTicks = 0;
        this.failedRepathAttempts = 0;
        this.unsafeDialoguePlayed = false;
        this.fallbackExplorationMode = false;
        this.blinkCooldownTicks = 0;
        this.stuckTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;

        if (planSafeEscapeRoute(20)) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.SCANNING,
                    this.targetSpot,
                    this.targetSpot,
                    "escape_route_planned"
            );
            return true;
        }

        if (planFallbackExploration(16)) {
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
        playUnsafeDialogueIfNeeded();
        PolenSafetyNavigator.tryEmergencyRelocateToSafeSurface(this.polen);
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
        PolenGestureController.triggerGesture(this.polen, PolenGesture.STARTLED);
        playUnsafeDialogueIfNeeded();
        this.polen.getAiState().setSearchState(
                getSearchType(),
                PolenSearchStatus.PATHING,
                this.targetSpot,
                this.targetSpot,
                this.fallbackExplorationMode ? "fallback_pathing" : "safety_pathing"
        );
        this.lastDistanceSqr = this.targetSpot == null ? Double.MAX_VALUE : this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
        moveToTargetSpot();
    }

    @Override
    public void tick() {
        if (this.targetSpot == null) {
            return;
        }

        this.polen.getLookControl().setLookAt(
                this.targetSpot.getX() + 0.5D,
                this.targetSpot.getY(),
                this.targetSpot.getZ() + 0.5D
        );

        if (this.repathCooldownTicks > 0) {
            this.repathCooldownTicks--;
        }
        if (this.blinkCooldownTicks > 0) {
            this.blinkCooldownTicks--;
        }

        if (!PolenSafetyNavigator.shouldSeekSafety(this.polen)) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.ARRIVED,
                    this.targetSpot,
                    this.targetSpot,
                    "safety_need_resolved"
            );
            return;
        }

        double distanceSqr = this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
        boolean reachedTarget = distanceSqr <= STOP_DISTANCE_SQR;
        updateStuckCounter(distanceSqr);

        if (!reachedTarget && this.blinkCooldownTicks == 0
                && (this.stuckTicks >= STUCK_TICKS_BEFORE_BLINK || this.polen.getNavigation().isDone())) {
            if (PolenSafetyNavigator.tryBlinkTowardSafeSpot(this.polen, this.targetSpot, 7)) {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.BLINKING,
                        this.targetSpot,
                        this.targetSpot,
                        "blink_toward_safety"
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

            if (planSafeEscapeRoute(nextRadius)) {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.PATHING,
                        this.targetSpot,
                        this.targetSpot,
                        "repath_safe_route"
                );
                moveToTargetSpot();
                this.failedRepathAttempts = 0;
            } else if (planFallbackExploration(16 + this.failedRepathAttempts * 6)) {
                this.polen.getAiState().setSearchState(
                        PolenSearchType.SAFE_EXPLORATION,
                        PolenSearchStatus.PATHING,
                        this.targetSpot,
                        this.targetSpot,
                        "repath_fallback_route"
                );
                moveToTargetSpot();
                this.failedRepathAttempts++;
            } else {
                this.failedRepathAttempts++;
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.FAILED,
                        this.targetSpot,
                        this.targetSpot,
                        "repath_failed"
                );
                if (this.failedRepathAttempts >= MAX_FAILED_REPATHS) {
                    PolenSafetyNavigator.tryEmergencyRelocateToSafeSurface(this.polen);
                    this.targetSpot = null;
                    return;
                }
            }

            this.repathCooldownTicks = REPATH_COOLDOWN_TICKS;
        }
    }

    @Override
    public void stop() {
        this.polen.getNavigation().stop();
        this.polen.getAiState().clearSearchState();
        this.targetSpot = null;
        this.repathCooldownTicks = 0;
        this.failedRepathAttempts = 0;
        this.unsafeDialoguePlayed = false;
        this.fallbackExplorationMode = false;
        this.blinkCooldownTicks = 0;
        this.stuckTicks = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
    }

    private boolean planSafeEscapeRoute(int radius) {
        if (PolenSafetyNavigator.shouldSeekRainShelter(this.polen)) {
            this.targetSpot = PolenSafetyNavigator.findNearbyShelteredSpot(this.polen, Math.max(8, radius));
        } else if (PolenSafetyNavigator.shouldSeekNightLight(this.polen)) {
            this.targetSpot = PolenSafetyNavigator.findNearbyNightLightSpot(this.polen, Math.max(10, radius));
        } else {
            this.targetSpot = PolenSafetyNavigator.findNearbySafeSurfaceSpot(this.polen, radius);
        }
        this.fallbackExplorationMode = false;
        return this.targetSpot != null;
    }

    private boolean planFallbackExploration(int radius) {
        this.targetSpot = PolenSafetyNavigator.findFallbackExplorationSpot(this.polen, radius);
        this.fallbackExplorationMode = this.targetSpot != null;
        return this.targetSpot != null;
    }

    private void moveToTargetSpot() {
        if (this.targetSpot == null) {
            return;
        }

        boolean pathStarted = this.polen.getNavigation().moveTo(
                this.targetSpot.getX() + 0.5D,
                this.targetSpot.getY(),
                this.targetSpot.getZ() + 0.5D,
                MOVE_SPEED
        );

        if (!pathStarted && this.blinkCooldownTicks == 0) {
            if (PolenSafetyNavigator.tryBlinkTowardSafeSpot(this.polen, this.targetSpot, 7)) {
                PolenGestureController.triggerGesture(this.polen, PolenGesture.STARTLED, BLINK_COOLDOWN_TICKS);
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.BLINKING,
                        this.targetSpot,
                        this.targetSpot,
                        "blink_after_path_fail"
                );
                this.blinkCooldownTicks = BLINK_COOLDOWN_TICKS;
                this.stuckTicks = 0;
                this.lastDistanceSqr = this.polen.distanceToSqr(Vec3.atCenterOf(this.targetSpot));
            } else {
                this.polen.getAiState().setSearchState(
                        getSearchType(),
                        PolenSearchStatus.FAILED,
                        this.targetSpot,
                        this.targetSpot,
                        "path_and_blink_failed"
                );
            }
        } else if (pathStarted) {
            this.polen.getAiState().setSearchState(
                    getSearchType(),
                    PolenSearchStatus.PATHING,
                    this.targetSpot,
                    this.targetSpot,
                    this.fallbackExplorationMode ? "following_fallback_path" : "following_safe_path"
            );
        }
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
}
