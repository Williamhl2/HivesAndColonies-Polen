package com.hivesandcolonies.hccharacters.common.npc.relationship;

import java.util.function.Consumer;
import java.util.function.IntFunction;

import com.hivesandcolonies.hccharacters.bootstrap.HcCharacters;
import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.common.network.ClientboundNpcAffinityNotificationPayload;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NpcRelationshipManager {
    public static final IntFunction<String> DEFAULT_RANK_RESOLVER = NpcRelationshipLevels::defaultRankName;

    private NpcRelationshipManager() {
    }

    public static NpcRelationshipRecord get(ServerPlayer player, String characterId) {
        return savedData(player).getOrCreate(characterId, player.getUUID());
    }

    public static NpcRelationshipRecord touch(
            ServerPlayer player,
            String characterId,
            String displayName,
            String reasonText,
            IntFunction<String> rankResolver
    ) {
        NpcRelationshipSavedData savedData = savedData(player);
        NpcRelationshipRecord record = savedData.getOrCreate(characterId, player.getUUID());
        record.incrementInteractions();
        record.setLastInteractionGameTime(player.level().getGameTime());
        savedData.setDirty();
        return record;
    }

    public static void addAffinity(
            ServerPlayer player,
            String characterId,
            String displayName,
            int amount,
            String reasonKey,
            String reasonText,
            IntFunction<String> rankResolver
    ) {
        NpcRelationshipSavedData savedData = savedData(player);
        NpcRelationshipRecord record = savedData.getOrCreate(characterId, player.getUUID());
        record.setLastInteractionGameTime(player.level().getGameTime());
        addAffinity(savedData, player, record, displayName, amount, reasonKey, reasonText, rankResolver);
    }

    public static boolean addAffinityWithCooldown(
            ServerPlayer player,
            String characterId,
            String displayName,
            int amount,
            String reasonKey,
            String reasonText,
            long cooldownTicks,
            IntFunction<String> rankResolver
    ) {
        NpcRelationshipSavedData savedData = savedData(player);
        NpcRelationshipRecord record = savedData.getOrCreate(characterId, player.getUUID());
        long now = player.level().getGameTime();
        if (cooldownTicks > 0L && record.isCooldownActive(reasonKey, now)) {
            return false;
        }
        if (cooldownTicks > 0L) {
            record.setCooldown(reasonKey, now + cooldownTicks);
        }
        record.setLastInteractionGameTime(now);
        addAffinity(savedData, player, record, displayName, amount, reasonKey, reasonText, rankResolver);
        return true;
    }

    public static void mutate(ServerPlayer player, String characterId, Consumer<NpcRelationshipRecord> consumer) {
        NpcRelationshipSavedData savedData = savedData(player);
        consumer.accept(savedData.getOrCreate(characterId, player.getUUID()));
        savedData.setDirty();
    }

    public static boolean hasFlag(ServerPlayer player, String characterId, String flag) {
        return get(player, characterId).hasFlag(flag);
    }

    public static void setFlag(ServerPlayer player, String characterId, String flag) {
        mutate(player, characterId, record -> record.setFlag(flag));
    }

    public static int incrementCounter(ServerPlayer player, String characterId, String counter) {
        NpcRelationshipSavedData savedData = savedData(player);
        NpcRelationshipRecord record = savedData.getOrCreate(characterId, player.getUUID());
        int value = record.incrementCounter(counter);
        savedData.setDirty();
        return value;
    }

    public static void markDirty(ServerPlayer player) {
        savedData(player).setDirty();
    }

    public static void notifyAffinityChange(
            ServerPlayer player,
            String displayName,
            int amount,
            int oldAffinity,
            int newAffinity,
            String rankName,
            boolean levelUp,
            String reasonText
    ) {
        notifyClient(player, displayName, amount, oldAffinity, newAffinity, rankName, levelUp, reasonText);
    }

    private static void addAffinity(
            NpcRelationshipSavedData savedData,
            ServerPlayer player,
            NpcRelationshipRecord record,
            String displayName,
            int amount,
            String reasonKey,
            String reasonText,
            IntFunction<String> rankResolver
    ) {
        if (amount == 0) {
            savedData.setDirty();
            return;
        }

        int oldAffinity = record.affinity();
        int oldRank = NpcRelationshipLevels.rankIndex(oldAffinity);
        record.setAffinity(oldAffinity + amount);
        int newAffinity = record.affinity();
        int newRank = NpcRelationshipLevels.rankIndex(newAffinity);
        savedData.setDirty();

        if (newAffinity == oldAffinity) {
            return;
        }

        String rankName = rankResolver.apply(newAffinity);
        boolean levelUp = newRank > oldRank;
        notifyClient(player, displayName, amount, oldAffinity, newAffinity, rankName, levelUp, reasonText);
        if (HcCharactersGameplayConfig.showAffinityDebugChat()) {
            player.displayClientMessage(Component.literal("[Afinidad] " + displayName + " "
                    + signed(amount) + " (" + oldAffinity + " -> " + newAffinity + ") " + reasonKey), false);
        }
        HcCharacters.LOGGER.debug("[NpcRelationship] {} {} {} ({} -> {}) reason={}",
                player.getGameProfile().getName(), displayName, signed(amount), oldAffinity, newAffinity, reasonKey);
    }

    private static void notifyClient(
            ServerPlayer player,
            String displayName,
            int amount,
            int oldAffinity,
            int newAffinity,
            String rankName,
            boolean levelUp,
            String reasonText
    ) {
        if (!HcCharactersGameplayConfig.showAffinityNotifications()) {
            return;
        }
        PacketDistributor.sendToPlayer(player, new ClientboundNpcAffinityNotificationPayload(
                displayName,
                amount,
                oldAffinity,
                newAffinity,
                rankName,
                levelUp,
                reasonText == null ? "" : reasonText
        ));
    }

    private static String signed(int value) {
        return value > 0 ? "+" + value : Integer.toString(value);
    }

    private static NpcRelationshipSavedData savedData(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            throw new IllegalStateException("Npc relationships are server-side only");
        }
        return NpcRelationshipSavedData.get(serverLevel);
    }
}
