package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import net.minecraft.core.BlockPos;

public record PolenEnvironmentSnapshot(
        BlockPos originPos,
        PolenShelterKind shelterKind,
        BlockPos hostileMemoryPos,
        BlockPos safetyThreatPos,
        BlockPos immediateHostileThreatPos,
        BlockPos rangedThreatPos,
        boolean safeStandingSpot,
        boolean dangerousStandingSpot,
        boolean claustrophobicStandingSpot,
        boolean exposedToRangedThreat,
        boolean rainExposed,
        boolean rainSheltered,
        boolean nearbyLight,
        boolean nearbyBed,
        boolean overheadCover,
        boolean nearOutdoorSurface,
        boolean night,
        boolean raining,
        boolean nearbyManagedLight,
        boolean activeManagedLight,
        boolean darkEnoughForLightMagic,
        boolean readyToIlluminateHere,
        boolean untrustedPlayerNearby
) {
    public boolean hostileNearby() {
        return this.hostileMemoryPos != null;
    }

    public boolean rangedThreatVisible() {
        return this.rangedThreatPos != null;
    }

    public boolean immediateThreat() {
        return this.immediateHostileThreatPos != null || this.exposedToRangedThreat || this.untrustedPlayerNearby;
    }

    public boolean isInUnsafeArea() {
        return !this.safeStandingSpot || this.hostileNearby() || this.exposedToRangedThreat;
    }

    public boolean shouldSeekRainShelter() {
        return this.rainExposed;
    }

    public boolean needsNightLight() {
        return this.night
                && !this.activeManagedLight
                && !this.nearbyManagedLight
                && this.darkEnoughForLightMagic;
    }

    public boolean shouldUseUnsafeDialogue() {
        return this.claustrophobicStandingSpot
                || this.exposedToRangedThreat
                || this.shouldSeekRainShelter()
                || this.needsNightLight();
    }

    public boolean shouldSeekSafety(boolean prioritizeBedReturn) {
        if (this.dangerousStandingSpot || this.safetyThreatPos != null || this.exposedToRangedThreat) {
            return true;
        }

        if (prioritizeBedReturn) {
            return this.claustrophobicStandingSpot;
        }

        return this.claustrophobicStandingSpot || this.shouldSeekRainShelter() || this.needsNightLight();
    }
}
