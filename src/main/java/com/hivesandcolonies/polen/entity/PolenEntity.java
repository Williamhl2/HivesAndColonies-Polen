package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.entity.ai.PolenAiFacade;
import com.hivesandcolonies.polen.entity.ai.PolenMood;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.state.PolenAiState;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
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
    private static final String TAG_FAVORITE_SOURCE_POS = "FavoriteSourcePos";
    private static final String TAG_RESTING_POS = "RestingPos";
    private static final String TAG_DANGEROUS_SPOT_POS = "DangerousSpotPos";
    private static final String TAG_DANGEROUS_SPOT_UNTIL = "DangerousSpotUntil";
    private static final String TAG_NEEDS = "NeedState";
    private static final String TAG_INTENT = "IntentState";
    private static final double DANGEROUS_SPOT_AVOID_RADIUS = 5.0D;

    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY_TICKS =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_MOOD =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);

    private final PolenAiState aiState = new PolenAiState();

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
        builder.define(QUIET_ACTIVITY, 0);
        builder.define(QUIET_ACTIVITY_TICKS, 0);
        builder.define(CURRENT_MOOD, PolenMood.CALM.getId());
    }

    public void refreshDisplayName() {
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
            PolenAiFacade.tickClient(this);
            return;
        }

        PolenAiFacade.tickServer(this);
    }

    @Override
    protected void registerGoals() {
        PolenAiFacade.registerGoals(this, this.goalSelector);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.aiState.save(
                tag,
                TAG_FAVORITE_FLOWER_POS,
                TAG_FAVORITE_HIVE_POS,
                TAG_FAVORITE_SOURCE_POS,
                TAG_RESTING_POS,
                TAG_DANGEROUS_SPOT_POS,
                TAG_DANGEROUS_SPOT_UNTIL,
                TAG_NEEDS,
                TAG_INTENT
        );
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.aiState.load(
                tag,
                TAG_FAVORITE_FLOWER_POS,
                TAG_FAVORITE_HIVE_POS,
                TAG_FAVORITE_SOURCE_POS,
                TAG_RESTING_POS,
                TAG_DANGEROUS_SPOT_POS,
                TAG_DANGEROUS_SPOT_UNTIL,
                TAG_NEEDS,
                TAG_INTENT
        );
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        return PolenInteractionController.handleMobInteract(this, player, hand);
    }

    public boolean isDoingQuietActivity() {
        return PolenAiFacade.isDoingQuietActivity(this);
    }

    public void startQuietActivity(int activityType, int ticks) {
        PolenAiFacade.startQuietActivity(this, activityType, ticks);
    }

    public void stopQuietActivity() {
        PolenAiFacade.stopQuietActivity(this);
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

    public PolenAiState getAiState() {
        return this.aiState;
    }

    public int pickQuietActivity() {
        return PolenAiFacade.pickQuietActivity(this);
    }

    public String getQuietActivityName() {
        return PolenAiFacade.getQuietActivityName(this);
    }

    public PolenIntent getCurrentIntent() {
        return this.aiState.getIntentState().currentIntent();
    }

    public String getCurrentIntentReason() {
        return this.aiState.getIntentState().currentReason();
    }

    public void setMood(PolenMood mood) {
        this.entityData.set(CURRENT_MOOD, mood.getId());
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
        PolenAiFacade.rememberInterestingSpot(this, pos);
    }

    public void rememberRestingSpot(BlockPos pos) {
        PolenAiFacade.rememberRestingSpot(this, pos);
    }

    double getDangerousSpotAvoidRadius() {
        return DANGEROUS_SPOT_AVOID_RADIUS;
    }
}
