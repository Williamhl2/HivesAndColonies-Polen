package com.hivesandcolonies.hccharacters.character.lucy.world;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class LucyVillageSceneDialogue {
    private static final Component LUCY_NAME = Component.translatable("entity.hc_characters.lucy").withStyle(ChatFormatting.AQUA);
    private static final Component SOA_NAME = Component.translatable("entity.hc_characters.soa_marjorie").withStyle(ChatFormatting.GOLD);

    private static final List<DialogueLine> AMBIENT_LINES = List.of(
            new DialogueLine(SOA_NAME, "dialogue.lucy.scene.ambient.soa.1"),
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.ambient.lucy.1"),
            new DialogueLine(SOA_NAME, "dialogue.lucy.scene.ambient.soa.2"),
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.ambient.lucy.2")
    );

    private static final List<DialogueLine> FIRST_CLUE_LINES = List.of(
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.clue.line1"),
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.clue.line2"),
            new DialogueLine(SOA_NAME, "dialogue.lucy.scene.clue.line3"),
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.clue.line4")
    );

    private static final List<DialogueLine> REPLACEMENT_CLUE_LINES = List.of(
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.replacement.line1"),
            new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.replacement.line2")
    );

    private static final List<DialogueLine> SOA_INTRO_LINES = List.of(
            new DialogueLine(SOA_NAME, "dialogue.lucy.scene.soa_intro.line1"),
            new DialogueLine(SOA_NAME, "dialogue.lucy.scene.soa_intro.line2")
    );

    private static final List<DialogueLine> SOA_REPEAT_LINES = List.of(
            new DialogueLine(SOA_NAME, "dialogue.lucy.scene.soa_repeat.line1")
    );

    private LucyVillageSceneDialogue() {
    }

    public static int ambientLineCount() {
        return AMBIENT_LINES.size();
    }

    public static void sendAmbientLine(ServerPlayer player, int step) {
        if (player == null || AMBIENT_LINES.isEmpty()) {
            return;
        }
        sendLine(player, AMBIENT_LINES.get(Math.floorMod(step, AMBIENT_LINES.size())));
    }

    public static void playFirstClue(Player player) {
        sendDialogueSequence(player, FIRST_CLUE_LINES);
        player.displayClientMessage(
                Component.translatable("dialogue.lucy.scene.clue.received").withStyle(ChatFormatting.GOLD),
                false
        );
    }

    public static void playReplacementClue(Player player) {
        sendDialogueSequence(player, REPLACEMENT_CLUE_LINES);
    }

    public static void playAlreadyHoldingClue(Player player) {
        sendLine(player, new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.repeat.line1"));
    }

    public static void playDormant(Player player) {
        sendLine(player, new DialogueLine(LUCY_NAME, "dialogue.lucy.scene.after_discovery.line1"));
    }

    public static void playSoaIntroduction(Player player) {
        sendDialogueSequence(player, SOA_INTRO_LINES);
    }

    public static void playSoaRepeat(Player player) {
        sendDialogueSequence(player, SOA_REPEAT_LINES);
    }

    private static void sendDialogueSequence(Player player, List<DialogueLine> lines) {
        for (DialogueLine line : lines) {
            sendLine(player, line);
        }
    }

    private static void sendLine(Player player, DialogueLine line) {
        if (player == null || line == null) {
            return;
        }

        player.displayClientMessage(
                line.speaker().copy()
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable(line.dialogueKey()).withStyle(ChatFormatting.WHITE)),
                false
        );
    }

    private record DialogueLine(Component speaker, String dialogueKey) {
    }
}
