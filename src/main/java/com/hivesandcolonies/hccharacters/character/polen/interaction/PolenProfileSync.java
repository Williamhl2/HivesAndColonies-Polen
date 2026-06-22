package com.hivesandcolonies.hccharacters.character.polen.interaction;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.interests.PolenInterest;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenChapterManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenMemoryType;
import com.hivesandcolonies.hccharacters.common.network.ClientboundPolenProfilePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public final class PolenProfileSync {
    private PolenProfileSync() {
    }

    public static void sendToClient(PolenEntity polen, Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || polen == null) {
            return;
        }

        String storyStageKey = "screen.polen.profile.story."
                + PolenWorldStateManager.storyStage(serverPlayer.serverLevel()).name().toLowerCase(Locale.ROOT);
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundPolenProfilePayload(
                polen.getId(),
                PolenPlayerRelationshipManager.getAffinity(serverPlayer),
                PolenPlayerRelationshipManager.getNextAffinityThreshold(serverPlayer),
                PolenPlayerRelationshipManager.getInteractionCount(serverPlayer),
                PolenChapterManager.getCurrentChapter(serverPlayer),
                PolenPlayerRelationshipManager.isTrustWalkUnlocked(serverPlayer),
                PolenPlayerRelationshipManager.hasAnyGiftCooldown(serverPlayer),
                PolenPlayerRelationshipManager.getRelationshipRankText(serverPlayer),
                storyStageKey,
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.BEES),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.MAGIC),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.COLONIES),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.FOOD),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.DECORATION),
                PolenWorldStateManager.interest(serverPlayer.serverLevel(), PolenInterest.EXPLORATION),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_FLOWER.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_HIVE.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_SOURCE.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_COLONY.getFlag()),
                PolenStoryFlagsManager.hasFlag(serverPlayer.serverLevel(), PolenMemoryType.FIRST_RESIDENCE.getFlag())
        ));
    }
}
