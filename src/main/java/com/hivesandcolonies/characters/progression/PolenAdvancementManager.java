package com.hivesandcolonies.characters.progression;

import com.hivesandcolonies.characters.Characters;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class PolenAdvancementManager {

    private PolenAdvancementManager() {}

    public static final ResourceLocation ROOT =
            ResourceLocation.fromNamespaceAndPath(Characters.MODID, "story/root");

    public static final ResourceLocation FIRST_MEETING =
            ResourceLocation.fromNamespaceAndPath(Characters.MODID, "story/first_meeting");

    public static final ResourceLocation FIRST_TRUST =
            ResourceLocation.fromNamespaceAndPath(Characters.MODID, "story/first_trust");

    public static final ResourceLocation NAME_REVEAL =
            ResourceLocation.fromNamespaceAndPath(Characters.MODID, "story/name_reveal");

    private static void grant(
            ServerPlayer player,
            ResourceLocation advancementId,
            String criterion
    ) {
        AdvancementHolder advancement =
                player.server.getAdvancements().get(advancementId);

        if (advancement == null) {
            return;
        }

        player.getAdvancements().award(advancement, criterion);
    }

    public static void grantRoot(ServerPlayer player) {
        grant(player, ROOT, "started");
    }

    public static void grantFirstMeeting(ServerPlayer player) {
        grantRoot(player);
        grant(player, FIRST_MEETING, "first_meeting");
    }

    public static void grantFirstTrust(ServerPlayer player) {
        grantFirstMeeting(player);
        grant(player, FIRST_TRUST, "first_trust");
    }

    public static void grantNameReveal(ServerPlayer player) {
        grantFirstTrust(player);
        grant(player, NAME_REVEAL, "revealed_name");
    }

    public static final ResourceLocation CHAPTER_0_COMPLETE =
        ResourceLocation.fromNamespaceAndPath(
                Characters.MODID,
                "story/chapter0_complete"
        );

    public static void grantChapter0Complete(
        ServerPlayer player
    ) {
        grantNameReveal(player);
        grant(
                player,
                CHAPTER_0_COMPLETE,
                "chapter0_complete"
        );
    }

    public static final ResourceLocation PLAYER_HAS_SHELTER =
        ResourceLocation.fromNamespaceAndPath(Characters.MODID, "story/player_has_shelter");

    public static void grantPlayerHasShelter(ServerPlayer player) {
        grantChapter0Complete(player);
        grant(player, PLAYER_HAS_SHELTER, "player_has_shelter");
    }
}
