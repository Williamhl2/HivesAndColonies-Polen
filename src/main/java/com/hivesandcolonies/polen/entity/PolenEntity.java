package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.activity.PolenQuietActivityController;
import com.hivesandcolonies.polen.entity.ai.memory.PolenMemoryHandler;
import com.hivesandcolonies.polen.entity.ai.mood.PolenMoodController;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.util.PolenNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class PolenEntity extends PathfinderMob {
    private static final String UNKNOWN_GIRL_KEY = "entity.polen.unknown_girl";
    private static final String POLEN_KEY = "entity.polen.polen";
    private static final String TAG_FAVORITE_FLOWER_POS = "FavoriteFlowerPos";
    private static final String TAG_FAVORITE_HIVE_POS = "FavoriteHivePos";
    private static final String TAG_RESTING_POS = "RestingPos";
    private static final String TAG_DANGEROUS_SPOT_POS = "DangerousSpotPos";
    private static final String TAG_DANGEROUS_SPOT_UNTIL = "DangerousSpotUntil";
    private static final double DANGEROUS_SPOT_AVOID_RADIUS = 5.0D;

    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY_TICKS =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_MOOD =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);

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

    void refreshDisplayName() {
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
            this.refreshDisplayName();
            this.updateMood();
        }

        if (this.tickCount % 100 == 0) {
            PolenMemoryHandler.seedMemoriesFromNearbyEnvironment(this);
        }

        PolenQuietActivityController.tickServer(this);
    }

    @Override
    protected void registerGoals() {
        PolenGoalRegistry.register(this, this.goalSelector);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        PolenNbtHelper.saveBlockPos(tag, TAG_FAVORITE_FLOWER_POS, this.favoriteFlowerPos);
        PolenNbtHelper.saveBlockPos(tag, TAG_FAVORITE_HIVE_POS, this.favoriteHivePos);
        PolenNbtHelper.saveBlockPos(tag, TAG_RESTING_POS, this.restingPos);
        PolenDangerMemoryTracker.save(this, tag, TAG_DANGEROUS_SPOT_POS, TAG_DANGEROUS_SPOT_UNTIL);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.favoriteFlowerPos = PolenNbtHelper.loadBlockPos(tag, TAG_FAVORITE_FLOWER_POS);
        this.favoriteHivePos = PolenNbtHelper.loadBlockPos(tag, TAG_FAVORITE_HIVE_POS);
        this.restingPos = PolenNbtHelper.loadBlockPos(tag, TAG_RESTING_POS);
        PolenDangerMemoryTracker.load(this, tag, TAG_DANGEROUS_SPOT_POS, TAG_DANGEROUS_SPOT_UNTIL);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return PolenInteractionController.handleMobInteract(this, player, hand);
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

    public BlockPos getDangerousSpotPos() {
        return PolenDangerMemoryTracker.getActiveDangerousSpotPos(this);
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

    long getLastAmbientDialogueGameTime() {
        return this.lastAmbientDialogueGameTime;
    }

    void setLastAmbientDialogueGameTime(long gameTime) {
        this.lastAmbientDialogueGameTime = gameTime;
    }

    BlockPos getDangerousSpotPosRaw() {
        return this.dangerousSpotPos;
    }

    long getDangerousSpotUntilGameTime() {
        return this.dangerousSpotUntilGameTime;
    }

    void setDangerousSpotState(BlockPos pos, long untilGameTime) {
        this.dangerousSpotPos = pos;
        this.dangerousSpotUntilGameTime = untilGameTime;
    }

    double getDangerousSpotAvoidRadius() {
        return DANGEROUS_SPOT_AVOID_RADIUS;
    }
}
