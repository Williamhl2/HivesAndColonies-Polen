package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class PolenThreatAssessmentHelper {
    private static final double DEFAULT_RANGED_THREAT_RANGE = 14.0D;

    private PolenThreatAssessmentHelper() {
    }

    public static boolean isProjectileDamage(DamageSource source) {
        return source != null && source.getDirectEntity() instanceof Projectile;
    }

    public static boolean isRangedHostile(Monster hostile) {
        return hostile instanceof RangedAttackMob
                || hostile instanceof AbstractSkeleton
                || hostile.getMainHandItem().getItem() instanceof ProjectileWeaponItem
                || hostile.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
    }

    public static BlockPos findNearestVisibleRangedThreatPos(PolenEntity polen, double radius) {
        Monster hostile = findNearestVisibleRangedThreat(polen, radius);
        return hostile == null ? null : hostile.blockPosition().immutable();
    }

    public static boolean isExposedToRangedThreat(PolenEntity polen, BlockPos pos, double radius) {
        return !findNearbyRangedThreats(polen, radius).isEmpty()
                && findNearbyRangedThreats(polen, radius).stream()
                .anyMatch(hostile -> hasLineOfFire(hostile.level(), hostile, pos));
    }

    public static double scoreRangedExposure(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        return scoreRangedExposure(polen, origin, candidate, DEFAULT_RANGED_THREAT_RANGE);
    }

    public static double scoreRangedExposure(PolenEntity polen, BlockPos origin, BlockPos candidate, double radius) {
        List<Monster> rangedThreats = findNearbyRangedThreats(polen, radius);
        if (rangedThreats.isEmpty()) {
            return 0.0D;
        }

        Level level = polen.level();
        boolean originExposed = false;
        boolean candidateExposed = false;
        double penalty = 0.0D;
        Vec3 candidateCenter = Vec3.atCenterOf(candidate);

        for (Monster hostile : rangedThreats) {
            boolean hostileSeesOrigin = hostile.hasLineOfSight(polen) || hasLineOfFire(level, hostile, origin);
            boolean hostileSeesCandidate = hasLineOfFire(level, hostile, candidate);
            originExposed |= hostileSeesOrigin;
            candidateExposed |= hostileSeesCandidate;

            if (!hostileSeesCandidate) {
                continue;
            }

            double distanceSqr = hostile.distanceToSqr(candidateCenter);
            penalty += Math.max(0.0D, 196.0D - distanceSqr) * 0.65D;
        }

        if (!originExposed && !candidateExposed) {
            return 0.0D;
        }

        if (candidateExposed) {
            penalty += PolenSafetyEvaluator.hasOverheadCover(level, candidate) ? 18.0D : 54.0D;
            if (level.canSeeSky(candidate)) {
                penalty += 42.0D;
            } else if (PolenSafetyEvaluator.isNearOutdoorSurface(level, candidate)) {
                penalty += 20.0D;
            }
        }

        if (originExposed && !candidateExposed) {
            penalty -= 52.0D;
        } else if (!originExposed && candidateExposed) {
            penalty += 18.0D;
        }

        return penalty;
    }

    public static boolean hasLineOfFire(Level level, Entity source, BlockPos targetPos) {
        if (level == null || source == null || targetPos == null) {
            return false;
        }

        Vec3 sourcePos = source.getEyePosition();
        Vec3 targetCenter = new Vec3(
                targetPos.getX() + 0.5D,
                targetPos.getY() + 0.9D,
                targetPos.getZ() + 0.5D
        );
        HitResult hitResult = level.clip(new ClipContext(
                sourcePos,
                targetCenter,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                source
        ));
        return hitResult.getType() == HitResult.Type.MISS;
    }

    private static Monster findNearestVisibleRangedThreat(PolenEntity polen, double radius) {
        return findNearbyRangedThreats(polen, radius).stream()
                .filter(hostile -> hostile.hasLineOfSight(polen) || hasLineOfFire(hostile.level(), hostile, polen.blockPosition()))
                .min((left, right) -> Double.compare(left.distanceToSqr(polen), right.distanceToSqr(polen)))
                .orElse(null);
    }

    private static List<Monster> findNearbyRangedThreats(PolenEntity polen, double radius) {
        Vec3 center = Vec3.atCenterOf(polen.blockPosition());
        return polen.level().getEntitiesOfClass(
                Monster.class,
                new AABB(center, center).inflate(radius),
                hostile -> hostile.isAlive()
                        && !hostile.isSpectator()
                        && isRangedHostile(hostile)
        );
    }
}
