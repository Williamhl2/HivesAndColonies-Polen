package com.hivesandcolonies.hccharacters.character.polen.item.focus;

import com.hivesandcolonies.hccharacters.character.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.hccharacters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue.PolenPrologueManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.hccharacters.common.util.CharacterNbtHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HiveheartCharmItem extends PolenLoreItem {
    private static final int USE_COOLDOWN = 100;
    private static final String TARGET_POS_TAG = "PolenClearingTarget";
    private static final String TARGET_DIMENSION_TAG = "PolenClearingDimension";

    public HiveheartCharmItem(Properties properties) {
        super(
                properties.stacksTo(1),
                PolenProgressionStage.PROLOGUE,
                true,
                new TooltipLine("tooltip.polen.hiveheart_charm.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.hiveheart_charm.line2", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.hiveheart_charm.line3", ChatFormatting.DARK_GRAY),
                new TooltipLine("tooltip.polen.hiveheart_charm.line4", ChatFormatting.DARK_GRAY)
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }

        if (!serverLevel.dimension().equals(Level.OVERWORLD)) {
            player.displayClientMessage(
                    Component.translatable("message.polen.item.hiveheart_charm.no_resonance"),
                    true
            );
            player.getCooldowns().addCooldown(this, USE_COOLDOWN);
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel overworld = serverLevel.getServer().overworld();
        if (PolenStoryFlagsManager.hasFlag(overworld, PolenStoryFlag.NAME_REVEALED) || isPolenAlreadyKnown(overworld)) {
            player.displayClientMessage(
                    Component.translatable("message.polen.item.hiveheart_charm.dormant"),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos targetPos = PolenPrologueManager.spawnPolenAtNearbyCherryGrove(serverPlayer);
        if (targetPos == null) {
            player.displayClientMessage(
                    Component.translatable("message.polen.item.hiveheart_charm.no_resonance"),
                    true
            );
            player.getCooldowns().addCooldown(this, USE_COOLDOWN);
            return InteractionResultHolder.fail(stack);
        }

        BlockPos locatorTarget = PolenPrologueManager.resolveLocatorTarget(overworld);
        if (locatorTarget == null) {
            locatorTarget = targetPos;
        }

        bindTarget(stack, overworld, locatorTarget);
        String directionKey = resolveDirectionKey(player, locatorTarget);
        int distanceBlocks = horizontalDistance(player.position(), locatorTarget);
        String distanceKey = resolveDistanceKey(distanceBlocks);
        player.displayClientMessage(
                Component.translatable(
                        "message.polen.item.hiveheart_charm.guidance",
                        Component.translatable(directionKey),
                        Component.translatable(distanceKey)
                ),
                true
        );

        serverLevel.playSound(
                null,
                player.blockPosition(),
                SoundEvents.LODESTONE_COMPASS_LOCK,
                SoundSource.PLAYERS,
                0.7F,
                pitchForDistance(distanceBlocks)
        );
        player.getCooldowns().addCooldown(this, USE_COOLDOWN);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Keep this tick intentionally lightweight. The charm used to rebuild or locate
        // Polen's prologue site from inventoryTick, which can run every tick for every
        // copy of the item and stall the integrated/dedicated server. Target refresh now
        // happens only when the player actively uses the item.
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    public static void bindTarget(ItemStack stack, ServerLevel level, BlockPos target) {
        if (stack == null || level == null || target == null) {
            return;
        }

        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CharacterNbtHelper.saveBlockPos(tag, TARGET_POS_TAG, target);
            tag.putString(TARGET_DIMENSION_TAG, level.dimension().location().toString());
        });
    }

    @Nullable
    public static GlobalPos getCompassTarget(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (!tag.contains(TARGET_DIMENSION_TAG, CompoundTag.TAG_STRING)) {
            return null;
        }

        BlockPos target = CharacterNbtHelper.loadBlockPos(tag, TARGET_POS_TAG);
        if (target == null) {
            return null;
        }

        ResourceLocation location = ResourceLocation.tryParse(tag.getString(TARGET_DIMENSION_TAG));
        if (location == null) {
            return null;
        }

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, location);
        return GlobalPos.of(dimension, target);
    }


    private static boolean isPolenAlreadyKnown(ServerLevel level) {
        if (level == null) {
            return false;
        }
        PolenWorldStoryData data = PolenWorldStateManager.get(level);
        return data.isPolenSpawned() || data.getPolenEntityUuid() != null;
    }

    private static float pitchForDistance(int distance) {
        if (distance <= 32) {
            return 1.2F;
        }
        if (distance <= 96) {
            return 1.05F;
        }
        if (distance <= 224) {
            return 0.95F;
        }
        return 0.82F;
    }

    private static int horizontalDistance(Vec3 playerPos, BlockPos target) {
        double dx = target.getX() + 0.5D - playerPos.x;
        double dz = target.getZ() + 0.5D - playerPos.z;
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

    private static String resolveDistanceKey(int distance) {
        if (distance <= 24) {
            return "message.polen.item.hiveheart_charm.distance.immediate";
        }
        if (distance <= 80) {
            return "message.polen.item.hiveheart_charm.distance.near";
        }
        if (distance <= 192) {
            return "message.polen.item.hiveheart_charm.distance.far";
        }
        return "message.polen.item.hiveheart_charm.distance.distant";
    }

    private static String resolveDirectionKey(Player player, BlockPos target) {
        Vec3 toTarget = Vec3.atCenterOf(target).subtract(player.position());
        Vec3 horizontalToTarget = new Vec3(toTarget.x, 0.0D, toTarget.z);
        if (horizontalToTarget.lengthSqr() <= 64.0D) {
            return "message.polen.item.hiveheart_charm.direction.here";
        }

        Vec3 facing = player.getLookAngle();
        Vec3 horizontalFacing = new Vec3(facing.x, 0.0D, facing.z);
        if (horizontalFacing.lengthSqr() < 0.0001D) {
            horizontalFacing = Vec3.atLowerCornerOf(player.getDirection().getNormal());
        }
        horizontalFacing = horizontalFacing.normalize();
        horizontalToTarget = horizontalToTarget.normalize();

        Vec3 right = new Vec3(-horizontalFacing.z, 0.0D, horizontalFacing.x);
        double forwardDot = horizontalFacing.dot(horizontalToTarget);
        double rightDot = right.dot(horizontalToTarget);
        double angle = Math.atan2(rightDot, forwardDot);
        double sector = Math.PI / 8.0D;

        if (angle >= -sector && angle < sector) {
            return "message.polen.item.hiveheart_charm.direction.ahead";
        }
        if (angle >= sector && angle < 3.0D * sector) {
            return "message.polen.item.hiveheart_charm.direction.ahead_right";
        }
        if (angle >= 3.0D * sector && angle < 5.0D * sector) {
            return "message.polen.item.hiveheart_charm.direction.right";
        }
        if (angle >= 5.0D * sector && angle < 7.0D * sector) {
            return "message.polen.item.hiveheart_charm.direction.back_right";
        }
        if (angle >= 7.0D * sector || angle < -7.0D * sector) {
            return "message.polen.item.hiveheart_charm.direction.back";
        }
        if (angle >= -7.0D * sector && angle < -5.0D * sector) {
            return "message.polen.item.hiveheart_charm.direction.back_left";
        }
        if (angle >= -5.0D * sector && angle < -3.0D * sector) {
            return "message.polen.item.hiveheart_charm.direction.left";
        }
        return "message.polen.item.hiveheart_charm.direction.ahead_left";
    }
}
