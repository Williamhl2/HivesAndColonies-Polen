package com.hivesandcolonies.hccharacters.character.polen.entity;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.action.PolenAutonomousActionPlan;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenAiFacade;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.PolenMovementHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.expression.gesture.PolenGesture;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.mood.PolenMood;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.state.PolenAiState;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskStatus;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenAffinityFactory;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import com.hivesandcolonies.hccharacters.integration.curios.PolenCuriosBridge;
import com.hivesandcolonies.hccharacters.character.polen.entity.equipment.PolenEquipmentInventory;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.world.PolenSingletonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.monster.Monster;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenThreatAssessmentHelper;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PolenEntity extends PathfinderMob {
    private static final String UNKNOWN_GIRL_KEY = "entity.hc_characters.unknown_girl";
    private static final String POLEN_KEY = "entity.hc_characters.polen";
    private static final String TAG_FAVORITE_FLOWER_POS = "FavoriteFlowerPos";
    private static final String TAG_FAVORITE_HIVE_POS = "FavoriteHivePos";
    private static final String TAG_FAVORITE_SOURCE_POS = "FavoriteSourcePos";
    private static final String TAG_RESTING_POS = "RestingPos";
    private static final String TAG_RESIDENCE_ANCHOR_POS = "ResidenceAnchorPos";
    private static final String TAG_RESIDENCE_USE_POS = "ResidenceUsePos";
    private static final String TAG_RESIDENCE_CONTEXT = "ResidenceContext";
    private static final String TAG_RESIDENCE_STAGE = "ResidenceStage";
    private static final String TAG_DANGEROUS_SPOT_POS = "DangerousSpotPos";
    private static final String TAG_DANGEROUS_SPOT_UNTIL = "DangerousSpotUntil";
    private static final String TAG_ACTIVE_LIGHT_POS = "ActiveLightPos";
    private static final String TAG_ACTIVE_LIGHT_UNTIL = "ActiveLightUntil";
    private static final String TAG_REQUESTED_HOME_UNTIL = "RequestedHomeUntil";
    private static final String TAG_NEEDS = "NeedState";
    private static final String TAG_INTENT = "IntentState";
    private static final String TAG_TRUST_WALK_PLAYER = "TrustWalkPlayer";
    private static final String TAG_TRUST_WALK_UNTIL = "TrustWalkUntil";
    private static final String TAG_EQUIPMENT = "PolenEquipment";
    private static final double DANGEROUS_SPOT_AVOID_RADIUS = 5.0D;
    private static final long MONSTER_EVADE_COOLDOWN_TICKS = 30L;
    private static final long MONSTER_DAMAGE_GRACE_TICKS = 20L;

    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> QUIET_ACTIVITY_TICKS =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_MOOD =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_GESTURE =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CURRENT_GESTURE_TICKS =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EQUIPPED_AFFINITY_CHARM =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_ASSIGNED_HOME =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRUST_WALK_ACTIVE =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> NEED_SAFETY =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEED_SOCIAL =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEED_CURIOSITY =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEED_REST =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> NEED_MAGIC =
            SynchedEntityData.defineId(PolenEntity.class, EntityDataSerializers.INT);

    private final PolenAiState aiState = new PolenAiState();
    private final PolenEquipmentInventory equipmentInventory = new PolenEquipmentInventory();
    private UUID trustWalkPlayerUuid;
    private long trustWalkUntilGameTime;
    private long monsterEvadeCooldownUntilGameTime;
    private long monsterDamageGraceUntilGameTime;
    private int navigationStepAssistCooldownTicks;

    public PolenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable(UNKNOWN_GIRL_KEY));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        PolenMovementHelper.configureNavigation(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(QUIET_ACTIVITY, 0);
        builder.define(QUIET_ACTIVITY_TICKS, 0);
        builder.define(CURRENT_MOOD, PolenMood.CALM.getId());
        builder.define(CURRENT_GESTURE, PolenGesture.IDLE.getId());
        builder.define(CURRENT_GESTURE_TICKS, 0);
        builder.define(EQUIPPED_AFFINITY_CHARM, PolenWorldAffinity.NONE.getId());
        builder.define(HAS_ASSIGNED_HOME, false);
        builder.define(TRUST_WALK_ACTIVE, false);
        builder.define(NEED_SAFETY, 24);
        builder.define(NEED_SOCIAL, 36);
        builder.define(NEED_CURIOSITY, 44);
        builder.define(NEED_REST, 28);
        builder.define(NEED_MAGIC, 24);
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
        if (source.getEntity() instanceof Player) {
            return false;
        }

        boolean projectileDamage = PolenThreatAssessmentHelper.isProjectileDamage(source);
        boolean rangedHostileSource = source.getEntity() instanceof Monster hostileSource
                && PolenThreatAssessmentHelper.isRangedHostile(hostileSource);
        int evadeSearchRadius = projectileDamage || rangedHostileSource ? 12 : 8;
        int evadeBlinkDistance = projectileDamage || rangedHostileSource ? 10 : 8;
        long evadeCooldown = projectileDamage || rangedHostileSource ? 40L : MONSTER_EVADE_COOLDOWN_TICKS;
        long damageGrace = projectileDamage || rangedHostileSource ? 28L : MONSTER_DAMAGE_GRACE_TICKS;

        if (this.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameTime() < this.monsterDamageGraceUntilGameTime) {
            return false;
        }

        BlockPos nearestThreatPos = projectileDamage || rangedHostileSource
                ? PolenThreatAssessmentHelper.findNearestVisibleRangedThreatPos(this, evadeSearchRadius + 2.0D)
                : com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator
                .getNearestHostileThreatPos(this, 8.0D);
        if (this.level() instanceof ServerLevel serverLevel
                && nearestThreatPos != null
                && serverLevel.getGameTime() >= this.monsterEvadeCooldownUntilGameTime
                && this.getHealth() > Math.max(4.0F, amount)
                && com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator
                .tryImmediateHostileBlink(this, evadeSearchRadius, evadeBlinkDistance)) {
            PolenDangerMemoryTracker.rememberDangerousSpot(this, nearestThreatPos);
            this.stopTrustWalk();
            this.stopQuietActivity();
            this.getNavigation().stop();
            this.monsterEvadeCooldownUntilGameTime = serverLevel.getGameTime() + evadeCooldown;
            this.monsterDamageGraceUntilGameTime = serverLevel.getGameTime() + damageGrace;
            this.invulnerableTime = projectileDamage || rangedHostileSource ? 14 : 10;
            return false;
        }

        if (source.getEntity() instanceof Monster hostile) {
            if (this.level() instanceof ServerLevel serverLevel
                    && serverLevel.getGameTime() >= this.monsterEvadeCooldownUntilGameTime
                    && this.getHealth() > Math.max(4.0F, amount)
                    && com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator
                    .tryImmediateHostileBlink(this, evadeSearchRadius, evadeBlinkDistance)) {
                PolenDangerMemoryTracker.rememberDangerousSpot(this, hostile.blockPosition());
                this.stopTrustWalk();
                this.stopQuietActivity();
                this.getNavigation().stop();
                this.monsterEvadeCooldownUntilGameTime = serverLevel.getGameTime() + evadeCooldown;
                this.monsterDamageGraceUntilGameTime = serverLevel.getGameTime() + damageGrace;
                this.invulnerableTime = projectileDamage || rangedHostileSource ? 14 : 10;
                return false;
            }

            PolenDangerMemoryTracker.rememberDangerousSpot(this, hostile.blockPosition());
            this.stopTrustWalk();
        } else if (nearestThreatPos != null) {
            PolenDangerMemoryTracker.rememberDangerousSpot(this, nearestThreatPos);
        } else {
            PolenDangerMemoryTracker.rememberDangerousSpot(this, this.blockPosition());
        }

        this.stopQuietActivity();
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            PolenAiFacade.tickClient(this);
            return;
        }

        if (!PolenSingletonManager.claimOrDiscardDuplicate(this)) {
            return;
        }

        this.ensureInitialAffinityCharm();
        if (this.tickCount % 100 == 0) {
            PolenCuriosBridge.syncAffinityCharmToCurios(this);
        }
        tickNavigationMobilityAssist();
        this.tickPolenFunctionalState();
        PolenAiFacade.tickServer(this);
    }

    @Override
    protected void registerGoals() {
        PolenAiFacade.registerGoals(this, this.goalSelector);
    }

    @Override
    public Direction getBedOrientation() {
        BlockPos sleepingPos = this.getSleepingPos().orElse(null);
        if (sleepingPos != null) {
            BlockState state = this.level().getBlockState(sleepingPos);
            if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
                return state.getValue(HorizontalDirectionalBlock.FACING);
            }
        }
        return super.getBedOrientation();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag equipmentTag = new CompoundTag();
        this.equipmentInventory.save(equipmentTag);
        tag.put(TAG_EQUIPMENT, equipmentTag);

        if (this.trustWalkPlayerUuid != null) {
            tag.putUUID(TAG_TRUST_WALK_PLAYER, this.trustWalkPlayerUuid);
            tag.putLong(TAG_TRUST_WALK_UNTIL, this.trustWalkUntilGameTime);
        }

        this.aiState.save(
                tag,
                TAG_FAVORITE_FLOWER_POS,
                TAG_FAVORITE_HIVE_POS,
                TAG_FAVORITE_SOURCE_POS,
                TAG_RESTING_POS,
                TAG_RESIDENCE_ANCHOR_POS,
                TAG_RESIDENCE_USE_POS,
                TAG_RESIDENCE_CONTEXT,
                TAG_RESIDENCE_STAGE,
                TAG_DANGEROUS_SPOT_POS,
                TAG_DANGEROUS_SPOT_UNTIL,
                TAG_ACTIVE_LIGHT_POS,
                TAG_ACTIVE_LIGHT_UNTIL,
                TAG_REQUESTED_HOME_UNTIL,
                TAG_NEEDS,
                TAG_INTENT
        );
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(TAG_EQUIPMENT)) {
            this.equipmentInventory.load(tag.getCompound(TAG_EQUIPMENT));
            this.syncEquipmentState();
        }

        if (tag.hasUUID(TAG_TRUST_WALK_PLAYER)) {
            this.trustWalkPlayerUuid = tag.getUUID(TAG_TRUST_WALK_PLAYER);
            this.trustWalkUntilGameTime = Math.max(0L, tag.getLong(TAG_TRUST_WALK_UNTIL));
        }

        this.aiState.load(
                tag,
                TAG_FAVORITE_FLOWER_POS,
                TAG_FAVORITE_HIVE_POS,
                TAG_FAVORITE_SOURCE_POS,
                TAG_RESTING_POS,
                TAG_RESIDENCE_ANCHOR_POS,
                TAG_RESIDENCE_USE_POS,
                TAG_RESIDENCE_CONTEXT,
                TAG_RESIDENCE_STAGE,
                TAG_DANGEROUS_SPOT_POS,
                TAG_DANGEROUS_SPOT_UNTIL,
                TAG_ACTIVE_LIGHT_POS,
                TAG_ACTIVE_LIGHT_UNTIL,
                TAG_REQUESTED_HOME_UNTIL,
                TAG_NEEDS,
                TAG_INTENT
        );
        this.syncHomeState();
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

    public PolenAutonomousActionPlan pickQuietActionPlan() {
        return PolenAiFacade.pickQuietActionPlan(this);
    }


    public PolenEquipmentInventory getPolenEquipmentInventory() {
        return this.equipmentInventory;
    }

    public PolenWorldAffinity getEquippedAffinityCharm() {
        return PolenWorldAffinity.fromId(this.entityData.get(EQUIPPED_AFFINITY_CHARM));
    }

    public void equipAffinityCharm(PolenWorldAffinity affinity) {
        PolenWorldAffinity safeAffinity = affinity == null ? PolenWorldAffinity.NONE : affinity;
        this.equipmentInventory.setAffinityCharm(safeAffinity);
        this.entityData.set(EQUIPPED_AFFINITY_CHARM, safeAffinity.getId());
        PolenCuriosBridge.syncAffinityCharmToCurios(this);
    }

    public void ensureInitialAffinityCharm() {
        if (this.getEquippedAffinityCharm() != PolenWorldAffinity.NONE) {
            return;
        }

        if (this.equipmentInventory.hasAffinityCharm()) {
            this.syncEquipmentState();
            return;
        }

        this.equipAffinityCharm(PolenAffinityFactory.createInitialAffinity(this));
    }

    private void syncEquipmentState() {
        this.entityData.set(EQUIPPED_AFFINITY_CHARM, this.equipmentInventory.getAffinityCharm().getId());
        PolenCuriosBridge.syncAffinityCharmToCurios(this);
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

    public PolenTaskType getCurrentTask() {
        return this.aiState.getTaskState().getCurrentTask();
    }

    public PolenTaskStatus getCurrentTaskStatus() {
        return this.aiState.getTaskState().getStatus();
    }

    public String getCurrentTaskReason() {
        return this.aiState.getTaskState().getReason();
    }

    public String getCurrentTaskNote() {
        return this.aiState.getTaskState().getNote();
    }

    public void setMood(PolenMood mood) {
        this.entityData.set(CURRENT_MOOD, mood.getId());
    }

    public PolenGesture getGesture() {
        return PolenGesture.fromId(this.entityData.get(CURRENT_GESTURE));
    }

    public int getGestureTicks() {
        return this.entityData.get(CURRENT_GESTURE_TICKS);
    }

    public void setGestureState(PolenGesture gesture, int ticks) {
        this.entityData.set(CURRENT_GESTURE, gesture.getId());
        this.entityData.set(CURRENT_GESTURE_TICKS, ticks);
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

    public void rememberResidence(BlockPos pos) {
        PolenAiFacade.rememberResidence(this, pos);
        this.syncHomeState();
    }


    public boolean hasAssignedHome() {
        return this.entityData.get(HAS_ASSIGNED_HOME);
    }

    public boolean isTrustWalkSyncedActive() {
        return this.entityData.get(TRUST_WALK_ACTIVE);
    }

    public int getProfileNeedSafety() {
        return this.entityData.get(NEED_SAFETY);
    }

    public int getProfileNeedSocial() {
        return this.entityData.get(NEED_SOCIAL);
    }

    public int getProfileNeedCuriosity() {
        return this.entityData.get(NEED_CURIOSITY);
    }

    public int getProfileNeedRest() {
        return this.entityData.get(NEED_REST);
    }

    public int getProfileNeedMagic() {
        return this.entityData.get(NEED_MAGIC);
    }

    public void syncHomeState() {
        this.syncProfileState();
    }

    public void syncProfileState() {
        this.entityData.set(HAS_ASSIGNED_HOME, PolenHomeManager.hasValidRememberedResidence(this));
        this.entityData.set(TRUST_WALK_ACTIVE, this.trustWalkPlayerUuid != null && this.level().getGameTime() < this.trustWalkUntilGameTime);
        this.entityData.set(NEED_SAFETY, this.aiState.getNeedState().safety());
        this.entityData.set(NEED_SOCIAL, this.aiState.getNeedState().social());
        this.entityData.set(NEED_CURIOSITY, this.aiState.getNeedState().curiosity());
        this.entityData.set(NEED_REST, this.aiState.getNeedState().rest());
        this.entityData.set(NEED_MAGIC, this.aiState.getNeedState().magic());
    }

    public boolean isTrustWalkActive() {
        return this.trustWalkPlayerUuid != null && this.level().getGameTime() < this.trustWalkUntilGameTime;
    }

    public boolean hasPendingReturnHomeRequest() {
        return this.aiState.hasPendingHomeRequest(this.level().getGameTime());
    }

    public void requestReturnHome(int durationTicks) {
        if (durationTicks <= 0) {
            return;
        }
        this.aiState.requestReturnHomeUntil(this.level().getGameTime() + durationTicks);
        this.stopQuietActivity();
        this.stopTrustWalk();
    }

    public void clearReturnHomeRequest() {
        this.aiState.clearReturnHomeRequest();
    }

    public void performNavigationStepAssist(Vec3 pushVector) {
        this.jumpFromGround();
        if (pushVector != null && pushVector.lengthSqr() > 0.0001D) {
            this.setDeltaMovement(this.getDeltaMovement().add(pushVector.x, 0.0D, pushVector.z));
        }
    }

    public ServerPlayer getTrustWalkPlayer() {
        if (!(this.level() instanceof ServerLevel serverLevel) || this.trustWalkPlayerUuid == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(this.trustWalkPlayerUuid);
    }

    public void startTrustWalk(ServerPlayer player, int durationTicks) {
        if (player == null || durationTicks <= 0) {
            return;
        }
        this.trustWalkPlayerUuid = player.getUUID();
        this.trustWalkUntilGameTime = this.level().getGameTime() + durationTicks;
        this.entityData.set(TRUST_WALK_ACTIVE, true);
        this.stopQuietActivity();
    }

    public void stopTrustWalk() {
        this.trustWalkPlayerUuid = null;
        this.trustWalkUntilGameTime = 0L;
        this.entityData.set(TRUST_WALK_ACTIVE, false);
        this.getNavigation().stop();
    }

    private void tickPolenFunctionalState() {
        if (this.tickCount % 20 == 0) {
            PolenHomeManager.clearInvalidResidence(this);
            if (this.tickCount % 40 == 0 && !this.isSleeping()) {
                PolenHomeManager.tryAutoAssignNearbyBeeBed(this);
            }
            if (this.hasPendingReturnHomeRequest()
                    && (!PolenHomeManager.hasHomeCenter(this) || PolenHomeManager.isNearHomeCenter(this, 3.0D))) {
                this.clearReturnHomeRequest();
            }
            this.syncProfileState();
        }
        if (this.tickCount % 200 == 0 && this.level() instanceof ServerLevel serverLevel) {
            PolenWorldStateManager.rememberLastKnownPosition(serverLevel, this.blockPosition());
        }

        if (this.trustWalkPlayerUuid != null && this.level().getGameTime() >= this.trustWalkUntilGameTime) {
            this.stopTrustWalk();
        }
    }

    private void tickNavigationMobilityAssist() {
        if (this.navigationStepAssistCooldownTicks > 0) {
            this.navigationStepAssistCooldownTicks--;
            return;
        }

        if (PolenMovementHelper.tryAssistStepUp(this)) {
            this.navigationStepAssistCooldownTicks = 8;
        }
    }

    double getDangerousSpotAvoidRadius() {
        return DANGEROUS_SPOT_AVOID_RADIUS;
    }
}
