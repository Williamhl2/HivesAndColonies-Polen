package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.ability.magic.PolenMagicController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenThreatAssessmentHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterKind;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PolenEnvironmentResolver {
    private static final double HOSTILE_MEMORY_RANGE = 8.0D;
    private static final double SAFETY_THREAT_RANGE = 6.0D;
    private static final double IMMEDIATE_HOSTILE_THREAT_RANGE = 5.5D;
    private static final double UNTRUSTED_PLAYER_RANGE = 2.5D;

    private PolenEnvironmentResolver() {
    }

    public static PolenEnvironmentSnapshot inspect(PolenEntity polen) {
        if (polen == null) {
            return emptySnapshot(null);
        }

        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        return new PolenEnvironmentSnapshot(
                origin.immutable(),
                resolveShelterKind(level, origin),
                findNearestHostilePos(polen, HOSTILE_MEMORY_RANGE),
                findNearestHostilePos(polen, SAFETY_THREAT_RANGE),
                findNearestHostilePos(polen, IMMEDIATE_HOSTILE_THREAT_RANGE),
                PolenThreatAssessmentHelper.findNearestVisibleRangedThreatPos(polen, HOSTILE_MEMORY_RANGE + 4.0D),
                PolenSafetyEvaluator.isSafeStandingSpot(polen, origin),
                PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, origin),
                PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, origin),
                PolenThreatAssessmentHelper.isExposedToRangedThreat(polen, origin, HOSTILE_MEMORY_RANGE + 4.0D),
                PolenSafetyEvaluator.isExposedToRain(level, origin),
                PolenSafetyEvaluator.isRainShelteredStandingSpot(level, origin),
                PolenShelterContextResolver.hasNearbyLight(level, origin),
                PolenShelterContextResolver.hasNearbyBed(level, origin),
                PolenSafetyEvaluator.hasOverheadCover(level, origin),
                PolenSafetyEvaluator.isNearOutdoorSurface(level, origin),
                level.isNight(),
                level.isRaining(),
                PolenMagicController.hasNearbyManagedLight(polen),
                polen.getAiState().getActiveLightPos() != null,
                PolenRoutinePlanner.isDarkEnoughForLightMagic(polen),
                PolenRoutinePlanner.isReadyToIlluminateHere(polen),
                hasNearbyUntrustedPlayer(polen)
        );
    }

    public static PolenEnvironmentSnapshot inspect(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return emptySnapshot(pos);
        }

        return new PolenEnvironmentSnapshot(
                pos.immutable(),
                resolveShelterKind(level, pos),
                null,
                null,
                null,
                null,
                PolenSafetyEvaluator.isStandableSpotProxy(level, pos),
                PolenSafetyEvaluator.isDangerousStandingSpotProxy(level, pos),
                PolenSafetyEvaluator.isClaustrophobicStandingSpotProxy(level, pos),
                false,
                PolenSafetyEvaluator.isExposedToRain(level, pos),
                PolenSafetyEvaluator.isRainShelteredStandingSpot(level, pos),
                PolenShelterContextResolver.hasNearbyLight(level, pos),
                PolenShelterContextResolver.hasNearbyBed(level, pos),
                PolenSafetyEvaluator.hasOverheadCover(level, pos),
                PolenSafetyEvaluator.isNearOutdoorSurface(level, pos),
                level.isNight(),
                level.isRaining(),
                false,
                false,
                false,
                false,
                false
        );
    }

    private static PolenEnvironmentSnapshot emptySnapshot(BlockPos pos) {
        return new PolenEnvironmentSnapshot(
                pos == null ? null : pos.immutable(),
                PolenShelterKind.NONE,
                null,
                null,
                null,
                null,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false
        );
    }

    private static PolenShelterKind resolveShelterKind(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return PolenShelterKind.NONE;
        }

        if (PolenShelterContextResolver.isTreeShelter(level, pos)) {
            return PolenShelterKind.TREE;
        }

        if (PolenShelterContextResolver.isHouseInterior(level, pos)) {
            return PolenShelterKind.HOUSE;
        }

        if (PolenShelterContextResolver.isRoofShelter(level, pos)) {
            return PolenShelterKind.ROOF;
        }

        return PolenShelterKind.NONE;
    }

    private static boolean hasNearbyUntrustedPlayer(PolenEntity polen) {
        Player player = polen.level().getNearestPlayer(polen, UNTRUSTED_PLAYER_RANGE);
        return player != null && player.isAlive() && !polen.isComfortableWith(player);
    }

    private static BlockPos findNearestHostilePos(PolenEntity polen, double radius) {
        Monster nearest = polen.level().getEntitiesOfClass(
                        Monster.class,
                        new AABB(Vec3.atCenterOf(polen.blockPosition()), Vec3.atCenterOf(polen.blockPosition())).inflate(radius),
                        monster -> monster.isAlive() && !monster.isSpectator()
                ).stream()
                .min((left, right) -> Double.compare(left.distanceToSqr(polen), right.distanceToSqr(polen)))
                .orElse(null);
        return nearest == null ? null : nearest.blockPosition().immutable();
    }
}
