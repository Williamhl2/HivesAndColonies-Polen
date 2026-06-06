package com.hivesandcolonies.characters.character.polen.item.focus;

import com.hivesandcolonies.characters.character.polen.item.base.PolenLoreItem;
import com.hivesandcolonies.characters.character.polen.item.meta.PolenProgressionStage;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.characters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.characters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.characters.character.polen.progression.world.prologue.PolenPrologueManager;
import com.hivesandcolonies.characters.common.util.CharacterNbtHelper;
import com.hivesandcolonies.characters.common.item.base.TranslatableTooltipItem.TooltipLine;

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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HiveheartCharmItem extends PolenLoreItem {
    private static final int USE_COOLDOWN = 30;
    private static final String TARGET_POS_TAG = "PolenClearingTarget";
    private static final String TARGET_DIMENSION_TAG = "PolenClearingDimension";

    public HiveheartCharmItem(Properties properties) {
        super(
                properties.stacksTo(1),
                PolenProgressionStage.PROLOGUE,
                true,
                new TooltipLine("tooltip.polen.hiveheart_charm.line1", ChatFormatting.GOLD),
                new TooltipLine("tooltip.polen.hiveheart_charm.line2", ChatFormatting.GRAY),
                new TooltipLine("tooltip.polen.hiveheart_charm.line3", ChatFormatting.DARK_GRAY)
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

        if (PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.NAME_REVEALED)) {
            player.displayClientMessage(
                    Component.translatable("message.polen.item.hiveheart_charm.dormant"),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        BlockPos target = resolveOrBindTarget(stack, serverLevel);
        if (target == null) {
            player.displayClientMessage(
                    Component.translatable("message.polen.item.hiveheart_charm.no_resonance"),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        String directionKey = resolveDirectionKey(player, target);
        int distanceBlocks = horizontalDistance(player.position(), target);
        player.displayClientMessage(
                Component.translatable(
                        "message.polen.item.hiveheart_charm.guidance",
                        Component.translatable(directionKey),
                        distanceBlocks,
                        target.getX(),
                        target.getZ()
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
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            PolenPrologueManager.ensurePrologueContent(serverLevel);
            BlockPos desiredTarget = PolenPrologueManager.resolveLocatorTarget(serverLevel);
            BlockPos storedTarget = readTarget(stack);
            if (desiredTarget != null && !desiredTarget.equals(storedTarget)) {
                bindTarget(stack, serverLevel, desiredTarget);
            }
        }
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

    @Nullable
    private static BlockPos resolveOrBindTarget(ItemStack stack, ServerLevel level) {
        GlobalPos stored = getCompassTarget(stack);
        if (stored != null && stored.dimension().equals(level.dimension())) {
            return stored.pos();
        }

        PolenPrologueManager.ensurePrologueContent(level);
        BlockPos target = PolenPrologueManager.resolveLocatorTarget(level);
        bindTarget(stack, level, target);
        return target;
    }

    @Nullable
    private static BlockPos readTarget(ItemStack stack) {
        GlobalPos target = getCompassTarget(stack);
        return target == null ? null : target.pos();
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
