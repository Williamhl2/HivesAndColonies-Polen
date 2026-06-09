package com.hivesandcolonies.hccharacters.common.npc.relationship;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class NpcRelationshipMemory {
    private static final String ROOT_KEY = "hc_characters_npc_relationships";
    private static final String PLAYERS_KEY = "players";
    private static final String INTERACTIONS_KEY = "interactions";
    private static final String TRUST_KEY = "trust";
    private static final String LAST_INTERACTION_KEY = "last_interaction";
    private static final String NEXT_REWARD_KEY = "next_reward";
    private static final String SPECIAL_REWARDS_KEY = "special_rewards";

    private final Entity npc;
    private final String profileId;

    public NpcRelationshipMemory(Entity npc, String profileId) {
        this.npc = npc;
        this.profileId = profileId;
    }

    public PlayerRecord touch(Player player) {
        PlayerRecord record = this.get(player);
        record.interactions += 1;
        record.trust = Math.min(100, record.trust + 1);
        record.lastInteractionGameTime = this.npc.level().getGameTime();
        this.put(player, record);
        return record;
    }

    public PlayerRecord get(Player player) {
        CompoundTag data = this.playerTag(player.getUUID());
        return new PlayerRecord(
                data.getInt(INTERACTIONS_KEY),
                data.getInt(TRUST_KEY),
                data.getLong(LAST_INTERACTION_KEY),
                data.getLong(NEXT_REWARD_KEY),
                data.getInt(SPECIAL_REWARDS_KEY)
        );
    }

    public void put(Player player, PlayerRecord record) {
        CompoundTag data = this.playerTag(player.getUUID());
        data.putInt(INTERACTIONS_KEY, record.interactions);
        data.putInt(TRUST_KEY, record.trust);
        data.putLong(LAST_INTERACTION_KEY, record.lastInteractionGameTime);
        data.putLong(NEXT_REWARD_KEY, record.nextRewardGameTime);
        data.putInt(SPECIAL_REWARDS_KEY, record.specialRewards);
    }

    private CompoundTag playerTag(UUID playerId) {
        CompoundTag root = this.npc.getPersistentData();
        CompoundTag relationships = root.getCompound(ROOT_KEY);
        CompoundTag profile = relationships.getCompound(this.profileId);
        CompoundTag players = profile.getCompound(PLAYERS_KEY);
        String key = playerId.toString();
        CompoundTag data = players.getCompound(key);
        players.put(key, data);
        profile.put(PLAYERS_KEY, players);
        relationships.put(this.profileId, profile);
        root.put(ROOT_KEY, relationships);
        return data;
    }

    public static final class PlayerRecord {
        public int interactions;
        public int trust;
        public long lastInteractionGameTime;
        public long nextRewardGameTime;
        public int specialRewards;

        private PlayerRecord(int interactions, int trust, long lastInteractionGameTime, long nextRewardGameTime, int specialRewards) {
            this.interactions = interactions;
            this.trust = trust;
            this.lastInteractionGameTime = lastInteractionGameTime;
            this.nextRewardGameTime = nextRewardGameTime;
            this.specialRewards = specialRewards;
        }

        public int trustTier() {
            if (this.trust >= 18 || this.interactions >= 18) {
                return 3;
            }
            if (this.trust >= 10 || this.interactions >= 10) {
                return 2;
            }
            if (this.trust >= 4 || this.interactions >= 4) {
                return 1;
            }
            return 0;
        }
    }
}
