package com.hivesandcolonies.hccharacters.character.soa.entity;

import java.util.EnumSet;

import com.hivesandcolonies.hccharacters.common.entity.SimpleCharacterEntity;
import com.hivesandcolonies.hccharacters.integration.curios.PolenCuriosBridge;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipInteraction;
import com.hivesandcolonies.hccharacters.character.soa.dialogue.SoaMarjorieDialogue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SoaMarjorieEntity extends SimpleCharacterEntity {
    private static final String SOA_KEY = "entity.hc_characters.soa_marjorie";
    private static final int DEFENSE_DRAW_TICKS = 120;
    public static final String CURIOS_BACKPACK_SLOT = "backpack";
    public static final String CURIOS_TOOL_RIGHT_SLOT = "tool_right";
    public static final String CURIOS_TOOL_LEFT_SLOT = "tool_left";
    private static final ResourceLocation SOPHISTICATED_BACKPACK = ResourceLocation.fromNamespaceAndPath("sophisticatedbackpacks", "backpack");
    private static final float PLAYER_WARNING_STRIKE_DAMAGE = 38.0F;

    private int defenseDrawTicks;
    private int miningDrawTicks;
    private int torchDrawTicks;
    private int ambientDialogueCooldown;
    private int equipmentSyncCooldown;

    public SoaMarjorieEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable(SOA_KEY));
        this.setCustomNameVisible(true);
        this.equipExpertMiningTools();
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
                    SoaMarjorieDialogue.rewardPool()
            );
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
            this.equipExpertMiningTools();
            this.syncMiningEquipmentToCurios();
        }
        this.tickAmbientDialogue();
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
        String[] lines = new String[] {
                "Esta veta no está sola... hay algo bueno cerca.",
                "Antorcha cada pocos pasos. La oscuridad cobra intereses.",
                "Si el eco vuelve seco, hay cámara grande adelante.",
                "La netherita no perdona manos torpes. Por suerte, las mías no lo son."
        };
        this.sayNearby(lines[this.getRandom().nextInt(lines.length)]);
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
        player.displayClientMessage(Component.literal("<SoaMarjorie> " + text), false);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!this.level().isClientSide && hurt) {
            Entity attacker = source.getEntity();
            if (attacker instanceof Player player) {
                this.warnPlayerWithMiningAxe(player);
            }
        }
        return hurt;
    }

    private void warnPlayerWithMiningAxe(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        this.defenseDrawTicks = DEFENSE_DRAW_TICKS;
        this.equipExpertMiningTools();
        this.sayTo(player, "Una advertencia basta. La siguiente abre hasta bedrock.");
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
            // Sophisticated Backpacks is a required dependency, so this should never happen in a valid install.
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 44.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 34.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
        this.goalSelector.addGoal(2, new TorchDarknessGoal(this));
        this.goalSelector.addGoal(3, new MiningSurveyGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }


    private static final class TorchDarknessGoal extends Goal {
        private static final int LIGHT_THRESHOLD = 6;
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
            if (this.soa.level().isClientSide) {
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
                    this.soa.sayNearby("Aquí faltaba luz. Los monstruos no pagan alquiler.");
                }
                this.lastTorchPos = this.targetPos.immutable();
                this.cooldown = PLACE_COOLDOWN_TICKS + this.soa.getRandom().nextInt(80);
            } else {
                this.cooldown = 30;
            }
            this.targetPos = null;
        }

        private boolean isDarkEnough() {
            return this.soa.level().getMaxLocalRawBrightness(this.soa.blockPosition()) <= LIGHT_THRESHOLD;
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
            if (!this.canPlaceTorchAt(pos)) {
                return false;
            }
            return level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 3);
        }
    }

    private static final class MiningSurveyGoal extends Goal {
        private static final int HORIZONTAL_RADIUS = 12;
        private static final int VERTICAL_RADIUS = 5;
        private static final int SEARCH_INTERVAL_TICKS = 80;

        private final SoaMarjorieEntity soa;
        private BlockPos orePos;
        private BlockPos standPos;
        private int nextSearchTick;
        private int inspectTicks;

        private MiningSurveyGoal(SoaMarjorieEntity soa) {
            this.soa = soa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.soa.isVehicle() || this.soa.isPassenger()) {
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
                    && isOre(this.soa.level().getBlockState(this.orePos))
                    && this.soa.distanceToSqr(Vec3.atCenterOf(this.orePos)) < 18.0D * 18.0D
                    && (!this.soa.getNavigation().isDone() || this.inspectTicks > 0);
        }

        @Override
        public void start() {
            this.inspectTicks = 100 + this.soa.getRandom().nextInt(80);
            this.soa.drawMiningPickaxeBriefly();
            if (this.soa.getRandom().nextInt(3) == 0) {
                this.soa.sayNearby("Voy a revisar esa veta. No todas las piedras dicen la verdad a primera vista.");
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
            if (this.inspectTicks > 0) {
                --this.inspectTicks;
                this.soa.drawMiningPickaxeBriefly();
            }
            if (this.standPos != null
                    && this.soa.getNavigation().isDone()
                    && this.soa.distanceToSqr(Vec3.atCenterOf(this.standPos)) > 2.25D) {
                this.moveToStandPos();
            }
        }

        @Override
        public void stop() {
            this.orePos = null;
            this.standPos = null;
            this.inspectTicks = 0;
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
                        BlockState state = this.soa.level().getBlockState(cursor);
                        if (!isOre(state)) {
                            continue;
                        }
                        BlockPos candidateOre = cursor.immutable();
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

        private static boolean isOre(BlockState state) {
            return state.is(BlockTags.COAL_ORES)
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
