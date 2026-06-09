package com.hivesandcolonies.hccharacters.common.client.hud;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import com.hivesandcolonies.hccharacters.bootstrap.config.HcCharactersGameplayConfig;
import com.hivesandcolonies.hccharacters.common.network.ClientboundNpcAffinityNotificationPayload;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public final class NpcAffinityOverlayClient {
    public static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath("hc_characters", "npc_affinity_overlay");

    private static final int WIDTH = 250;
    private static final int HEIGHT = 56;
    private static final int PADDING = 7;
    private static final int GAP = 6;
    private static final int MAX_VISIBLE = 3;
    private static final float TEXT_SCALE = 0.75F;
    private static final Deque<Notification> NOTIFICATIONS = new ArrayDeque<>();

    private NpcAffinityOverlayClient() {
    }

    public static void enqueue(ClientboundNpcAffinityNotificationPayload payload) {
        if (!HcCharactersGameplayConfig.showAffinityNotifications()) {
            return;
        }
        NOTIFICATIONS.addLast(new Notification(payload, System.currentTimeMillis()));
        while (NOTIFICATIONS.size() > 6) {
            NOTIFICATIONS.removeFirst();
        }
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.screen != null || NOTIFICATIONS.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        long duration = Math.max(1000L, HcCharactersGameplayConfig.affinityNotificationDurationTicks() * 50L);
        NOTIFICATIONS.removeIf(notification -> now - notification.createdAtMillis > duration);
        if (NOTIFICATIONS.isEmpty()) {
            return;
        }

        Font font = minecraft.font;
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int x = screenWidth - WIDTH - 12;
        int y = 18;
        int rendered = 0;

        for (Notification notification : NOTIFICATIONS) {
            if (rendered >= MAX_VISIBLE) {
                break;
            }
            renderNotification(guiGraphics, font, notification.payload, x, y + rendered * (HEIGHT + GAP), now - notification.createdAtMillis, duration);
            rendered++;
        }
    }

    private static void renderNotification(
            GuiGraphics guiGraphics,
            Font font,
            ClientboundNpcAffinityNotificationPayload payload,
            int x,
            int y,
            long ageMillis,
            long durationMillis
    ) {
        int alpha = alphaFor(ageMillis, durationMillis);
        if (alpha <= 0) {
            return;
        }

        int background = alpha << 24 | 0x18120E;
        int border = alpha << 24 | (payload.delta() >= 0 ? 0xD4AF37 : 0x8F3D38);
        int text = alpha << 24 | 0xF6E9D0;
        int muted = alpha << 24 | 0xBDAF9A;
        int positive = alpha << 24 | 0xE4C76A;
        int negative = alpha << 24 | 0xE2827D;
        int fill = alpha << 24 | 0x4F3922;

        guiGraphics.fill(x, y, x + WIDTH, y + HEIGHT, background);
        guiGraphics.fill(x, y, x + WIDTH, y + 1, border);
        guiGraphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, border);
        guiGraphics.fill(x, y, x + 1, y + HEIGHT, border);
        guiGraphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, border);

        int scaledTextWidth = (int) ((WIDTH - PADDING * 2) / TEXT_SCALE);
        String deltaText = payload.delta() > 0 ? "+" + payload.delta() : Integer.toString(payload.delta());
        String title = payload.levelUp() ? "Nuevo rango con " + payload.characterName() : payload.characterName() + "  " + deltaText + " afinidad";
        drawScaledString(guiGraphics, font, trim(font, title, scaledTextWidth), x + PADDING, y + 6, text);

        String message = payload.levelUp() ? payload.rankName() + ": " + payload.message() : payload.message();
        List<FormattedCharSequence> messageLines = font.split(Component.literal(message), scaledTextWidth);
        if (!messageLines.isEmpty()) {
            drawScaledString(guiGraphics, font, messageLines.get(0), x + PADDING, y + 18, muted);
        }
        if (messageLines.size() > 1) {
            drawScaledString(guiGraphics, font, messageLines.get(1), x + PADDING, y + 28, muted);
        }

        int barX = x + PADDING;
        int barY = y + HEIGHT - 8;
        int barWidth = WIDTH - PADDING * 2;
        int progressWidth = Math.max(2, (int) (barWidth * (payload.newAffinity() / 100.0D)));
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 3, fill);
        guiGraphics.fill(barX, barY, barX + progressWidth, barY + 3, payload.delta() >= 0 ? positive : negative);
    }

    private static void drawScaledString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        guiGraphics.drawString(font, text, Math.round(x / TEXT_SCALE), Math.round(y / TEXT_SCALE), color, false);
        guiGraphics.pose().popPose();
    }

    private static void drawScaledString(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        guiGraphics.drawString(font, text, Math.round(x / TEXT_SCALE), Math.round(y / TEXT_SCALE), color, false);
        guiGraphics.pose().popPose();
    }

    private static int alphaFor(long ageMillis, long durationMillis) {
        long fadeMillis = Math.min(450L, durationMillis / 3L);
        if (ageMillis < fadeMillis) {
            return (int) (255L * ageMillis / fadeMillis);
        }
        long remaining = durationMillis - ageMillis;
        if (remaining < fadeMillis) {
            return Math.max(0, (int) (255L * remaining / fadeMillis));
        }
        return 255;
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, Math.max(0, maxWidth - font.width("..."))) + "...";
    }

    private record Notification(ClientboundNpcAffinityNotificationPayload payload, long createdAtMillis) {
    }
}
