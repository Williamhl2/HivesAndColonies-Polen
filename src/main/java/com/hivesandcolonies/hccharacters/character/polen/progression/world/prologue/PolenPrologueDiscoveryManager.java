package com.hivesandcolonies.hccharacters.character.polen.progression.world.prologue;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModItems;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.story.PolenStoryStage;
import com.hivesandcolonies.hccharacters.character.polen.interaction.PolenProfileSync;
import com.hivesandcolonies.hccharacters.character.polen.item.focus.HiveheartCharmItem;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenRelationshipEvents;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.hccharacters.character.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStateManager;
import com.hivesandcolonies.hccharacters.character.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.hccharacters.character.polen.story.PolenStoryEventManager;
import com.hivesandcolonies.hccharacters.character.polen.world.PolenSingletonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class PolenPrologueDiscoveryManager {
    private static final int DISCOVERY_CHECK_INTERVAL = 20;
    private static final double CLEARING_DISCOVERY_RADIUS = PolenPrologueSiteLayout.FLOWER_RING_RADIUS + 10.0D;
    private static final double POLEN_DISCOVERY_RADIUS = 18.0D;

    private PolenPrologueDiscoveryManager() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        if (overworld == null || overworld.getGameTime() % DISCOVERY_CHECK_INTERVAL != 0L) {
            return;
        }

        PolenWorldStoryData data = PolenWorldStateManager.get(overworld);
        if (PolenPrologueRuntime.isLocatorDormant(overworld, data)) {
            return;
        }

        PolenEntity polen = PolenSingletonManager.findLivingPolen(overworld);
        BlockPos clearingCenter = data.getPrologueClearingCenter();
        if (polen == null || clearingCenter == null) {
            return;
        }

        for (ServerPlayer player : overworld.players()) {
            if (shouldTriggerDiscovery(player, polen, clearingCenter)) {
                completeDiscovery(overworld, player, polen);
                break;
            }
        }
    }

    private static boolean shouldTriggerDiscovery(ServerPlayer player, PolenEntity polen, BlockPos clearingCenter) {
        if (player == null || polen == null || clearingCenter == null || !player.isAlive() || player.isSpectator()) {
            return false;
        }

        Vec3 clearingCenterPos = Vec3.atCenterOf(clearingCenter);
        return player.distanceToSqr(clearingCenterPos) <= CLEARING_DISCOVERY_RADIUS * CLEARING_DISCOVERY_RADIUS
                && player.distanceToSqr(polen) <= POLEN_DISCOVERY_RADIUS * POLEN_DISCOVERY_RADIUS
                && player.hasLineOfSight(polen);
    }

    private static void completeDiscovery(ServerLevel level, ServerPlayer player, PolenEntity polen) {
        PolenStoryFlagsManager.setFlag(level, PolenStoryFlag.PROLOGUE_SITE_DISCOVERED);
        PolenWorldStateManager.setStoryStage(level, PolenStoryStage.SETTLING);
        PolenWorldStateManager.rememberLastKnownPosition(level, polen.blockPosition());

        clearHiveheartTargets(player);
        PolenRelationshipEvents.firstMeeting(player);
        PolenAdvancementManager.grantFirstMeeting(player);
        PolenStoryEventManager.playFirstMeeting(player);
        PolenProfileSync.sendToClient(polen, player);
    }

    private static void clearHiveheartTargets(ServerPlayer player) {
        if (player == null) {
            return;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(ModItems.HIVEHEART_CHARM.get())) {
                HiveheartCharmItem.clearTarget(stack);
            }
        }
    }
}
