package com.hivesandcolonies.hccharacters.character.soa.world;

import com.hivesandcolonies.hccharacters.character.soa.companion.SoaMartaCompanionController;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class SoaMarjorieCompanionEvents {
    private SoaMarjorieCompanionEvents() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (shouldBlockMartaInteraction(event.getEntity(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (shouldBlockMartaInteraction(event.getEntity(), event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static boolean shouldBlockMartaInteraction(Player player, Entity target) {
        return SoaMartaCompanionController.shouldBlockInteraction(player, target);
    }
}
