package com.hivesandcolonies.characters.character.polen.entity.ai.ability.magic;

import com.hivesandcolonies.characters.character.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.characters.character.polen.entity.PolenAmbientDialogueController;
import com.hivesandcolonies.characters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.characters.character.polen.entity.ai.expression.activity.PolenQuietActivityController;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.light.PolenLightSpotHelper;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedState;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.bootstrap.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class PolenMagicController {

    private static final int QUIET_MAGIC_INTERVAL = 24;
    private static final int MAGIC_DIALOGUE_CHANCE = 5;
    private static final long MANAGED_LIGHT_MIN_LIFETIME_TICKS = 200L;
    private static final Vector3f MAGIC_GREEN = new Vector3f(0.32F, 0.92F, 0.45F);
    private static final Vector3f MAGIC_PURPLE = new Vector3f(0.72F, 0.28F, 0.94F);

    private PolenMagicController() {
    }

    public static void tickQuietMagic(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)
                || !PolenQuietActivityController.isDoingQuietActivity(polen)
                || !canUseSubtleMagic(polen)
                || !canUseCurrentQuietMagic(polen)
                || polen.tickCount % QUIET_MAGIC_INTERVAL != 0) {
            return;
        }

        switch (polen.getQuietActivityType()) {
            case PolenQuietActivityController.QUIET_ACTIVITY_SINGING -> spawnSingingSpell(serverLevel, polen);
            case PolenQuietActivityController.QUIET_ACTIVITY_DRAWING -> spawnDrawingSpell(serverLevel, polen);
            case PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING -> spawnAttunementSpell(serverLevel, polen);
            case PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING -> spawnIlluminationSpell(serverLevel, polen);
            case PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING -> spawnReflectionSpell(serverLevel, polen);
            default -> {
                return;
            }
        }

        if (hasNearbySourceLikeInterest(polen)) {
            rememberNearbySource(polen);
        }
        applyQuietMagicBenefits(polen);
        if (polen.getRandom().nextInt(MAGIC_DIALOGUE_CHANCE) == 0) {
            PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_MAGIC);
        }
    }

    public static void tickPersistentMagic(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos managedLightPos = polen.getAiState().getActiveLightPos();
        if (managedLightPos == null) {
            return;
        }

        if (!serverLevel.getBlockState(managedLightPos).is(ModBlocks.POLEN_LANTERN.get())) {
            clearManagedLight(polen);
            return;
        }

        if (serverLevel.getGameTime() >= polen.getAiState().getActiveLightUntilGameTime()
                && shouldRemoveManagedLight(polen, serverLevel, managedLightPos)) {
            serverLevel.destroyBlock(managedLightPos, false);
            clearManagedLight(polen);
        }
    }

    public static boolean blinkToSafety(PolenEntity polen, BlockPos target) {
        if (target == null || !(polen.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        double fromX = polen.getX();
        double fromY = polen.getY();
        double fromZ = polen.getZ();
        double toX = target.getX() + 0.5D;
        double toY = target.getY();
        double toZ = target.getZ() + 0.5D;

        spawnBlinkBurst(serverLevel, fromX, fromY + 0.9D, fromZ);
        serverLevel.playSound(
                null,
                fromX,
                fromY,
                fromZ,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                0.55F,
                1.35F
        );

        polen.teleportTo(toX, toY, toZ);

        spawnBlinkBurst(serverLevel, toX, toY + 0.9D, toZ);
        serverLevel.playSound(
                null,
                toX,
                toY,
                toZ,
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL,
                0.55F,
                1.15F
        );
        PolenAmbientDialogueController.tryPlay(polen, PolenDialogueManager.AMBIENT_MAGIC);
        return true;
    }


    public static boolean blinkToward(PolenEntity polen, BlockPos target, int maxDistance, boolean requireSafeSpot) {
        if (target == null || !(polen.level() instanceof ServerLevel)) {
            return false;
        }

        BlockPos blinkTarget = findBlinkStep(polen, target, Math.max(2, maxDistance), requireSafeSpot);
        if (blinkTarget == null) {
            return false;
        }

        return blinkToSafety(polen, blinkTarget);
    }

    public static boolean canBlinkToward(PolenEntity polen, BlockPos target, int maxDistance, boolean requireSafeSpot) {
        return target != null && findBlinkStep(polen, target, Math.max(2, maxDistance), requireSafeSpot) != null;
    }

    private static BlockPos findBlinkStep(PolenEntity polen, BlockPos target, int maxDistance, boolean requireSafeSpot) {
        BlockPos origin = polen.blockPosition();
        Vec3 originCenter = Vec3.atCenterOf(origin);
        Vec3 targetCenter = Vec3.atCenterOf(target);
        Vec3 delta = targetCenter.subtract(originCenter);
        double distance = delta.length();
        if (distance < 1.0D) {
            return null;
        }

        Vec3 direction = delta.normalize();
        int longestStep = Math.max(2, Math.min(maxDistance, (int) Math.ceil(distance)));
        double currentDistanceSqr = origin.distSqr(target);

        for (int step = longestStep; step >= 2; step--) {
            Vec3 ideal = originCenter.add(direction.scale(step));
            BlockPos base = BlockPos.containing(ideal);
            BlockPos best = null;
            double bestScore = Double.MAX_VALUE;

            for (int dy = 2; dy >= -3; dy--) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos candidate = base.offset(dx, dy, dz);
                        if (!isValidBlinkSpot(polen, candidate, requireSafeSpot)) {
                            continue;
                        }

                        double candidateDistanceSqr = candidate.distSqr(target);
                        if (candidateDistanceSqr >= currentDistanceSqr - 1.0D) {
                            continue;
                        }

                        double score = candidateDistanceSqr + candidate.distSqr(base) * 0.35D;
                        if (candidate.getY() > origin.getY()) {
                            score -= (candidate.getY() - origin.getY()) * 2.0D;
                        }

                        if (score < bestScore) {
                            bestScore = score;
                            best = candidate.immutable();
                        }
                    }
                }
            }

            if (best != null) {
                return best;
            }
        }

        return null;
    }

    private static boolean isValidBlinkSpot(PolenEntity polen, BlockPos pos, boolean requireSafeSpot) {
        return requireSafeSpot
                ? PolenSafetyEvaluator.isSafeStandingSpot(polen, pos)
                : PolenSafetyEvaluator.isStandableSpot(polen, pos);
    }

    private static boolean canUseSubtleMagic(PolenEntity polen) {
        if (polen.getQuietActivityType() == PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING) {
            return true;
        }
        PolenMood mood = polen.getMood();
        return mood != PolenMood.TIMID && mood != PolenMood.UNSETTLED;
    }

    public static boolean tryPlaceManagedLightImmediately(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        if (polen.getAiState().getActiveLightPos() != null
                && serverLevel.getBlockState(polen.getAiState().getActiveLightPos()).is(ModBlocks.POLEN_LANTERN.get())) {
            return true;
        }

        PolenLightSpotHelper.ManagedLightPlacement placement = PolenLightSpotHelper.findImmediateManagedLightPlacement(polen);
        if (placement == null) {
            return false;
        }

        serverLevel.setBlock(placement.pos(), placement.state(), 3);
        polen.getAiState().setActiveLightState(
                placement.pos().immutable(),
                serverLevel.getGameTime() + MANAGED_LIGHT_MIN_LIFETIME_TICKS
        );
        return true;
    }

    private static boolean canUseCurrentQuietMagic(PolenEntity polen) {
        return switch (polen.getQuietActivityType()) {
            case PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING ->
                    PolenRoutinePlanner.isDarkEnoughForLightMagic(polen) || polen.getAiState().getActiveLightPos() != null;
            case PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING ->
                    hasNearbyManagedLight(polen) || PolenRoutinePlanner.isNearRestingSpot(polen);
            case PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING -> hasNearbySourceLikeInterest(polen);
            default -> hasNearbySourceLikeInterest(polen) || hasNearbyManagedLight(polen);
        };
    }

    public static boolean hasNearbySourceLikeInterest(PolenEntity polen) {
        BlockPos rememberedSource = polen.getAiState().getFavoriteSourcePos();
        if (rememberedSource != null && rememberedSource.closerToCenterThan(polen.position(), 5.0D)) {
            return true;
        }

        return findNearbySourceInterest(polen) != null;
    }

    public static boolean hasNearbyManagedLight(PolenEntity polen) {
        BlockPos managedLightPos = polen.getAiState().getActiveLightPos();
        return managedLightPos != null
                && managedLightPos.closerToCenterThan(polen.position(), 4.0D)
                && polen.level().getBlockState(managedLightPos).is(ModBlocks.POLEN_LANTERN.get());
    }

    private static void spawnSingingSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getEyeY() + 0.15D;
        double z = polen.getZ();
        sendMagicDust(serverLevel, x, y, z, 5, 0.22D, 0.10D, 0.22D, 0.80F, false);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.1D, z, 2, 0.16D, 0.08D, 0.16D, 0.01D);
    }

    private static void spawnDrawingSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getY() + 0.2D;
        double z = polen.getZ();
        sendMagicDust(serverLevel, x, y, z, 6, 0.28D, 0.06D, 0.28D, 0.70F, true);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.35D, z, 1, 0.12D, 0.04D, 0.12D, 0.01D);
    }

    private static void spawnAttunementSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getEyeY() - 0.1D;
        double z = polen.getZ();
        sendMagicDust(serverLevel, x, y, z, 8, 0.35D, 0.18D, 0.35D, 1.0F, polen.tickCount % 48 < 24);
        sendMagicDust(serverLevel, x, y + 0.25D, z, 5, 0.20D, 0.10D, 0.20D, 0.75F, polen.tickCount % 48 >= 24);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.2D, z, 2, 0.18D, 0.16D, 0.18D, 0.02D);
    }

    private static void spawnIlluminationSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getEyeY();
        double z = polen.getZ();
        sendMagicDust(serverLevel, x, y, z, 10, 0.45D, 0.30D, 0.45D, 1.10F, polen.tickCount % 32 < 16);
        sendMagicDust(serverLevel, x, y + 0.45D, z, 6, 0.25D, 0.12D, 0.25D, 0.85F, polen.tickCount % 32 >= 16);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.25D, z, 4, 0.22D, 0.22D, 0.22D, 0.03D);
        placeManagedLightIfPossible(polen, serverLevel);
    }

    private static void spawnReflectionSpell(ServerLevel serverLevel, PolenEntity polen) {
        double x = polen.getX();
        double y = polen.getEyeY() - 0.05D;
        double z = polen.getZ();
        sendMagicDust(serverLevel, x, y, z, 4, 0.18D, 0.08D, 0.18D, 0.55F, polen.tickCount % 40 < 20);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 0.1D, z, 1, 0.10D, 0.04D, 0.10D, 0.01D);
    }

    private static void spawnBlinkBurst(ServerLevel serverLevel, double x, double y, double z) {
        serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 16, 0.35D, 0.45D, 0.35D, 0.12D);
        serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 8, 0.26D, 0.30D, 0.26D, 0.03D);
        sendMagicDust(serverLevel, x, y, z, 12, 0.30D, 0.30D, 0.30D, 0.95F, false);
    }

    private static PolenInterestTarget findNearbySourceInterest(PolenEntity polen) {
        return PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.SOURCE);
    }

    private static void rememberNearbySource(PolenEntity polen) {
        PolenInterestTarget nearbySource = findNearbySourceInterest(polen);
        if (nearbySource != null) {
            polen.getAiState().setFavoriteSourcePos(nearbySource.pos());
        }
    }

    private static void applyQuietMagicBenefits(PolenEntity polen) {
        PolenNeedState needs = polen.getAiState().getNeedState();
        if (polen.getQuietActivityType() == PolenQuietActivityController.QUIET_ACTIVITY_ILLUMINATING) {
            needs.adjustMagic(-4);
            needs.adjustRest(-1);
            return;
        }

        if (polen.getQuietActivityType() == PolenQuietActivityController.QUIET_ACTIVITY_REFLECTING) {
            needs.adjustRest(-4);
            needs.adjustSafety(-2);
            needs.adjustMagic(-1);
            return;
        }

        if (polen.getQuietActivityType() == PolenQuietActivityController.QUIET_ACTIVITY_ATTUNING) {
            needs.adjustMagic(-6);
            needs.adjustRest(-2);
            needs.adjustCuriosity(-1);
            if (polen.getHealth() < polen.getMaxHealth()) {
                polen.heal(1.0F);
            }
            return;
        }

        needs.adjustMagic(-2);
    }

    private static boolean placeManagedLightIfPossible(PolenEntity polen, ServerLevel serverLevel) {
        if (polen.getAiState().getActiveLightPos() != null
                && serverLevel.getBlockState(polen.getAiState().getActiveLightPos()).is(ModBlocks.POLEN_LANTERN.get())) {
            return true;
        }

        PolenLightSpotHelper.ManagedLightPlacement placement =
                PolenLightSpotHelper.findManagedLightPlacement(polen, polen.blockPosition());
        if (placement == null) {
            return false;
        }

        serverLevel.setBlock(placement.pos(), placement.state(), 3);
        polen.getAiState().setActiveLightState(
                placement.pos().immutable(),
                serverLevel.getGameTime() + MANAGED_LIGHT_MIN_LIFETIME_TICKS
        );
        return true;
    }

    private static boolean shouldRemoveManagedLight(PolenEntity polen, ServerLevel serverLevel, BlockPos managedLightPos) {
        return serverLevel.isDay()
                && serverLevel.canSeeSky(managedLightPos.above())
                && PolenSafetyEvaluator.isNearOutdoorSurface(serverLevel, managedLightPos)
                && polen.distanceToSqr(Vec3.atCenterOf(managedLightPos)) <= 100.0D;
    }

    private static void clearManagedLight(PolenEntity polen) {
        polen.getAiState().setActiveLightState(null, 0L);
    }

    private static void sendMagicDust(
            ServerLevel serverLevel,
            double x,
            double y,
            double z,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            float scale,
            boolean reverse
    ) {
        DustColorTransitionOptions dust = reverse
                ? new DustColorTransitionOptions(MAGIC_PURPLE, MAGIC_GREEN, scale)
                : new DustColorTransitionOptions(MAGIC_GREEN, MAGIC_PURPLE, scale);
        serverLevel.sendParticles(dust, x, y, z, count, offsetX, offsetY, offsetZ, 0.01D);
    }
}
