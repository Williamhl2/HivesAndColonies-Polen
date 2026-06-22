package com.hivesandcolonies.hccharacters.character.soa.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.character.soa.dialogue.SoaMarjorieDialogue;
import com.hivesandcolonies.hccharacters.character.soa.progression.SoaMarjorieRelationship;
import com.hivesandcolonies.hccharacters.common.entity.SimpleCharacterEntity;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipInteraction;
import com.hivesandcolonies.hccharacters.common.util.LevelBrightnessHelper;
import com.hivesandcolonies.hccharacters.integration.curios.PolenCuriosBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SoaMarjorieEntity extends SimpleCharacterEntity {
    private static final String SOA_KEY = "entity.hc_characters.soa_marjorie";
    private static final int DEFENSE_DRAW_TICKS = 120;
    private static final int BOARD_RETURN_DISTANCE_SQR = 14 * 14;
    private static final int BOARD_HARD_LIMIT_DISTANCE_SQR = 28 * 28;
    private static final int CAVE_RETURN_DISTANCE_SQR = 32 * 32;
    private static final int CAVE_HARD_LIMIT_DISTANCE_SQR = 52 * 52;
    private static final int BOARD_GIFT_MIN_DELAY = 20 * 30;
    private static final int BOARD_GIFT_RANDOM_DELAY = 20 * 50;
    private static final int HOSTILE_SCAN_INTERVAL_TICKS = 30;
    private static final double HOSTILE_SCAN_RADIUS = 10.0D;
    private static final String MARTA_TAG = "hc_characters_soa_marta";
    private static final String MARTA_OWNER_TAG = "SoaMarjorieOwner";
    private static final String MARTA_COMPANION_UUID_TAG = "SoaMarjorieMartaUuid";
    private static final double MARTA_FOLLOW_DISTANCE_SQR = 7.0D * 7.0D;
    private static final double MARTA_TELEPORT_DISTANCE_SQR = 36.0D * 36.0D;

    public static final String CURIOS_BACKPACK_SLOT = "backpack";
    public static final String CURIOS_TOOL_RIGHT_SLOT = "tool_right";
    public static final String CURIOS_TOOL_LEFT_SLOT = "tool_left";

    private static final ResourceLocation SOPHISTICATED_BACKPACK = ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "backpack");
    private static final TagKey<Block> SOA_MINEABLE_ORES = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(HcCharacters.MODID, "soa_marjorie_mineable")
    );
    private static final float PLAYER_WARNING_STRIKE_DAMAGE = 38.0F;

    private EncounterMode encounterMode = EncounterMode.NONE;
    private BlockPos encounterAnchor;
    private int encounterTicksLeft;
    private int boardGiftDelayTicks;
    private boolean boardGiftGiven;

    private int defenseDrawTicks;
    private int miningDrawTicks;
    private int torchDrawTicks;
    private int ambientDialogueCooldown;
    private int equipmentSyncCooldown;
    private int hostileScanCooldown;
    private UUID martaUuid;

    public SoaMarjorieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable(SOA_KEY));
        this.setCustomNameVisible(true);
        this.equipExpertMiningTools();
    }

    public void startBoardVisit(BlockPos boardPos, int durationTicks) {
        this.startBoardVisit(boardPos, durationTicks, null);
    }

    public void startBoardVisit(BlockPos boardPos, int durationTicks, ServerPlayer witness) {
        this.encounterMode = EncounterMode.BOARD_VISIT;
        this.encounterAnchor = boardPos.immutable();
        this.encounterTicksLeft = Math.max(20 * 30, durationTicks);
        this.boardGiftDelayTicks = BOARD_GIFT_MIN_DELAY + this.getRandom().nextInt(BOARD_GIFT_RANDOM_DELAY + 1);
        this.boardGiftGiven = false;
        this.ambientDialogueCooldown = 20 + this.getRandom().nextInt(80);
        if (witness != null) {
            SoaMarjorieRelationship.recordBoardVisit(witness);
            this.sayTo(witness, SoaMarjorieRelationship.arrivalBoardLine(witness));
        } else {
            this.sayNearby("dialogue.soa.marjorie.nearby.board_spawn");
        }
    }

    public void startCaveMiningEncounter(int durationTicks) {
        this.startCaveMiningEncounter(durationTicks, null);
    }

    public void startCaveMiningEncounter(int durationTicks, ServerPlayer witness) {
        this.encounterMode = EncounterMode.CAVE_MINING;
        this.encounterAnchor = this.blockPosition().immutable();
        this.encounterTicksLeft = Math.max(20 * 45, durationTicks);
        this.boardGiftDelayTicks = 0;
        this.boardGiftGiven = true;
        this.ambientDialogueCooldown = 20 + this.getRandom().nextInt(80);
        if (witness != null) {
            SoaMarjorieRelationship.recordCaveEncounter(witness);
            this.sayTo(witness, SoaMarjorieRelationship.arrivalCaveLine(witness));
        } else {
            this.sayNearby("dialogue.soa.marjorie.nearby.cave_spawn");
        }
    }

    public boolean isEncounterActive() {
        return this.encounterMode != EncounterMode.NONE && this.encounterTicksLeft > 0;
    }

    public boolean isBoardVisit() {
        return this.encounterMode == EncounterMode.BOARD_VISIT && this.encounterTicksLeft > 0;
    }

    public boolean isCaveMiningEncounter() {
        return this.encounterMode == EncounterMode.CAVE_MINING && this.encounterTicksLeft > 0;
    }

    public BlockPos getEncounterAnchor() {
        return this.encounterAnchor;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            NpcRelationshipInteraction.interact(
                    this,
                    player,
                    SoaMarjorieDialogue.PROFILE_ID,
                    SoaMarjorieDialogue.SPEAKER,
                    SoaMarjorieDialogue.TIER_0,
                    SoaMarjorieDialogue.TIER_1,
                    SoaMarjorieDialogue.TIER_2,
                    SoaMarjorieDialogue.TIER_3,
                    SoaMarjorieDialogue.rewardPool(),
                    SoaMarjorieRelationship.RANK_RESOLVER,
                    "relationship.soa.reason.interaction"
            );
            if (this.isBoardVisit() && !this.boardGiftGiven) {
                this.tryGiveBoardGift(player);
            }
            this.getLookControl().setLookAt(player, 30.0F, 30.0F);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    private void equipExpertMiningTools() {
        if (this.defenseDrawTicks > 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            return;
        }
        if (this.miningDrawTicks > 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_PICKAXE));
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
            return;
        }
        if (this.torchDrawTicks > 0) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.TORCH));
            return;
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    public boolean isDefenseAxeDrawn() {
        return this.defenseDrawTicks > 0;
    }

    public boolean isMiningPickaxeDrawn() {
        return this.miningDrawTicks > 0;
    }

    public void drawMiningPickaxeBriefly() {
        if (this.defenseDrawTicks <= 0) {
            this.miningDrawTicks = Math.max(this.miningDrawTicks, 40);
        }
    }

    public void drawTorchBriefly() {
        if (this.defenseDrawTicks <= 0 && this.miningDrawTicks <= 0) {
            this.torchDrawTicks = Math.max(this.torchDrawTicks, 30);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.defenseDrawTicks > 0) {
            --this.defenseDrawTicks;
        }
        if (this.miningDrawTicks > 0) {
            --this.miningDrawTicks;
        }
        if (this.torchDrawTicks > 0) {
            --this.torchDrawTicks;
        }
        if (!this.level().isClientSide) {
            this.tickMartaCompanion();
            this.tickEncounterLifetime();
            this.tickSurvivabilityAndThreats();
            this.equipExpertMiningTools();
            this.syncMiningEquipmentToCurios();
        }
        this.tickAmbientDialogue();
    }

    private void tickMartaCompanion() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isAlive()) {
            return;
        }
        Allay marta = this.getOrCreateMarta(serverLevel);
        if (marta == null || !marta.isAlive()) {
            return;
        }
        this.keepMartaUntamperable(marta);
        double distanceSqr = marta.distanceToSqr(this);
        if (distanceSqr > MARTA_TELEPORT_DISTANCE_SQR) {
            marta.teleportTo(this.getX() + 1.0D, this.getY() + 0.5D, this.getZ() + 1.0D);
            marta.getNavigation().stop();
            return;
        }
        if (distanceSqr > MARTA_FOLLOW_DISTANCE_SQR && marta.getNavigation().isDone()) {
            marta.getNavigation().moveTo(this, 0.55D);
        }
    }

    private Allay getOrCreateMarta(ServerLevel serverLevel) {
        Allay existing = this.findMarta(serverLevel);
        if (existing != null) {
            return existing;
        }
        Allay marta = EntityType.ALLAY.create(serverLevel);
        if (marta == null) {
            return null;
        }
        marta.moveTo(this.getX() + 1.0D, this.getY() + 0.25D, this.getZ() + 1.0D, this.getYRot(), 0.0F);
        this.keepMartaUntamperable(marta);
        if (serverLevel.addFreshEntity(marta)) {
            this.martaUuid = marta.getUUID();
            return marta;
        }
        return null;
    }

    private Allay findMarta(ServerLevel serverLevel) {
        if (this.martaUuid != null && serverLevel.getEntity(this.martaUuid) instanceof Allay marta && this.isMarta(marta)) {
            return marta;
        }
        AABB searchArea = this.getBoundingBox().inflate(48.0D);
        List<Allay> candidates = serverLevel.getEntitiesOfClass(Allay.class, searchArea, this::isMarta);
        if (candidates.isEmpty()) {
            this.martaUuid = null;
            return null;
        }
        Allay nearest = candidates.get(0);
        double nearestDistance = nearest.distanceToSqr(this);
        for (int i = 1; i < candidates.size(); i++) {
            Allay candidate = candidates.get(i);
            double distance = candidate.distanceToSqr(this);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }
        this.martaUuid = nearest.getUUID();
        return nearest;
    }

    private boolean isMarta(Allay allay) {
        return allay.getTags().contains(MARTA_TAG)
                && allay.getPersistentData().getString(MARTA_OWNER_TAG).equals(this.getStringUUID());
    }

    private void keepMartaUntamperable(Allay marta) {
        marta.addTag(MARTA_TAG);
        marta.getPersistentData().putString(MARTA_OWNER_TAG, this.getStringUUID());
        marta.setCustomName(Component.translatable("entity.hc_characters.marta"));
        marta.setCustomNameVisible(true);
        marta.setPersistenceRequired();
        marta.setCanPickUpLoot(false);
        marta.setInvulnerable(true);
        // marta.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND));
        if (marta instanceof Mob mob) {
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        }
    }

    private void discardMartaCompanion() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Allay marta = this.findMarta(serverLevel);
        if (marta != null) {
            marta.discard();
        }
        this.martaUuid = null;
    }

    public static boolean isProtectedMarta(Entity entity) {
        return entity instanceof Allay && entity.getTags().contains(MARTA_TAG);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && reason != RemovalReason.UNLOADED_TO_CHUNK) {
            this.discardMartaCompanion();
        }
        super.remove(reason);
    }

    private void tickEncounterLifetime() {
        if (this.encounterMode == EncounterMode.NONE) {
            return;
        }
        if (this.encounterTicksLeft > 0) {
            --this.encounterTicksLeft;
        }
        if (this.isBoardVisit()) {
            this.tickBoardVisit();
        } else if (this.isCaveMiningEncounter()) {
            this.tickCaveAnchor();
        }
        if (this.encounterTicksLeft <= 0) {
            this.sayNearby(this.encounterMode == EncounterMode.BOARD_VISIT
                    ? "dialogue.soa.marjorie.encounter_end.board"
                    : "dialogue.soa.marjorie.encounter_end.cave");
            this.discard();
        }
    }

    private void tickBoardVisit() {
        if (this.encounterAnchor == null) {
            this.encounterTicksLeft = 0;
            return;
        }
        if (!this.level().getBlockState(this.encounterAnchor).isAir()
                && this.distanceToSqr(Vec3.atCenterOf(this.encounterAnchor)) > BOARD_RETURN_DISTANCE_SQR) {
            this.getNavigation().moveTo(
                    this.encounterAnchor.getX() + 0.5D,
                    this.encounterAnchor.getY(),
                    this.encounterAnchor.getZ() + 0.5D,
                    0.95D
            );
        }
        if (this.distanceToSqr(Vec3.atCenterOf(this.encounterAnchor)) > BOARD_HARD_LIMIT_DISTANCE_SQR) {
            this.encounterTicksLeft = 0;
            return;
        }
        if (!this.boardGiftGiven && this.boardGiftDelayTicks > 0) {
            --this.boardGiftDelayTicks;
        }
        if (!this.boardGiftGiven && this.boardGiftDelayTicks <= 0) {
            Player player = this.findNearestPlayer(9.0D);
            if (player != null) {
                this.tryGiveBoardGift(player);
            }
        }
    }

    private void tickCaveAnchor() {
        if (this.encounterAnchor == null) {
            this.encounterAnchor = this.blockPosition().immutable();
            return;
        }
        double distanceSqr = this.distanceToSqr(Vec3.atCenterOf(this.encounterAnchor));
        if (distanceSqr > CAVE_HARD_LIMIT_DISTANCE_SQR) {
            this.encounterTicksLeft = 0;
            return;
        }
        if (distanceSqr > CAVE_RETURN_DISTANCE_SQR && this.getNavigation().isDone()) {
            this.getNavigation().moveTo(
                    this.encounterAnchor.getX() + 0.5D,
                    this.encounterAnchor.getY(),
                    this.encounterAnchor.getZ() + 0.5D,
                    0.9D
            );
        }
    }

    private void tryGiveBoardGift(Player player) {
        ItemStack gift = this.createBoardGift();
        if (gift.isEmpty()) {
            return;
        }
        ItemStack delivered = gift.copy();
        if (!player.addItem(delivered)) {
            player.drop(delivered, false);
        }
        this.boardGiftGiven = true;
        if (player instanceof ServerPlayer serverPlayer) {
            SoaMarjorieRelationship.recordBoardGift(serverPlayer);
        }
        this.sayTo(player, "dialogue.soa.marjorie.board_gift");
    }

    private ItemStack createBoardGift() {
        int roll = this.getRandom().nextInt(8);
        if (roll <= 2) {
            return new ItemStack(Items.TORCH, 8 + this.getRandom().nextInt(9));
        }
        if (roll <= 4) {
            return new ItemStack(Items.COAL, 2 + this.getRandom().nextInt(3));
        }
        if (roll == 5) {
            return new ItemStack(Items.BREAD, 1 + this.getRandom().nextInt(2));
        }
        if (roll == 6) {
            return new ItemStack(Items.IRON_NUGGET, 3 + this.getRandom().nextInt(4));
        }
        return new ItemStack(Items.RAW_COPPER, 2 + this.getRandom().nextInt(4));
    }

    private void tickAmbientDialogue() {
        if (this.level().isClientSide) {
            return;
        }
        if (this.ambientDialogueCooldown > 0) {
            --this.ambientDialogueCooldown;
            return;
        }
        if (this.getRandom().nextInt(240) != 0) {
            return;
        }
        Player nearest = this.findNearestPlayer(14.0D);
        if (nearest instanceof ServerPlayer serverPlayer) {
            String line = this.isBoardVisit()
                    ? SoaMarjorieRelationship.idleBoardLine(serverPlayer, this.getRandom())
                    : SoaMarjorieRelationship.idleCaveLine(serverPlayer, this.getRandom());
            this.sayTo(serverPlayer, line);
        } else {
            String[] lines;
            if (this.isBoardVisit()) {
                lines = new String[] {
                        "dialogue.soa.marjorie.nearby.board_idle.1",
                        "dialogue.soa.marjorie.nearby.board_idle.2",
                        "dialogue.soa.marjorie.nearby.board_idle.3",
                        "dialogue.soa.marjorie.nearby.board_idle.4"
                };
            } else {
                lines = new String[] {
                        "dialogue.soa.marjorie.nearby.cave_idle.1",
                        "dialogue.soa.marjorie.nearby.cave_idle.2",
                        "dialogue.soa.marjorie.nearby.cave_idle.3",
                        "dialogue.soa.marjorie.nearby.cave_idle.4"
                };
            }
            this.sayNearby(lines[this.getRandom().nextInt(lines.length)]);
        }
        this.ambientDialogueCooldown = 20 * 35 + this.getRandom().nextInt(20 * 45);
    }

    private void sayNearby(String text) {
        for (Player player : this.level().players()) {
            if (this.distanceToSqr(player) <= 18.0D * 18.0D) {
                this.sayTo(player, text);
            }
        }
    }

    private void sayTo(Player player, String text) {
        player.displayClientMessage(
                Component.literal("<")
                        .append(Component.translatable(SOA_KEY))
                        .append("> ")
                        .append(asDialogueComponent(text)),
                false
        );
    }

    private static Component asDialogueComponent(String text) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        if (!text.contains(" ")) {
            return Component.translatable(text);
        }
        return Component.literal(text);
    }

    private Player findNearestPlayer(double radius) {
        Player nearest = null;
        double bestDistance = radius * radius;
        for (Player player : this.level().players()) {
            if (player.isSpectator()) {
                continue;
            }
            double distance = this.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        float effectiveAmount = this.scaleIncomingDamage(attacker, amount);
        boolean hurt = super.hurt(source, effectiveAmount);
        if (!this.level().isClientSide && hurt) {
            if (attacker instanceof LivingEntity livingAttacker) {
                this.setDefensiveTarget(livingAttacker);
            }
            if (attacker instanceof Player player) {
                this.warnPlayerWithMiningAxe(player);
            }
        }
        return hurt;
    }

    private float scaleIncomingDamage(Entity attacker, float amount) {
        if (attacker instanceof Player) {
            return Math.min(amount, 0.75F);
        }
        return Math.min(amount * 0.08F, 0.35F);
    }

    private void tickSurvivabilityAndThreats() {
        if (this.tickCount % 40 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(3.0F);
        }
        if (this.hostileScanCooldown > 0) {
            --this.hostileScanCooldown;
            return;
        }
        this.hostileScanCooldown = HOSTILE_SCAN_INTERVAL_TICKS;
        if (!this.isCaveMiningEncounter()) {
            return;
        }
        this.scanNearbyHostiles();
    }

    private void scanNearbyHostiles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        AABB area = this.getBoundingBox().inflate(HOSTILE_SCAN_RADIUS);
        List<Monster> monsters = serverLevel.getEntitiesOfClass(
                Monster.class,
                area,
                monster -> monster.isAlive() && this.hasLineOfSight(monster)
        );
        Monster closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Monster monster : monsters) {
            LivingEntity monsterTarget = monster.getTarget();
            boolean isThreat = monsterTarget == this || monsterTarget instanceof Player || monsterTarget == null;
            if (!isThreat) {
                continue;
            }
            double distance = this.distanceToSqr(monster);
            if (distance < closestDistance) {
                closestDistance = distance;
                closest = monster;
            }
        }
        if (closest == null) {
            return;
        }
        if (closest.getTarget() == null && this.getRandom().nextInt(3) == 0) {
            closest.setTarget(this);
        }
        this.setDefensiveTarget(closest);
    }

    private void setDefensiveTarget(LivingEntity target) {
        if (target == this || !target.isAlive()) {
            return;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        this.defenseDrawTicks = DEFENSE_DRAW_TICKS;
        this.setTarget(target);
        this.equipExpertMiningTools();
    }

    private void warnPlayerWithMiningAxe(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        this.defenseDrawTicks = DEFENSE_DRAW_TICKS;
        this.equipExpertMiningTools();
        if (player instanceof ServerPlayer serverPlayer) {
            SoaMarjorieRelationship.recordAttack(serverPlayer);
        }
        this.sayTo(player, "dialogue.soa.marjorie.attack_warning");
        this.getLookControl().setLookAt(player, 30.0F, 30.0F);

        double distanceSqr = this.distanceToSqr(player);
        if (distanceSqr > 16.0D) {
            this.getNavigation().moveTo(player, 1.35D);
            return;
        }

        player.hurt(this.damageSources().mobAttack(this), PLAYER_WARNING_STRIKE_DAMAGE);
        if (player.isAlive() && player.getHealth() > 1.0F) {
            player.setHealth(1.0F);
        }
    }

    private void syncMiningEquipmentToCurios() {
        if (this.equipmentSyncCooldown > 0) {
            --this.equipmentSyncCooldown;
            return;
        }
        this.equipmentSyncCooldown = 40;

        this.syncCuriosSlot(CURIOS_BACKPACK_SLOT, this.createSophisticatedBackpackStack());
        this.syncCuriosSlot(CURIOS_TOOL_RIGHT_SLOT, new ItemStack(Items.NETHERITE_PICKAXE));
        this.syncCuriosSlot(CURIOS_TOOL_LEFT_SLOT, new ItemStack(Items.NETHERITE_AXE));
    }

    private void syncCuriosSlot(String slot, ItemStack wanted) {
        if (wanted.isEmpty()) {
            return;
        }
        ItemStack current = PolenCuriosBridge.getCuriosStack(this, slot, 0);
        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, wanted)) {
            return;
        }
        PolenCuriosBridge.setCuriosStack(this, slot, 0, wanted);
    }

    private ItemStack createSophisticatedBackpackStack() {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(SOPHISTICATED_BACKPACK));
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 240.0D)
                .add(Attributes.ARMOR, 30.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 36.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DefensiveAxeAttackGoal(this));
        this.goalSelector.addGoal(2, new StayNearBoardGoal(this));
        this.goalSelector.addGoal(3, new TorchDarknessGoal(this));
        this.goalSelector.addGoal(4, new MiningWorkGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("SoaMarjorieEncounterMode", this.encounterMode.name());
        compound.putInt("SoaMarjorieEncounterTicksLeft", this.encounterTicksLeft);
        compound.putInt("SoaMarjorieBoardGiftDelay", this.boardGiftDelayTicks);
        compound.putBoolean("SoaMarjorieBoardGiftGiven", this.boardGiftGiven);
        if (this.encounterAnchor != null) {
            compound.putLong("SoaMarjorieEncounterAnchor", this.encounterAnchor.asLong());
        }
        if (this.martaUuid != null) {
            compound.putUUID(MARTA_COMPANION_UUID_TAG, this.martaUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        try {
            this.encounterMode = EncounterMode.valueOf(compound.getString("SoaMarjorieEncounterMode"));
        } catch (IllegalArgumentException exception) {
            this.encounterMode = EncounterMode.NONE;
        }
        this.encounterTicksLeft = compound.getInt("SoaMarjorieEncounterTicksLeft");
        this.boardGiftDelayTicks = compound.getInt("SoaMarjorieBoardGiftDelay");
        this.boardGiftGiven = compound.getBoolean("SoaMarjorieBoardGiftGiven");
        if (compound.contains("SoaMarjorieEncounterAnchor")) {
            this.encounterAnchor = BlockPos.of(compound.getLong("SoaMarjorieEncounterAnchor"));
        } else {
            this.encounterAnchor = null;
        }
        if (compound.hasUUID(MARTA_COMPANION_UUID_TAG)) {
            this.martaUuid = compound.getUUID(MARTA_COMPANION_UUID_TAG);
        } else {
            this.martaUuid = null;
        }
    }

    private enum EncounterMode {
        NONE,
        BOARD_VISIT,
        CAVE_MINING
    }

    private static final class DefensiveAxeAttackGoal extends MeleeAttackGoal {
        private final SoaMarjorieEntity soa;

        private DefensiveAxeAttackGoal(SoaMarjorieEntity soa) {
            super(soa, 1.25D, true);
            this.soa = soa;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.soa.getTarget();
            return target != null && target.isAlive() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.soa.getTarget();
            return target != null && target.isAlive() && super.canContinueToUse();
        }

        @Override
        public void start() {
            this.soa.defenseDrawTicks = DEFENSE_DRAW_TICKS;
            this.soa.equipExpertMiningTools();
            super.start();
        }

        @Override
        public void tick() {
            this.soa.defenseDrawTicks = DEFENSE_DRAW_TICKS;
            this.soa.equipExpertMiningTools();
            super.tick();
        }

        @Override
        public void stop() {
            super.stop();
            LivingEntity target = this.soa.getTarget();
            if (target == null || !target.isAlive()) {
                this.soa.setTarget(null);
            }
        }
    }

    private static final class StayNearBoardGoal extends Goal {
        private final SoaMarjorieEntity soa;

        private StayNearBoardGoal(SoaMarjorieEntity soa) {
            this.soa = soa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.soa.isBoardVisit() || this.soa.getEncounterAnchor() == null) {
                return false;
            }
            return this.soa.distanceToSqr(Vec3.atCenterOf(this.soa.getEncounterAnchor())) > BOARD_RETURN_DISTANCE_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            return this.soa.isBoardVisit()
                    && this.soa.getEncounterAnchor() != null
                    && this.soa.distanceToSqr(Vec3.atCenterOf(this.soa.getEncounterAnchor())) > 5.0D * 5.0D;
        }

        @Override
        public void tick() {
            BlockPos anchor = this.soa.getEncounterAnchor();
            if (anchor != null && this.soa.getNavigation().isDone()) {
                this.soa.getNavigation().moveTo(anchor.getX() + 0.5D, anchor.getY(), anchor.getZ() + 0.5D, 0.95D);
            }
        }
    }

    private static final class TorchDarknessGoal extends Goal {
        private static final int LIGHT_THRESHOLD = 8;
        private static final int PLACE_COOLDOWN_TICKS = 100;
        private static final double MIN_DISTANCE_FROM_LAST_TORCH_SQR = 6.0D * 6.0D;

        private final SoaMarjorieEntity soa;
        private int cooldown;
        private BlockPos lastTorchPos;
        private BlockPos targetPos;

        private TorchDarknessGoal(SoaMarjorieEntity soa) {
            this.soa = soa;
        }

        @Override
        public boolean canUse() {
            if (this.soa.level().isClientSide || !this.soa.isCaveMiningEncounter()) {
                return false;
            }
            if (!HcCharactersGameplayConfig.soaMarjorieCanPlaceTorches()) {
                return false;
            }
            if (this.cooldown > 0) {
                --this.cooldown;
                return false;
            }
            if (!this.isDarkEnough()) {
                return false;
            }
            if (this.lastTorchPos != null
                    && this.soa.distanceToSqr(Vec3.atCenterOf(this.lastTorchPos)) < MIN_DISTANCE_FROM_LAST_TORCH_SQR) {
                return false;
            }
            this.targetPos = this.findTorchPos();
            return this.targetPos != null;
        }

        @Override
        public void start() {
            if (this.targetPos != null && this.placeTorch(this.targetPos)) {
                this.soa.drawTorchBriefly();
                if (this.soa.getRandom().nextInt(4) == 0) {
                    this.soa.sayNearby("dialogue.soa.marjorie.torch_place");
                }
                this.lastTorchPos = this.targetPos.immutable();
                this.cooldown = PLACE_COOLDOWN_TICKS + this.soa.getRandom().nextInt(80);
            } else {
                this.cooldown = 30;
            }
            this.targetPos = null;
        }

        private boolean isDarkEnough() {
            return LevelBrightnessHelper.maxLocalRawBrightness(this.soa.level(), this.soa.blockPosition()) <= LIGHT_THRESHOLD;
        }

        private BlockPos findTorchPos() {
            BlockPos base = this.soa.blockPosition();
            BlockPos[] candidates = new BlockPos[] {
                    base,
                    base.north(), base.south(), base.east(), base.west(),
                    base.below(),
                    base.north().below(), base.south().below(), base.east().below(), base.west().below()
            };
            for (BlockPos candidate : candidates) {
                BlockPos torchPos = candidate;
                if (this.canPlaceTorchAt(torchPos)) {
                    return torchPos;
                }
                BlockPos above = candidate.above();
                if (this.canPlaceTorchAt(above)) {
                    return above;
                }
            }
            return null;
        }

        private boolean canPlaceTorchAt(BlockPos pos) {
            Level level = this.soa.level();
            if (!level.getBlockState(pos).isAir()) {
                return false;
            }
            BlockState torchState = Blocks.TORCH.defaultBlockState();
            return torchState.canSurvive(level, pos);
        }

        private boolean placeTorch(BlockPos pos) {
            Level level = this.soa.level();
            if (!level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }
            if (!this.canPlaceTorchAt(pos)) {
                return false;
            }
            return level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 3);
        }
    }

    private static final class MiningWorkGoal extends Goal {
        private static final int HORIZONTAL_RADIUS = 12;
        private static final int VERTICAL_RADIUS = 5;
        private static final int SEARCH_INTERVAL_TICKS = 20;
        private static final int WORK_TICKS_PER_BLOCK = 35;
        private static final int BLOCK_COOLDOWN_TICKS = 45;
        private static final int PLAYER_SHARE_RADIUS = 13;

        private final SoaMarjorieEntity soa;
        private BlockPos orePos;
        private BlockPos standPos;
        private int nextSearchTick;
        private int workTicks;
        private int cooldownTicks;
        private int minedBlocks;

        private MiningWorkGoal(SoaMarjorieEntity soa) {
            this.soa = soa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.soa.isVehicle() || this.soa.isPassenger() || !this.soa.isCaveMiningEncounter()) {
                return false;
            }
            if (!HcCharactersGameplayConfig.soaMarjorieCanMineBlocks()) {
                return false;
            }
            if (!this.soa.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }
            if (this.minedBlocks >= HcCharactersGameplayConfig.soaMarjorieMaxBlocksPerCaveEncounter()) {
                return false;
            }
            Player companion = this.soa.findNearestPlayer(PLAYER_SHARE_RADIUS);
            if (companion == null) {
                return false;
            }
            if (this.cooldownTicks > 0) {
                --this.cooldownTicks;
                return false;
            }
            if (this.nextSearchTick > 0) {
                --this.nextSearchTick;
                return false;
            }
            this.nextSearchTick = SEARCH_INTERVAL_TICKS + this.soa.getRandom().nextInt(60);
            return this.findOreTarget();
        }

        @Override
        public boolean canContinueToUse() {
            return this.orePos != null
                    && this.standPos != null
                    && this.soa.isCaveMiningEncounter()
                    && this.minedBlocks < HcCharactersGameplayConfig.soaMarjorieMaxBlocksPerCaveEncounter()
                    && this.canMine(this.orePos)
                    && this.soa.distanceToSqr(Vec3.atCenterOf(this.orePos)) < 18.0D * 18.0D
                    && this.soa.findNearestPlayer(PLAYER_SHARE_RADIUS) != null;
        }

        @Override
        public void start() {
            this.workTicks = WORK_TICKS_PER_BLOCK + this.soa.getRandom().nextInt(30);
            this.soa.drawMiningPickaxeBriefly();
            if (this.soa.getRandom().nextInt(3) == 0) {
                this.soa.sayNearby("dialogue.soa.marjorie.mining_start");
            }
            this.moveToStandPos();
        }

        @Override
        public void tick() {
            if (this.orePos == null) {
                return;
            }
            this.soa.drawMiningPickaxeBriefly();
            this.soa.getLookControl().setLookAt(
                    this.orePos.getX() + 0.5D,
                    this.orePos.getY() + 0.5D,
                    this.orePos.getZ() + 0.5D,
                    30.0F,
                    30.0F
            );
            if (this.standPos != null
                    && this.soa.getNavigation().isDone()
                    && this.soa.distanceToSqr(Vec3.atCenterOf(this.standPos)) > 2.25D) {
                this.moveToStandPos();
                return;
            }
            if (this.soa.distanceToSqr(Vec3.atCenterOf(this.orePos)) > 12.25D) {
                return;
            }
            if (this.workTicks > 0) {
                --this.workTicks;
                return;
            }
            if (this.mineOre()) {
                ++this.minedBlocks;
                this.cooldownTicks = BLOCK_COOLDOWN_TICKS + this.soa.getRandom().nextInt(45);
            }
            this.orePos = null;
            this.standPos = null;
        }

        @Override
        public void stop() {
            this.orePos = null;
            this.standPos = null;
            this.workTicks = 0;
        }

        private void moveToStandPos() {
            if (this.standPos != null) {
                this.soa.getNavigation().moveTo(
                        this.standPos.getX() + 0.5D,
                        this.standPos.getY(),
                        this.standPos.getZ() + 0.5D,
                        0.9D
                );
            }
        }

        private boolean mineOre() {
            if (!(this.soa.level() instanceof ServerLevel serverLevel) || this.orePos == null || !this.canMine(this.orePos)) {
                return false;
            }
            BlockState state = serverLevel.getBlockState(this.orePos);
            ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
            List<ItemStack> drops = Block.getDrops(state, serverLevel, this.orePos, serverLevel.getBlockEntity(this.orePos), this.soa, tool);
            serverLevel.levelEvent(2001, this.orePos, Block.getId(state));
            serverLevel.setBlock(this.orePos, Blocks.AIR.defaultBlockState(), 3);
            this.shareCompanionReward(drops);
            if (this.soa.getRandom().nextInt(5) == 0) {
                this.soa.sayNearby("dialogue.soa.marjorie.share_reward");
            }
            return true;
        }

        private void shareCompanionReward(List<ItemStack> drops) {
            Player companion = this.soa.findNearestPlayer(PLAYER_SHARE_RADIUS);
            if (companion == null) {
                return;
            }
            List<ItemStack> rewards = new ArrayList<>();
            for (ItemStack drop : drops) {
                if (drop.isEmpty()) {
                    continue;
                }
                int share = drop.getCount() / 4;
                int remainder = drop.getCount() % 4;
                for (int i = 0; i < remainder; i++) {
                    if (this.soa.getRandom().nextInt(4) == 0) {
                        ++share;
                    }
                }
                if (share <= 0) {
                    continue;
                }
                ItemStack reward = drop.copy();
                reward.setCount(share);
                rewards.add(reward);
            }
            int totalShared = 0;
            for (ItemStack reward : rewards) {
                totalShared += reward.getCount();
                ItemStack delivered = reward.copy();
                if (!companion.addItem(delivered)) {
                    companion.drop(delivered, false);
                }
            }
            if (totalShared > 0 && companion instanceof ServerPlayer serverPlayer) {
                SoaMarjorieRelationship.recordOreShared(serverPlayer, totalShared);
            }
        }

        private boolean findOreTarget() {
            BlockPos origin = this.soa.blockPosition();
            BlockPos bestOre = null;
            BlockPos bestStand = null;
            double bestDistance = Double.MAX_VALUE;

            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int y = -VERTICAL_RADIUS; y <= VERTICAL_RADIUS; y++) {
                for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
                    for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                        cursor.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                        BlockPos candidateOre = cursor.immutable();
                        if (!this.canMine(candidateOre)) {
                            continue;
                        }
                        BlockPos candidateStand = this.findStandPos(candidateOre);
                        if (candidateStand == null) {
                            continue;
                        }
                        double distance = this.soa.distanceToSqr(Vec3.atCenterOf(candidateStand));
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            bestOre = candidateOre;
                            bestStand = candidateStand;
                        }
                    }
                }
            }

            this.orePos = bestOre;
            this.standPos = bestStand;
            return this.orePos != null && this.standPos != null;
        }

        private boolean canMine(BlockPos pos) {
            Level level = this.soa.level();
            BlockState state = level.getBlockState(pos);
            return isOre(state) && hasExposedFace(level, pos);
        }

        private BlockPos findStandPos(BlockPos ore) {
            BlockPos[] candidates = new BlockPos[] {
                    ore.north(), ore.south(), ore.east(), ore.west(), ore.above(), ore.below()
            };
            for (BlockPos candidate : candidates) {
                if (this.canStandAt(candidate)) {
                    return candidate;
                }
            }
            return null;
        }

        private boolean canStandAt(BlockPos pos) {
            Level level = this.soa.level();
            return level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()
                    && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
        }

        private static boolean hasExposedFace(Level level, BlockPos pos) {
            for (Direction direction : Direction.values()) {
                if (level.getBlockState(pos.relative(direction)).isAir()) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isOre(BlockState state) {
            return state.is(SOA_MINEABLE_ORES)
                    || state.is(BlockTags.COAL_ORES)
                    || state.is(BlockTags.IRON_ORES)
                    || state.is(BlockTags.COPPER_ORES)
                    || state.is(BlockTags.GOLD_ORES)
                    || state.is(BlockTags.REDSTONE_ORES)
                    || state.is(BlockTags.EMERALD_ORES)
                    || state.is(BlockTags.LAPIS_ORES)
                    || state.is(BlockTags.DIAMOND_ORES);
        }
    }
}
