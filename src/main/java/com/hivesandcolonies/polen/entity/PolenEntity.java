package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.routine.PolenRoutinePlanner;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;
import com.hivesandcolonies.polen.entity.ai.goal.PolenCuriousInterestGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenIdleHobbyGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenKeepDistanceGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenRoutineGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenSafeStrollGoal;
import com.hivesandcolonies.polen.entity.ai.goal.PolenSeekSafetyGoal;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodController;
import com.hivesandcolonies.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.polen.story.PolenStoryEventManager;
import com.hivesandcolonies.polen.util.PolenNbtHelper;
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
        builder.define(QUIET_ACTIVITY, PolenQuietActivityController.QUIET_ACTIVITY_NONE);
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
            PolenQuietActivityController.tickClientParticles(this);
            return;
        }

        if (this.tickCount % 20 == 0) {
            this.updateDisplayName();
            this.updateMood();
        }

        if (this.tickCount % 100 == 0) {
            PolenMemoryHandler.seedMemoriesFromNearbyEnvironment(this);
        }

        PolenQuietActivityController.tickServer(this);
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
        PolenNbtHelper.saveBlockPos(tag, TAG_FAVORITE_FLOWER_POS, this.favoriteFlowerPos);
        PolenNbtHelper.saveBlockPos(tag, TAG_FAVORITE_HIVE_POS, this.favoriteHivePos);
        PolenNbtHelper.saveBlockPos(tag, TAG_RESTING_POS, this.restingPos);
        PolenNbtHelper.saveBlockPos(tag, TAG_DANGEROUS_SPOT_POS, this.dangerousSpotPos);
        tag.putLong(TAG_DANGEROUS_SPOT_UNTIL, this.dangerousSpotUntilGameTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.favoriteFlowerPos = PolenNbtHelper.loadBlockPos(tag, TAG_FAVORITE_FLOWER_POS);
        this.favoriteHivePos = PolenNbtHelper.loadBlockPos(tag, TAG_FAVORITE_HIVE_POS);
        this.restingPos = PolenNbtHelper.loadBlockPos(tag, TAG_RESTING_POS);
        this.dangerousSpotPos = PolenNbtHelper.loadBlockPos(tag, TAG_DANGEROUS_SPOT_POS);
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
        return PolenQuietActivityController.isDoingQuietActivity(this);
    }

    public void startQuietActivity(int activityType, int ticks) {
        PolenQuietActivityController.startQuietActivity(this, activityType, ticks);
    }

    public void stopQuietActivity() {
        PolenQuietActivityController.stopQuietActivity(this);
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
        return PolenRoutinePlanner.getRoutineTarget(this);
    }

    public boolean isRememberedSpotStillValid(BlockPos pos) {
        return PolenRoutinePlanner.isRememberedSpotStillValid(this, pos);
    }

    public boolean isInUnsafeArea() {
        return PolenSafetyNavigator.isInUnsafeArea(this);
    }

    public boolean shouldSeekSafety() {
        return PolenSafetyNavigator.shouldSeekSafety(this);
    }

    public boolean shouldUseUnsafeDialogue() {
        return PolenSafetyNavigator.shouldUseUnsafeDialogue(this);
    }

    public boolean isSafeStandingSpot(BlockPos pos) {
        return PolenSafetyEvaluator.isSafeStandingSpot(this, pos);
    }

    public boolean isSafeInterestSpot(BlockPos pos) {
        return PolenRoutinePlanner.isSafeInterestSpot(this, pos);
    }

    public BlockPos findNearbySafeSurfaceSpot(int radius) {
        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(this, radius);
    }

    public Vec3 getNearestSafeSpotCenter(int radius) {
        return PolenSafetyNavigator.getNearestSafeSpotCenter(this, radius);
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
        return PolenSafetyEvaluator.isDangerousMemorySpot(
                this.dangerousSpotPos,
                pos,
                DANGEROUS_SPOT_AVOID_RADIUS
        );
    }

    public BlockPos getDangerousSpotPos() {
        return hasDangerousSpotMemory() ? this.dangerousSpotPos : null;
    }

    public int pickQuietActivity() {
        return PolenQuietActivityController.pickQuietActivity(this);
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
        return PolenQuietActivityController.getQuietActivityName(this);
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
        this.entityData.set(CURRENT_MOOD, PolenMoodController.calculateMood(this).getId());
    }

    public void setFavoriteFlowerPos(BlockPos pos) {
        this.favoriteFlowerPos = pos;
    }

    public void setFavoriteHivePos(BlockPos pos) {
        this.favoriteHivePos = pos;
    }

    public void setRestingPos(BlockPos pos) {
        this.restingPos = pos;
    }

    public int getQuietActivityType() {
        return this.entityData.get(QUIET_ACTIVITY);
    }

    public int getQuietActivityTicks() {
        return this.entityData.get(QUIET_ACTIVITY_TICKS);
    }

    // The controller owns quiet activity behavior, but synced state still lives on the entity.
    public void setQuietActivityState(int activityType, int ticks) {
        this.entityData.set(QUIET_ACTIVITY, activityType);
        this.entityData.set(QUIET_ACTIVITY_TICKS, ticks);
    }

    public void rememberInterestingSpot(BlockPos pos) {
        PolenMemoryHandler.rememberInterestingSpot(this, pos);
    }

    public void rememberRestingSpot(BlockPos pos) {
        PolenMemoryHandler.rememberRestingSpot(this, pos);
    }

}
