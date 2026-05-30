package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.goal.PolenCuriousInterestGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenIdleHobbyGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenKeepDistanceGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenRoutineGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenSafeStrollGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenSeekSafetyGoal;
import com.hivesandcolonies.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.polen.story.PolenStoryEventManager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class PolenEntity extends PathfinderMob {
    private static final String UNKNOWN_GIRL_KEY = "entity.polen.unknown_girl";
    private static final String POLEN_KEY = "entity.polen.polen";
    private static final String TAG_FAVORITE_FLOWER_POS = "FavoriteFlowerPos";
    private static final String TAG_FAVORITE_HIVE_POS = "FavoriteHivePos";
    private static final String TAG_RESTING_POS = "RestingPos";
    private static final String TAG_DANGEROUS_SPOT_POS = "DangerousSpotPos";
    private static final String TAG_DANGEROUS_SPOT_UNTIL = "DangerousSpotUntil";
    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY_TICKS =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_MOOD =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);

    public static final int QUIET_ACTIVITY_NONE = 0;
    public static final int QUIET_ACTIVITY_SINGING = 1;
    public static final int QUIET_ACTIVITY_DRAWING = 2;
    private static final long AMBIENT_DIALOGUE_COOLDOWN = 160L;
    private static final double AMBIENT_DIALOGUE_RANGE = 8.0D;
    private static final double DANGEROUS_SPOT_AVOID_RADIUS = 5.0D;
    private static final long DANGEROUS_SPOT_MEMORY_DURATION = 24000L;

    private BlockPos favoriteFlowerPos;
    private BlockPos favoriteHivePos;
    private BlockPos restingPos;
    private BlockPos dangerousSpotPos;
    private long dangerousSpotUntilGameTime;
    private long lastAmbientDialogueGameTime;

    public PolenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable(UNKNOWN_GIRL_KEY));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(QUIET_ACTIVITY, QUIET_ACTIVITY_NONE);
        builder.define(QUIET_ACTIVITY_TICKS, 0);
        builder.define(CURRENT_MOOD, PolenMood.CALM.getId());
    }

    public void updateDisplayName() {
        if (this.level() instanceof ServerLevel serverLevel
                && PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.NAME_REVEALED)) {
            this.setCustomName(Component.translatable(POLEN_KEY));
        } else {
            this.setCustomName(Component.translatable(UNKNOWN_GIRL_KEY));
        }

        this.setCustomNameVisible(true);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            tickQuietActivityParticles();
            return;
        }

        if (this.tickCount % 20 == 0) {
            this.updateDisplayName();
            this.updateMood();
        }

        if (this.tickCount % 100 == 0) {
            seedMemoriesFromNearbyEnvironment();
        }

        int quietActivityTicks = this.entityData.get(QUIET_ACTIVITY_TICKS);
        if (quietActivityTicks > 0) {
            this.entityData.set(QUIET_ACTIVITY_TICKS, quietActivityTicks - 1);
            if (quietActivityTicks == 1) {
                stopQuietActivity();
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PolenSeekSafetyGoal(this));
        this.goalSelector.addGoal(2, new PolenKeepDistanceGoal(this));
        this.goalSelector.addGoal(3, new PolenRoutineGoal(this));
        this.goalSelector.addGoal(4, new PolenIdleHobbyGoal(this));
        this.goalSelector.addGoal(5, new PolenCuriousInterestGoal(this));
        this.goalSelector.addGoal(6, new PolenSafeStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        saveBlockPos(tag, TAG_FAVORITE_FLOWER_POS, this.favoriteFlowerPos);
        saveBlockPos(tag, TAG_FAVORITE_HIVE_POS, this.favoriteHivePos);
        saveBlockPos(tag, TAG_RESTING_POS, this.restingPos);
        saveBlockPos(tag, TAG_DANGEROUS_SPOT_POS, this.dangerousSpotPos);
        tag.putLong(TAG_DANGEROUS_SPOT_UNTIL, this.dangerousSpotUntilGameTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.favoriteFlowerPos = loadBlockPos(tag, TAG_FAVORITE_FLOWER_POS);
        this.favoriteHivePos = loadBlockPos(tag, TAG_FAVORITE_HIVE_POS);
        this.restingPos = loadBlockPos(tag, TAG_RESTING_POS);
        this.dangerousSpotPos = loadBlockPos(tag, TAG_DANGEROUS_SPOT_POS);
        this.dangerousSpotUntilGameTime = Math.max(0L, tag.getLong(TAG_DANGEROUS_SPOT_UNTIL));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            updateDisplayName();
            PolenPlayerRelationshipManager.recordInteraction(player);

            if (player instanceof ServerPlayer serverPlayer) {
                PolenAdvancementManager.grantFirstMeeting(serverPlayer);
            }

            int currentChapter = PolenChapterManager.getCurrentChapter(player);
            int affinity = PolenAffinityManager.getAffinity(player);

            /*
             * TODO v0.0.6
             *
             * Shelter recognition event is currently disabled.
             *
             * The original implementation triggered automatically when
             * entering FOUNDATION chapter, which caused premature story
             * progression.
             *
             * Re-enable only after implementing a real shelter detection
             * system (bed, roof, walls, door, etc.).
             */
            // if (shouldPlayShelterRecognition(currentChapter, player)) {
            //     PolenStoryEventManager.playShelterRecognition(player);
            //     updateDisplayName();
            //     return InteractionResult.SUCCESS;
            // }

            if (player instanceof ServerPlayer serverPlayer
                    && affinity >= PolenAffinityLevels.FIRST_TRUST) {
                PolenAdvancementManager.grantFirstTrust(serverPlayer);
            }

            if (shouldRevealName(player, affinity)) {
                PolenStoryEventManager.playNameReveal(player);
                updateDisplayName();
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(
                    PolenDialogueManager.getDialogue(
                            player,
                            currentChapter,
                            affinity,
                            this.getRandom()
                    ),
                    false
            );

            updateDisplayName();
        }

        return InteractionResult.SUCCESS;
    }

    // Función adelantada para reconocer el refugio, se activará en el capítulo de la fundación
    // private static boolean shouldPlayShelterRecognition(int currentChapter, Player player) {
    //     return currentChapter == PolenChapterManager.FOUNDATION
    //             && !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.PLAYER_HAS_SHELTER);
    // }

    private static boolean shouldRevealName(Player player, int affinity) {
        return !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL;
    }

    public boolean isDoingQuietActivity() {
        return this.entityData.get(QUIET_ACTIVITY) != QUIET_ACTIVITY_NONE;
    }

    public void startQuietActivity(int activityType, int ticks) {
        this.entityData.set(QUIET_ACTIVITY, activityType);
        this.entityData.set(QUIET_ACTIVITY_TICKS, ticks);
    }

    public void stopQuietActivity() {
        if (this.entityData.get(QUIET_ACTIVITY) != QUIET_ACTIVITY_NONE
                || this.entityData.get(QUIET_ACTIVITY_TICKS) != 0) {
            this.entityData.set(QUIET_ACTIVITY, QUIET_ACTIVITY_NONE);
            this.entityData.set(QUIET_ACTIVITY_TICKS, 0);
        }
    }

    public boolean hasNearbyPlayer(double range) {
        return this.level().getNearestPlayer(this, range) != null;
    }

    public boolean isComfortableWith(Player player) {
        return PolenAffinityManager.getAffinity(player) >= PolenAffinityLevels.FRIEND;
    }

    public PolenMood getMood() {
        return PolenMood.fromId(this.entityData.get(CURRENT_MOOD));
    }

    public BlockPos getRoutineTarget() {
        if (this.level().isRaining() || this.level().isThundering() || this.level().isNight()) {
            if (isRememberedSpotStillValid(this.restingPos) && isSafeStandingSpot(this.restingPos)) {
                return this.restingPos;
            }

            BlockPos safeSpot = findNearbySafeSurfaceSpot(10);
            return safeSpot != null ? safeSpot : null;
        }

        long dayTime = this.level().getDayTime() % 24000L;
        if (dayTime < 6000L && isRememberedSpotStillValid(this.favoriteFlowerPos) && isSafeInterestSpot(this.favoriteFlowerPos)) {
            return this.favoriteFlowerPos;
        }

        if (dayTime < 12000L && isRememberedSpotStillValid(this.favoriteHivePos) && isSafeInterestSpot(this.favoriteHivePos)) {
            return this.favoriteHivePos;
        }

        if (isRememberedSpotStillValid(this.favoriteFlowerPos) && isSafeInterestSpot(this.favoriteFlowerPos)) {
            return this.favoriteFlowerPos;
        }

        if (isRememberedSpotStillValid(this.favoriteHivePos) && isSafeInterestSpot(this.favoriteHivePos)) {
            return this.favoriteHivePos;
        }

        if (isRememberedSpotStillValid(this.restingPos) && isSafeStandingSpot(this.restingPos)) {
            return this.restingPos;
        }

        BlockPos safeSpot = findNearbySafeSurfaceSpot(10);
        return safeSpot != null ? safeSpot : null;
    }

    public boolean isRememberedSpotStillValid(BlockPos pos) {
        if (pos == null) {
            return false;
        }

        return (!this.level().getBlockState(pos).isAir() || pos.closerToCenterThan(this.position(), 2.0D))
                && !isDangerousMemorySpot(pos);
    }

    public void rememberInterestingSpot(BlockPos pos) {
        if (pos == null) {
            return;
        }

        if (this.level().getBlockState(pos).is(net.minecraft.tags.BlockTags.FLOWERS)) {
            this.favoriteFlowerPos = pos.immutable();
            return;
        }

        if (this.level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEE_NEST)
                || this.level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEEHIVE)) {
            this.favoriteHivePos = pos.immutable();
        }
    }

    public void rememberRestingSpot(BlockPos pos) {
        if (pos != null && isSafeStandingSpot(pos) && !isDangerousMemorySpot(pos)) {
            this.restingPos = pos.immutable();
        }
    }

    public boolean isInUnsafeArea() {
        boolean unsafe = !isSafeStandingSpot(this.blockPosition());
        if (unsafe) {
            rememberDangerousSpot(this.blockPosition());
        }
        return unsafe;
    }

    public boolean isSafeStandingSpot(BlockPos pos) {
        if (pos == null) {
            return false;
        }

        if (!this.level().getFluidState(pos).isEmpty() || !this.level().getFluidState(pos.above()).isEmpty()) {
            return false;
        }

        if (!this.level().getBlockState(pos).canBeReplaced() || !this.level().getBlockState(pos.above()).canBeReplaced()) {
            return false;
        }

        if (!this.level().getBlockState(pos.below()).isFaceSturdy(this.level(), pos.below(), net.minecraft.core.Direction.UP)) {
            return false;
        }

        int surfaceY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        boolean nearSurface = pos.getY() >= surfaceY - 2;
        boolean brightEnough = this.level().getMaxLocalRawBrightness(pos) >= 8;

        return nearSurface && brightEnough;
    }

    public boolean isSafeInterestSpot(BlockPos pos) {
        if (pos == null) {
            return false;
        }

        int surfaceY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        boolean nearSurface = pos.getY() >= surfaceY - 2;
        boolean brightEnough = this.level().getMaxLocalRawBrightness(pos.above()) >= 8;

        return nearSurface && brightEnough && !isDangerousMemorySpot(pos);
    }

    public BlockPos findNearbySafeSurfaceSpot(int radius) {
        BlockPos origin = this.blockPosition();
        BlockPos bestPos = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (int searchRadius : new int[] {radius, radius * 2}) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    BlockPos columnPos = new BlockPos(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                    int surfaceY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, columnPos.getX(), columnPos.getZ());
                    BlockPos candidate = new BlockPos(columnPos.getX(), surfaceY, columnPos.getZ());

                    if (!isSafeStandingSpot(candidate) || isDangerousMemorySpot(candidate)) {
                        continue;
                    }

                    double distanceSqr = candidate.distSqr(origin);
                    if (distanceSqr < bestDistanceSqr) {
                        bestDistanceSqr = distanceSqr;
                        bestPos = candidate.immutable();
                    }
                }
            }

            if (bestPos != null) {
                return bestPos;
            }
        }

        return bestPos;
    }

    public Vec3 getNearestSafeSpotCenter(int radius) {
        BlockPos pos = findNearbySafeSurfaceSpot(radius);
        return pos == null ? null : Vec3.atCenterOf(pos);
    }

    public void rememberDangerousSpot(BlockPos pos) {
        if (pos == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        this.dangerousSpotPos = pos.immutable();
        this.dangerousSpotUntilGameTime = serverLevel.getGameTime() + DANGEROUS_SPOT_MEMORY_DURATION;
    }

    public boolean hasDangerousSpotMemory() {
        return this.dangerousSpotPos != null
                && this.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameTime() < this.dangerousSpotUntilGameTime;
    }

    public boolean isDangerousMemorySpot(BlockPos pos) {
        if (pos == null || !hasDangerousSpotMemory()) {
            return false;
        }

        double dx = (this.dangerousSpotPos.getX() + 0.5D) - (pos.getX() + 0.5D);
        double dz = (this.dangerousSpotPos.getZ() + 0.5D) - (pos.getZ() + 0.5D);
        return (dx * dx + dz * dz) <= (DANGEROUS_SPOT_AVOID_RADIUS * DANGEROUS_SPOT_AVOID_RADIUS);
    }

    public BlockPos getDangerousSpotPos() {
        return hasDangerousSpotMemory() ? this.dangerousSpotPos : null;
    }

    public int pickQuietActivity() {
        PolenMood mood = getMood();
        if (mood == PolenMood.INSPIRED || mood == PolenMood.CURIOUS) {
            return this.getRandom().nextInt(3) == 0
                    ? QUIET_ACTIVITY_SINGING
                    : QUIET_ACTIVITY_DRAWING;
        }

        return this.getRandom().nextBoolean()
                ? QUIET_ACTIVITY_SINGING
                : QUIET_ACTIVITY_DRAWING;
    }

    public BlockPos getFavoriteFlowerPos() {
        return this.favoriteFlowerPos;
    }

    public BlockPos getFavoriteHivePos() {
        return this.favoriteHivePos;
    }

    public BlockPos getRestingPos() {
        return this.restingPos;
    }

    public String getQuietActivityName() {
        return switch (this.entityData.get(QUIET_ACTIVITY)) {
            case QUIET_ACTIVITY_SINGING -> "singing";
            case QUIET_ACTIVITY_DRAWING -> "drawing";
            default -> "none";
        };
    }

    public void tryAmbientDialogue(String situation) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (gameTime - this.lastAmbientDialogueGameTime < AMBIENT_DIALOGUE_COOLDOWN) {
            return;
        }

        boolean sentAny = false;
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(
                ServerPlayer.class,
                this.getBoundingBox().inflate(AMBIENT_DIALOGUE_RANGE)
        )) {
            player.displayClientMessage(
                    PolenDialogueManager.getAmbientDialogue(player, situation, this.getRandom()),
                    false
            );
            sentAny = true;
        }

        if (sentAny) {
            this.lastAmbientDialogueGameTime = gameTime;
        }
    }

    private void updateMood() {
        PolenMood nextMood;

        Player nearbyPlayer = this.level().getNearestPlayer(this, 2.5D);
        if (nearbyPlayer != null && !isComfortableWith(nearbyPlayer)) {
            nextMood = PolenMood.TIMID;
        } else if (this.level().isThundering()
                || this.level().isRaining() && this.level().canSeeSky(this.blockPosition())) {
            nextMood = PolenMood.UNSETTLED;
        } else if (isDoingQuietActivity()) {
            nextMood = PolenMood.INSPIRED;
        } else if (isNearRememberedInterest()) {
            nextMood = PolenMood.CURIOUS;
        } else {
            nextMood = PolenMood.CALM;
        }

        this.entityData.set(CURRENT_MOOD, nextMood.getId());
    }

    private boolean isNearRememberedInterest() {
        return this.favoriteFlowerPos != null && this.favoriteFlowerPos.closerToCenterThan(this.position(), 3.5D)
                || this.favoriteHivePos != null && this.favoriteHivePos.closerToCenterThan(this.position(), 3.5D);
    }

    private void seedMemoriesFromNearbyEnvironment() {
        if (this.level() instanceof ServerLevel serverLevel
                && this.dangerousSpotPos != null
                && serverLevel.getGameTime() >= this.dangerousSpotUntilGameTime) {
            this.dangerousSpotPos = null;
            this.dangerousSpotUntilGameTime = 0L;
        }

        if (this.restingPos == null) {
            this.restingPos = this.blockPosition().immutable();
        }

        if (this.favoriteFlowerPos != null && this.favoriteHivePos != null) {
            return;
        }

        BlockPos origin = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -2, -6), origin.offset(6, 2, 6))) {
            if (this.favoriteFlowerPos == null
                    && this.level().getBlockState(pos).is(net.minecraft.tags.BlockTags.FLOWERS)) {
                this.favoriteFlowerPos = pos.immutable();
            }

            if (this.favoriteHivePos == null
                    && (this.level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEE_NEST)
                    || this.level().getBlockState(pos).is(net.minecraft.world.level.block.Blocks.BEEHIVE))) {
                this.favoriteHivePos = pos.immutable();
            }

            if (this.favoriteFlowerPos != null && this.favoriteHivePos != null) {
                return;
            }
        }
    }

    private static void saveBlockPos(CompoundTag tag, String key, BlockPos pos) {
        if (pos == null) {
            return;
        }

        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        tag.put(key, posTag);
    }

    private static BlockPos loadBlockPos(CompoundTag tag, String key) {
        if (!tag.contains(key, CompoundTag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag posTag = tag.getCompound(key);
        return new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
    }

    private void tickQuietActivityParticles() {
        int quietActivity = this.entityData.get(QUIET_ACTIVITY);
        if (quietActivity == QUIET_ACTIVITY_NONE || this.tickCount % 12 != 0) {
            return;
        }

        double x = this.getX();
        double y = this.getEyeY() + 0.2D;
        double z = this.getZ();

        if (quietActivity == QUIET_ACTIVITY_SINGING) {
            this.level().addParticle(
                    ParticleTypes.NOTE,
                    x,
                    y + 0.2D,
                    z,
                    this.getRandom().nextDouble(),
                    0.0D,
                    0.0D
            );
            return;
        }

        if (quietActivity == QUIET_ACTIVITY_DRAWING) {
            double offsetX = (this.getRandom().nextDouble() - 0.5D) * 0.4D;
            double offsetZ = (this.getRandom().nextDouble() - 0.5D) * 0.4D;
            this.level().addParticle(
                    ParticleTypes.ENCHANT,
                    x + offsetX,
                    y - 0.5D,
                    z + offsetZ,
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }
}
