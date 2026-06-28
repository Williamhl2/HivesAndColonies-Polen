package com.hivesandcolonies.hccharacters.character.soa.companion;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.common.util.LocalizedText;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Owns Marta's lifecycle and protection rules.
 *
 * Marta intentionally remains a vanilla Allay so she keeps vanilla sounds, movement and mod compatibility.  The
 * controller marks only Soa Marjorie's companion with an entity tag and a gold-styled custom name, allowing the
 * client renderer to draw her as a golden "variocolor" variant without changing every Allay in the world.
 */
public final class SoaMartaCompanionController {
    public static final String MARTA_TAG = "hc_characters_soa_marta";
    public static final String MARTA_GOLDEN_VARIANT_TAG = "hc_characters_soa_marta_golden";

    private static final String MARTA_OWNER_TAG = "SoaMarjorieOwner";
    private static final String MARTA_COMPANION_UUID_TAG = "SoaMarjorieMartaUuid";
    private static final String MARTA_VARIANT_TAG = "SoaMarjorieMartaVariant";
    private static final String GOLDEN_VARIANT = "golden";
    private static final TextColor GOLD_TEXT_COLOR = TextColor.fromLegacyFormat(ChatFormatting.GOLD);
    private static final double MARTA_FOLLOW_DISTANCE_SQR = 7.0D * 7.0D;
    private static final double MARTA_TELEPORT_DISTANCE_SQR = 36.0D * 36.0D;
    private static final double MARTA_SEARCH_RADIUS = 48.0D;

    private final SoaMarjorieEntity soa;
    private UUID martaUuid;

    public SoaMartaCompanionController(SoaMarjorieEntity soa) {
        this.soa = soa;
    }

    public void tick() {
        if (!(this.soa.level() instanceof ServerLevel serverLevel) || !this.soa.isAlive()) {
            return;
        }

        Allay marta = this.getOrCreateMarta(serverLevel);
        if (marta == null || !marta.isAlive()) {
            return;
        }

        this.keepMartaStyledAndProtected(marta);
        double distanceSqr = marta.distanceToSqr(this.soa);
        if (distanceSqr > MARTA_TELEPORT_DISTANCE_SQR) {
            marta.teleportTo(this.soa.getX() + 1.0D, this.soa.getY() + 0.5D, this.soa.getZ() + 1.0D);
            marta.getNavigation().stop();
            return;
        }

        if (distanceSqr > MARTA_FOLLOW_DISTANCE_SQR && marta.getNavigation().isDone()) {
            marta.getNavigation().moveTo(this.soa, 0.55D);
        }
    }

    public void addAdditionalSaveData(CompoundTag compound) {
        if (this.martaUuid != null) {
            compound.putUUID(MARTA_COMPANION_UUID_TAG, this.martaUuid);
        }
    }

    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.hasUUID(MARTA_COMPANION_UUID_TAG)) {
            this.martaUuid = compound.getUUID(MARTA_COMPANION_UUID_TAG);
        } else {
            this.martaUuid = null;
        }
    }

    public void discardCompanion() {
        if (!(this.soa.level() instanceof ServerLevel serverLevel)) {
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

    public static boolean isGoldenVariantMarta(Entity entity) {
        if (!(entity instanceof Allay)) {
            return false;
        }
        if (entity.getTags().contains(MARTA_GOLDEN_VARIANT_TAG)) {
            return true;
        }
        Component customName = entity.getCustomName();
        if (customName == null || GOLD_TEXT_COLOR == null || !GOLD_TEXT_COLOR.equals(customName.getStyle().getColor())) {
            return false;
        }
        String visibleName = customName.getString().toLowerCase(Locale.ROOT);
        return visibleName.equals("marta") || visibleName.contains("marta");
    }

    public static boolean mayInteract(Player player, Entity target) {
        return !isProtectedMarta(target) || player.isCreative() || player.isSpectator();
    }

    public static boolean shouldBlockInteraction(Player player, Entity target) {
        return isProtectedMarta(target) && !mayInteract(player, target);
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

        marta.moveTo(this.soa.getX() + 1.0D, this.soa.getY() + 0.25D, this.soa.getZ() + 1.0D, this.soa.getYRot(), 0.0F);
        this.keepMartaStyledAndProtected(marta);
        if (serverLevel.addFreshEntity(marta)) {
            this.martaUuid = marta.getUUID();
            return marta;
        }
        return null;
    }

    private Allay findMarta(ServerLevel serverLevel) {
        if (this.martaUuid != null && serverLevel.getEntity(this.martaUuid) instanceof Allay marta && this.isOwnedMarta(marta)) {
            return marta;
        }

        AABB searchArea = this.soa.getBoundingBox().inflate(MARTA_SEARCH_RADIUS);
        List<Allay> candidates = serverLevel.getEntitiesOfClass(Allay.class, searchArea, this::isOwnedMarta);
        if (candidates.isEmpty()) {
            this.martaUuid = null;
            return null;
        }

        Allay nearest = candidates.get(0);
        double nearestDistance = nearest.distanceToSqr(this.soa);
        for (int i = 1; i < candidates.size(); i++) {
            Allay candidate = candidates.get(i);
            double distance = candidate.distanceToSqr(this.soa);
            if (distance < nearestDistance) {
                nearest = candidate;
                nearestDistance = distance;
            }
        }

        this.martaUuid = nearest.getUUID();
        return nearest;
    }

    private boolean isOwnedMarta(Allay allay) {
        return allay.getTags().contains(MARTA_TAG)
                && allay.getPersistentData().getString(MARTA_OWNER_TAG).equals(this.soa.getStringUUID());
    }

    private void keepMartaStyledAndProtected(Allay marta) {
        marta.addTag(MARTA_TAG);
        marta.getPersistentData().putString(MARTA_OWNER_TAG, this.soa.getStringUUID());
        boolean goldenVariant = HcCharactersGameplayConfig.soaMarjorieMartaGoldenVariant();
        Component displayName = LocalizedText.literal("entity.hc_characters.marta");
        marta.setCustomName(goldenVariant ? displayName.copy().withStyle(ChatFormatting.GOLD) : displayName);
        marta.setCustomNameVisible(true);
        marta.setPersistenceRequired();
        marta.setCanPickUpLoot(false);
        marta.setInvulnerable(true);
        if (marta instanceof Mob mob) {
            mob.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
            mob.setDropChance(EquipmentSlot.OFFHAND, 0.0F);
        }

        if (goldenVariant) {
            marta.addTag(MARTA_GOLDEN_VARIANT_TAG);
            marta.getPersistentData().putString(MARTA_VARIANT_TAG, GOLDEN_VARIANT);
        } else {
            marta.removeTag(MARTA_GOLDEN_VARIANT_TAG);
            marta.getPersistentData().remove(MARTA_VARIANT_TAG);
        }
    }
}
