package com.hivesandcolonies.hccharacters.character.lucy.entity;

import java.util.EnumSet;

import com.hivesandcolonies.hccharacters.character.lucy.world.LucyVillageEncounterManager;
import com.hivesandcolonies.hccharacters.common.entity.SimpleCharacterEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LucyEntity extends SimpleCharacterEntity {
    private static final String LUCY_KEY = "entity.hc_characters.lucy";
    private static final String TAG_SCENE_ANCHOR = "LucySceneAnchor";
    private static final String TAG_RESTLESS_MOVE_COOLDOWN = "LucyRestlessMoveCooldown";
    private static final String TAG_RESTLESS_TURN_COOLDOWN = "LucyRestlessTurnCooldown";
    private static final int RETURN_DISTANCE_SQR = 7 * 7;
    private static final int RESTLESS_MOVE_RADIUS = 3;

    private BlockPos sceneAnchor;
    private int restlessMoveCooldown;
    private int restlessTurnCooldown;

    public LucyEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable(LUCY_KEY));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimpleCharacterEntity.createAttributes();
    }

    public void startVillageScene(BlockPos anchor) {
        this.sceneAnchor = anchor == null ? null : anchor.immutable();
        if (this.restlessMoveCooldown <= 0) {
            this.restlessMoveCooldown = 10 + this.getRandom().nextInt(20);
        }
        if (this.restlessTurnCooldown <= 0) {
            this.restlessTurnCooldown = 8 + this.getRandom().nextInt(16);
        }
    }

    public BlockPos getSceneAnchor() {
        return this.sceneAnchor;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new StayNearSceneAnchorGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            LucyVillageEncounterManager.handleInteraction(this, player);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.sceneAnchor == null) {
            return;
        }

        if (this.restlessMoveCooldown > 0) {
            --this.restlessMoveCooldown;
        }
        if (this.restlessTurnCooldown > 0) {
            --this.restlessTurnCooldown;
        }

        if (this.distanceToSqr(Vec3.atCenterOf(this.sceneAnchor)) > RETURN_DISTANCE_SQR
                && this.getNavigation().isDone()) {
            this.getNavigation().moveTo(
                    this.sceneAnchor.getX() + 0.5D,
                    this.sceneAnchor.getY(),
                    this.sceneAnchor.getZ() + 0.5D,
                    0.85D
            );
            this.restlessMoveCooldown = 12 + this.getRandom().nextInt(20);
            return;
        }

        if (this.restlessTurnCooldown <= 0) {
            float yaw = this.getRandom().nextFloat() * 360.0F;
            this.setYRot(yaw);
            this.setYHeadRot(yaw);
            this.setYBodyRot(yaw);
            this.restlessTurnCooldown = 8 + this.getRandom().nextInt(20);
        }

        if (this.restlessMoveCooldown <= 0 && this.getNavigation().isDone()) {
            BlockPos restlessSpot = this.findRestlessSpot();
            if (restlessSpot != null) {
                this.getNavigation().moveTo(
                        restlessSpot.getX() + 0.5D,
                        restlessSpot.getY(),
                        restlessSpot.getZ() + 0.5D,
                        1.0D
                );
            }
            this.restlessMoveCooldown = 18 + this.getRandom().nextInt(32);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.sceneAnchor != null) {
            compound.putLong(TAG_SCENE_ANCHOR, this.sceneAnchor.asLong());
        }
        compound.putInt(TAG_RESTLESS_MOVE_COOLDOWN, this.restlessMoveCooldown);
        compound.putInt(TAG_RESTLESS_TURN_COOLDOWN, this.restlessTurnCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.sceneAnchor = compound.contains(TAG_SCENE_ANCHOR)
                ? BlockPos.of(compound.getLong(TAG_SCENE_ANCHOR))
                : null;
        this.restlessMoveCooldown = compound.getInt(TAG_RESTLESS_MOVE_COOLDOWN);
        this.restlessTurnCooldown = compound.getInt(TAG_RESTLESS_TURN_COOLDOWN);
    }

    private BlockPos findRestlessSpot() {
        if (this.sceneAnchor == null || !(this.level() instanceof Level level)) {
            return null;
        }

        for (int attempt = 0; attempt < 10; attempt++) {
            int x = this.getRandom().nextInt(RESTLESS_MOVE_RADIUS * 2 + 1) - RESTLESS_MOVE_RADIUS;
            int z = this.getRandom().nextInt(RESTLESS_MOVE_RADIUS * 2 + 1) - RESTLESS_MOVE_RADIUS;
            BlockPos candidate = this.sceneAnchor.offset(x, 0, z);
            if (candidate.closerToCenterThan(Vec3.atCenterOf(this.sceneAnchor), 1.1D)) {
                continue;
            }
            if (canStandAt(level, candidate)) {
                return candidate.immutable();
            }
        }
        return null;
    }

    private static boolean canStandAt(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir()
                && level.getBlockState(pos.above()).isAir()
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }

    private static final class StayNearSceneAnchorGoal extends Goal {
        private final LucyEntity lucy;

        private StayNearSceneAnchorGoal(LucyEntity lucy) {
            this.lucy = lucy;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BlockPos anchor = this.lucy.getSceneAnchor();
            return anchor != null
                    && this.lucy.distanceToSqr(Vec3.atCenterOf(anchor)) > RETURN_DISTANCE_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos anchor = this.lucy.getSceneAnchor();
            return anchor != null
                    && this.lucy.distanceToSqr(Vec3.atCenterOf(anchor)) > 4.0D * 4.0D;
        }

        @Override
        public void tick() {
            BlockPos anchor = this.lucy.getSceneAnchor();
            if (anchor != null && this.lucy.getNavigation().isDone()) {
                this.lucy.getNavigation().moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D, 0.85D);
            }
        }
    }
}
