package com.hivesandcolonies.hccharacters.character.lucy.world;

import java.util.List;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModEntities;
import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.hivesandcolonies.hccharacters.character.lucy.entity.LucyEntity;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue.PolenPrologueManager;
import com.hivesandcolonies.hccharacters.character.soa.dialogue.SoaMarjorieDialogue;
import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;
import com.hivesandcolonies.hccharacters.common.npc.relationship.NpcRelationshipManager;
import com.hivesandcolonies.hccharacters.common.util.LocalizedText;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class LucyVillageEncounterManager {
    private static final String SOA_SCENE_INTRO_FLAG = "village_scene_intro";
    private static final int MANAGER_INTERVAL_TICKS = 40;
    private static final int PLAYER_RETRY_COOLDOWN_TICKS = 20 * 45;
    private static final int SCENE_DURATION_TICKS = 20 * 8 * 60;
    private static final int AMBIENT_INTERVAL_TICKS = 20 * 7;
    private static final double AMBIENT_AUDIENCE_RADIUS = 18.0D;

    private LucyVillageEncounterManager() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % MANAGER_INTERVAL_TICKS != 0) {
            return;
        }

        ServerLevel level = event.getServer().overworld();
        if (level == null) {
            return;
        }

        long now = level.getGameTime();
        LucyVillageEncounterSavedData savedData = LucyVillageEncounterSavedData.get(level);
        savedData.cleanup(now);

        warmVisitedVillageTaverns(level);

        if (PolenPrologueManager.isLocatorDormant(level)) {
            clearScene(level, savedData);
            return;
        }

        if (savedData.hasActiveScene()) {
            tickActiveScene(level, savedData, now);
            if (savedData.hasActiveScene()) {
                return;
            }
        }

        for (ServerPlayer player : level.players()) {
            if (!isPlayerEligible(player) || savedData.isPlayerOnRetryCooldown(player.getUUID(), now)) {
                continue;
            }
            if (trySpawnScene(level, player, savedData, now)) {
                return;
            }
            savedData.setPlayerRetryCooldown(player.getUUID(), now + PLAYER_RETRY_COOLDOWN_TICKS);
        }
    }

    public static InteractionResult handleInteraction(LucyEntity lucy, Player player) {
        if (lucy == null || player == null) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel overworld = serverPlayer.serverLevel().getServer().overworld();
        if (overworld == null || PolenPrologueManager.isLocatorDormant(overworld)) {
            LucyVillageSceneDialogue.playDormant(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        boolean alreadyKnown = PolenPrologueManager.hasReceivedOpeningClueMap(serverPlayer);
        boolean granted = PolenPrologueManager.grantOpeningClueMap(serverPlayer);
        if (granted) {
            PolenStoryFlagsManager.setFlag(serverPlayer.serverLevel(), PolenStoryFlag.LUCY_SOA_VILLAGE_EVENT_UNLOCKED);
            if (alreadyKnown) {
                LucyVillageSceneDialogue.playReplacementClue(serverPlayer);
            } else {
                LucyVillageSceneDialogue.playFirstClue(serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }

        if (serverPlayer.getInventory().contains(new ItemStack(ModItems.HIVEHEART_CHARM.get()))) {
            LucyVillageSceneDialogue.playAlreadyHoldingClue(serverPlayer);
        } else {
            LucyVillageSceneDialogue.playDormant(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult handleSoaInteraction(SoaMarjorieEntity soa, Player player) {
        if (soa == null || player == null) {
            return InteractionResult.PASS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        soa.getLookControl().setLookAt(player, 30.0F, 30.0F);
        if (!NpcRelationshipManager.hasFlag(serverPlayer, SoaMarjorieDialogue.PROFILE_ID, SOA_SCENE_INTRO_FLAG)) {
            NpcRelationshipManager.setFlag(serverPlayer, SoaMarjorieDialogue.PROFILE_ID, SOA_SCENE_INTRO_FLAG);
            LucyVillageSceneDialogue.playSoaIntroduction(serverPlayer);
            return InteractionResult.SUCCESS;
        }

        LucyVillageSceneDialogue.playSoaRepeat(serverPlayer);
        return InteractionResult.SUCCESS;
    }

    public static boolean hasActiveVillageScene(ServerLevel level) {
        return level != null && LucyVillageEncounterSavedData.get(level).hasActiveScene();
    }

    public static boolean isVillageSceneSoa(SoaMarjorieEntity soa) {
        return soa != null && soa.isVillageSceneActive();
    }

    private static void warmVisitedVillageTaverns(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player == null || player.isSpectator() || !player.level().dimension().equals(Level.OVERWORLD)) {
                continue;
            }
            LucyVillageTavernManager.ensureTavernGenerated(level, player.blockPosition());
        }
    }

    private static boolean trySpawnScene(
            ServerLevel level,
            ServerPlayer player,
            LucyVillageEncounterSavedData savedData,
            long now
    ) {
        LucyVillageSceneLocator.SceneLocation scene = LucyVillageTavernManager.ensureSceneLocation(level, player.blockPosition());
        if (scene == null) {
            return false;
        }

        LucyEntity lucy = ModEntities.LUCY.get().create(level);
        SoaMarjorieEntity soa = ModEntities.SOA_MARJORIE.get().create(level);
        if (lucy == null || soa == null) {
            return false;
        }

        lucy.moveTo(scene.lucyPos().getX() + 0.5D, scene.lucyPos().getY(), scene.lucyPos().getZ() + 0.5D, 180.0F, 0.0F);
        lucy.setCustomName(LocalizedText.literal("dialogue.lucy.scene.speaker.lucy"));
        lucy.startVillageScene(scene.anchorPos());
        lucy.setInvulnerable(true);
        lucy.setPersistenceRequired();

        soa.moveTo(scene.soaPos().getX() + 0.5D, scene.soaPos().getY(), scene.soaPos().getZ() + 0.5D, 0.0F, 0.0F);
        soa.setCustomName(LocalizedText.literal("dialogue.lucy.scene.speaker.soa"));
        soa.setCustomNameVisible(true);
        soa.startVillageScene(scene.soaPos(), scene.lucyPos());
        soa.setInvulnerable(true);
        soa.setPersistenceRequired();

        if (!level.addFreshEntity(lucy) || !level.addFreshEntity(soa)) {
            if (lucy.isAlive()) {
                lucy.discard();
            }
            if (soa.isAlive()) {
                soa.discard();
            }
            return false;
        }

        savedData.setActiveScene(scene.anchorPos(), lucy.getUUID(), soa.getUUID(), now + SCENE_DURATION_TICKS, now + (AMBIENT_INTERVAL_TICKS / 2));
        return true;
    }

    private static void tickActiveScene(ServerLevel level, LucyVillageEncounterSavedData savedData, long now) {
        LucyEntity lucy = resolveEntity(level, savedData.getActiveLucyUuid(), LucyEntity.class);
        SoaMarjorieEntity soa = resolveEntity(level, savedData.getActiveSoaUuid(), SoaMarjorieEntity.class);
        if (lucy == null || soa == null || !lucy.isAlive() || !soa.isAlive() || now >= savedData.getActiveUntil()) {
            clearScene(level, savedData, lucy, soa);
            return;
        }

        BlockPos anchor = savedData.getActiveAnchor();
        if (anchor == null) {
            clearScene(level, savedData, lucy, soa);
            return;
        }

        lucy.startVillageScene(anchor);
        lucy.setInvulnerable(true);
        soa.setInvulnerable(true);
        soa.startVillageScene(
                soa.getVillageSceneAnchor() == null ? soa.blockPosition() : soa.getVillageSceneAnchor(),
                lucy.blockPosition()
        );

        if (now < savedData.getNextAmbientTick()) {
            return;
        }

        List<ServerPlayer> audience = level.getEntitiesOfClass(
                ServerPlayer.class,
                new AABB(anchor).inflate(AMBIENT_AUDIENCE_RADIUS),
                player -> !player.isSpectator()
        );
        if (audience.isEmpty()) {
            savedData.advanceAmbient(now + AMBIENT_INTERVAL_TICKS, savedData.getAmbientStep());
            return;
        }

        int step = savedData.getAmbientStep();
        if (!LucyVillageSceneDialogue.hasAmbientLine(step)) {
            savedData.advanceAmbient(savedData.getActiveUntil() + 1L, step);
            return;
        }

        for (ServerPlayer player : audience) {
            LucyVillageSceneDialogue.sendAmbientLine(player, step);
        }
        savedData.advanceAmbient(now + AMBIENT_INTERVAL_TICKS, step + 1);
    }

    private static boolean isPlayerEligible(ServerPlayer player) {
        return player != null
                && !player.isSpectator()
                && player.level().dimension().equals(Level.OVERWORLD)
                && PolenPrologueManager.shouldOfferOpeningClueMap(player);
    }

    private static void clearScene(ServerLevel level, LucyVillageEncounterSavedData savedData) {
        LucyEntity lucy = resolveEntity(level, savedData.getActiveLucyUuid(), LucyEntity.class);
        SoaMarjorieEntity soa = resolveEntity(level, savedData.getActiveSoaUuid(), SoaMarjorieEntity.class);
        clearScene(level, savedData, lucy, soa);
    }

    private static void clearScene(
            ServerLevel level,
            LucyVillageEncounterSavedData savedData,
            LucyEntity lucy,
            SoaMarjorieEntity soa
    ) {
        discardEntity(lucy);
        discardEntity(soa);
        savedData.clearActiveScene();
    }

    private static <T extends Entity> T resolveEntity(ServerLevel level, java.util.UUID uuid, Class<T> type) {
        if (level == null || uuid == null) {
            return null;
        }
        Entity entity = level.getEntity(uuid);
        return type.isInstance(entity) ? type.cast(entity) : null;
    }

    private static void discardEntity(Entity entity) {
        if (entity != null && entity.isAlive()) {
            entity.discard();
        }
    }
}
