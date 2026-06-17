package com.hivesandcolonies.hccharacters.character.soa.world;

import com.hivesandcolonies.hccharacters.character.soa.entity.SoaMarjorieEntity;

import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class SoaMarjorieCompanionEvents {
    private SoaMarjorieCompanionEvents() {
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (SoaMarjorieEntity.isProtectedMarta(event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (SoaMarjorieEntity.isProtectedMarta(event.getTarget())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
