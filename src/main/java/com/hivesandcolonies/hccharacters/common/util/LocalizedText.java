package com.hivesandcolonies.hccharacters.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class LocalizedText {
    private LocalizedText() {
    }

    public static String resolve(String textOrKey) {
        if (textOrKey == null || textOrKey.isBlank()) {
            return "";
        }
        return Language.getInstance().has(textOrKey)
                ? Language.getInstance().getOrDefault(textOrKey)
                : textOrKey;
    }

    public static MutableComponent literal(String textOrKey) {
        return Component.literal(resolve(textOrKey));
    }

    public static MutableComponent dialogue(String speakerTextOrKey, ChatFormatting speakerStyle, String dialogueTextOrKey) {
        return literal(speakerTextOrKey).withStyle(speakerStyle)
                .append(Component.literal(": ").withStyle(ChatFormatting.WHITE))
                .append(literal(dialogueTextOrKey).withStyle(ChatFormatting.WHITE));
    }
}
