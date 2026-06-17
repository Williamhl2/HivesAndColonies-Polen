package com.hivesandcolonies.hccharacters.character.polen.client.screen;

import com.hivesandcolonies.hccharacters.character.polen.client.profile.PolenProfileClientActions;
import com.hivesandcolonies.hccharacters.character.polen.client.profile.PolenProfileView;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class PolenProfileScreen extends Screen {
    private static final int PANEL_WIDTH = 362;
    private static final int PANEL_HEIGHT = 244;
    private static final int PANEL_COLOR = 0xFF181026;
    private static final int PANEL_BORDER = 0xFF8C6FD1;
    private static final int SECTION_COLOR = 0xCC271E3C;
    private static final int TEXT = 0xFFEFEAFB;
    private static final int MUTED_TEXT = 0xFFC7B9DD;
    private static final int GOLD = 0xFFFFD86A;
    private static final int BAR_BG = 0xFF15101F;
    private static final int BAR_FILL = 0xFFE6C34A;
    private static final int RADAR_GRID = 0x66504279;
    private static final int RADAR_AXIS = 0x996B5A9C;
    private static final int RADAR_DATA = 0xFFF5C858;
    private static final int RADAR_POINT = 0xFFFFE8A1;
    private static final String[] RADAR_LABELS = {"bees", "magic", "colonies", "food", "decoration", "exploration"};

    private final PolenProfileView profile;
    private int panelX;
    private int panelY;

    public PolenProfileScreen(PolenProfileView profile) {
        super(Component.translatable("screen.polen.profile.title"));
        this.profile = profile;
    }

    @Override
    protected void init() {
        this.panelX = Math.max(8, (this.width - PANEL_WIDTH) / 2);
        this.panelY = Math.max(8, (this.height - PANEL_HEIGHT) / 2);
        this.clearWidgets();

        this.addRenderableWidget(Button.builder(Component.literal("x"), button -> this.onClose())
                .bounds(this.panelX + PANEL_WIDTH - 24, this.panelY + 8, 16, 16)
                .build());

        int buttonY = this.panelY + PANEL_HEIGHT - 30;
        int buttonWidth = 124;
        int buttonGap = 14;
        int buttonStartX = this.panelX + (PANEL_WIDTH - (buttonWidth * 2 + buttonGap)) / 2;

        Button followButton = Button.builder(
                        Component.translatable(this.profile.trustWalkActive()
                                ? "screen.polen.profile.action.stay"
                                : "screen.polen.profile.action.follow"),
                        button -> PolenProfileClientActions.send(this.profile.entityId(), PolenProfileClientActions.FOLLOW_TOGGLE))
                .bounds(buttonStartX, buttonY, buttonWidth, 20)
                .build();
        followButton.active = this.profile.canAskToFollow();
        this.addRenderableWidget(followButton);

        Button returnButton = Button.builder(
                        Component.translatable("screen.polen.profile.action.return_home"),
                        button -> PolenProfileClientActions.send(this.profile.entityId(), PolenProfileClientActions.RETURN_HOME))
                .bounds(buttonStartX + buttonWidth + buttonGap, buttonY, buttonWidth, 20)
                .build();
        returnButton.active = this.profile.canReturnHome();
        this.addRenderableWidget(returnButton);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally empty: this UI is a lightweight overlay.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x22000000);
        drawPanel(graphics);
        drawHeader(graphics);
        drawOverviewCard(graphics);
        drawGiftCard(graphics);
        drawInterestCard(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    private void drawPanel(GuiGraphics graphics) {
        graphics.fill(this.panelX, this.panelY, this.panelX + PANEL_WIDTH, this.panelY + PANEL_HEIGHT, PANEL_COLOR);
        graphics.fill(this.panelX, this.panelY, this.panelX + PANEL_WIDTH, this.panelY + 1, PANEL_BORDER);
        graphics.fill(this.panelX, this.panelY + PANEL_HEIGHT - 1, this.panelX + PANEL_WIDTH, this.panelY + PANEL_HEIGHT, PANEL_BORDER);
        graphics.fill(this.panelX, this.panelY, this.panelX + 1, this.panelY + PANEL_HEIGHT, PANEL_BORDER);
        graphics.fill(this.panelX + PANEL_WIDTH - 1, this.panelY, this.panelX + PANEL_WIDTH, this.panelY + PANEL_HEIGHT, PANEL_BORDER);
    }

    private void drawHeader(GuiGraphics graphics) {
        int x = this.panelX + 18;
        int y = this.panelY + 12;
        graphics.drawString(this.font, this.profile.displayName(), x, y, TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.currentPurposeTitleKey()), x, y + 14, GOLD, false);
        graphics.drawString(this.font, Component.translatable(this.profile.currentStatusKey()), x, y + 27, MUTED_TEXT, false);
    }

    private void drawOverviewCard(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 50;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 68, SECTION_COLOR);

        graphics.drawString(this.font, Component.translatable("screen.polen.profile.relationship"), x + 10, y + 8, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.literal(this.profile.relationshipRankText()), x + 74, y + 8, TEXT, false);
        drawCompactProgress(graphics, x + 10, y + 22, Component.translatable("screen.polen.profile.trust"), this.profile.relationshipAffinity(), this.profile.nextAffinityThreshold());

        graphics.drawString(this.font, Component.translatable(this.profile.followSummaryKey()), x + 10, y + 44, TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.homeSummaryKey()), x + 176, y + 44, TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.giftSummaryKey()), x + 10, y + 56, MUTED_TEXT, false);

        drawWrappedText(
                graphics,
                Component.translatable("screen.polen.profile.next_action.short", Component.translatable(this.profile.nextActionKey())),
                x + 176,
                y + 55,
                w - 186,
                MUTED_TEXT,
                2
        );
    }

    private void drawGiftCard(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 126;
        int w = 154;
        graphics.fill(x, y, x + w, y + 76, SECTION_COLOR);

        graphics.drawString(this.font, Component.translatable("screen.polen.profile.help_now"), x + 10, y + 8, GOLD, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.gifts.wants"), x + 10, y + 24, MUTED_TEXT, false);
        drawWrappedText(graphics, Component.translatable("screen.polen.profile.gifts.hint." + this.profile.giftHintKey()), x + 10, y + 36, w - 20, TEXT, 2);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.hover_more"), x + 10, y + 60, MUTED_TEXT, false);
    }

    private void drawInterestCard(GuiGraphics graphics) {
        int x = this.panelX + 192;
        int y = this.panelY + 126;
        int w = PANEL_WIDTH - 208;
        graphics.fill(x, y, x + w, y + 76, SECTION_COLOR);

        graphics.drawString(this.font, Component.translatable("screen.polen.profile.help_interest"), x + 10, y + 8, GOLD, false);
        drawInterestRadar(graphics, x + 43, y + 47, 23);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.top_interests"), x + 84, y + 20, MUTED_TEXT, false);
        drawWrappedText(graphics, topInterestsComponent(), x + 84, y + 32, w - 94, TEXT, 2);
        drawWrappedText(
                graphics,
                Component.translatable("screen.polen.profile.memories.unlocked", this.profile.unlockedMemoryCount(), this.profile.memoryEntries().size()),
                x + 84,
                y + 56,
                w - 94,
                MUTED_TEXT,
                2
        );
    }

    private void drawCompactProgress(GuiGraphics graphics, int x, int y, Component label, int value, int nextThreshold) {
        int labelWidth = 38;
        int barWidth = 184;
        graphics.drawString(this.font, label, x, y, MUTED_TEXT, false);
        int bx = x + labelWidth;
        graphics.fill(bx, y + 2, bx + barWidth, y + 7, BAR_BG);
        int fill = Math.max(2, Math.min(barWidth, value * barWidth / 100));
        graphics.fill(bx, y + 2, bx + fill, y + 7, BAR_FILL);
        graphics.drawString(this.font, Component.literal(value + "/100"), bx + barWidth + 8, y, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.next_threshold", nextThreshold), x, y + 11, MUTED_TEXT, false);
    }

    private void drawWrappedText(GuiGraphics graphics, Component text, int x, int y, int width, int color, int maxLines) {
        int row = 0;
        for (FormattedCharSequence line : this.font.split(text, width)) {
            if (row >= maxLines) {
                break;
            }
            graphics.drawString(this.font, line, x, y + row * 10, color, false);
            row++;
        }
    }

    private void drawInterestRadar(GuiGraphics graphics, int centerX, int centerY, int radius) {
        for (int ring = 1; ring <= 3; ring++) {
            drawRadarPolygon(graphics, centerX, centerY, radius * ring / 3.0D, RADAR_GRID);
        }

        for (int index = 0; index < RADAR_LABELS.length; index++) {
            int[] outer = radarPoint(centerX, centerY, radius, index, 100);
            drawLine(graphics, centerX, centerY, outer[0], outer[1], RADAR_AXIS);
        }

        int[] xs = new int[RADAR_LABELS.length];
        int[] ys = new int[RADAR_LABELS.length];
        for (int index = 0; index < RADAR_LABELS.length; index++) {
            int[] point = radarPoint(centerX, centerY, radius, index, interestValue(RADAR_LABELS[index]));
            xs[index] = point[0];
            ys[index] = point[1];
            drawLine(graphics, centerX, centerY, xs[index], ys[index], 0x55F5C858);
        }

        for (int index = 0; index < RADAR_LABELS.length; index++) {
            int next = (index + 1) % RADAR_LABELS.length;
            drawLine(graphics, xs[index], ys[index], xs[next], ys[next], RADAR_DATA);
            graphics.fill(xs[index] - 1, ys[index] - 1, xs[index] + 2, ys[index] + 2, RADAR_POINT);
        }

        graphics.fill(centerX - 1, centerY - 1, centerX + 2, centerY + 2, RADAR_POINT);
    }

    private void drawRadarPolygon(GuiGraphics graphics, int centerX, int centerY, double radius, int color) {
        int[] first = radarPoint(centerX, centerY, radius, 0, 100);
        int[] previous = first;
        for (int index = 1; index < RADAR_LABELS.length; index++) {
            int[] point = radarPoint(centerX, centerY, radius, index, 100);
            drawLine(graphics, previous[0], previous[1], point[0], point[1], color);
            previous = point;
        }
        drawLine(graphics, previous[0], previous[1], first[0], first[1], color);
    }

    private int[] radarPoint(int centerX, int centerY, double radius, int index, int value) {
        double amount = Math.max(0.12D, Math.min(1.0D, value / 100.0D));
        double angle = -Math.PI / 2.0D + (Math.PI * 2.0D * index / RADAR_LABELS.length);
        int x = centerX + (int) Math.round(Math.cos(angle) * radius * amount);
        int y = centerY + (int) Math.round(Math.sin(angle) * radius * amount);
        return new int[] {x, y};
    }

    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) {
            graphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }

        for (int step = 0; step <= steps; step++) {
            int x = x1 + (x2 - x1) * step / steps;
            int y = y1 + (y2 - y1) * step / steps;
            graphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int overviewX = this.panelX + 16;
        int overviewY = this.panelY + 50;
        int overviewW = PANEL_WIDTH - 32;

        if (isHovering(mouseX, mouseY, this.panelX + 16, this.panelY + 10, PANEL_WIDTH - 48, 34)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable(this.profile.currentPurposeTitleKey()),
                    Component.translatable(this.profile.currentPurposeBodyKey()),
                    Component.translatable(this.profile.currentStatusKey())
            );
            return;
        }

        if (isHovering(mouseX, mouseY, overviewX + 8, overviewY + 18, 294, 18)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    pair(Component.translatable("screen.polen.profile.relationship"), Component.literal(this.profile.relationshipRankText())),
                    pair(Component.translatable("screen.polen.profile.trust"), Component.literal(this.profile.relationshipAffinity() + "/100")),
                    Component.translatable("screen.polen.profile.next_threshold", this.profile.nextAffinityThreshold())
            );
            return;
        }

        if (isHovering(mouseX, mouseY, overviewX + 8, overviewY + 40, 150, 12)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable(this.profile.followSummaryKey()),
                    Component.translatable(this.profile.trustWalkStateKey())
            );
            return;
        }

        if (isHovering(mouseX, mouseY, overviewX + 172, overviewY + 52, overviewW - 180, 20)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.polen.profile.next_action.short", Component.translatable(this.profile.nextActionKey())),
                    Component.translatable(this.profile.focusHintKey())
            );
            return;
        }

        if (isHovering(mouseX, mouseY, overviewX + 172, overviewY + 40, overviewW - 180, 12)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable(this.profile.homeSummaryKey()),
                    Component.translatable(this.profile.hasHome()
                            ? "screen.polen.profile.actions.home.assigned"
                            : "screen.polen.profile.actions.home.missing"),
                    Component.translatable("screen.polen.profile.home.bed_instruction")
            );
            return;
        }

        if (isHovering(mouseX, mouseY, this.panelX + 16, this.panelY + 126, 154, 76)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.polen.profile.help_now"),
                    pair(
                            Component.translatable("screen.polen.profile.gifts.wants"),
                            Component.translatable("screen.polen.profile.gifts.hint." + this.profile.giftHintKey())
                    ),
                    pair(
                            Component.translatable("screen.polen.profile.gifts.dislikes"),
                            Component.translatable("screen.polen.profile.gifts.dislikes.basic")
                    )
            );
            return;
        }

        if (isHovering(mouseX, mouseY, this.panelX + 192, this.panelY + 126, PANEL_WIDTH - 208, 76)) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.translatable("screen.polen.profile.help_interest"));
            lines.add(pair(Component.translatable("screen.polen.profile.top_interests"), topInterestsComponent()));
            for (String label : RADAR_LABELS) {
                lines.add(pair(interestLabel(label), Component.literal(String.valueOf(interestValue(label)))));
            }
            lines.add(Component.translatable(this.profile.focusHintKey()));
            lines.add(Component.translatable("screen.polen.profile.memories.unlocked", this.profile.unlockedMemoryCount(), this.profile.memoryEntries().size()));
            graphics.renderComponentTooltip(this.font, lines, mouseX, mouseY);
        }
    }

    private void renderTooltipLines(GuiGraphics graphics, int mouseX, int mouseY, Component... lines) {
        List<Component> tooltip = new ArrayList<>();
        for (Component line : lines) {
            if (line != null && !line.getString().isBlank()) {
                tooltip.add(line);
            }
        }
        if (!tooltip.isEmpty()) {
            graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    private Component topInterestsComponent() {
        MutableComponent result = Component.empty();
        boolean first = true;
        for (PolenProfileView.InterestBar bar : this.profile.strongestInterests(2)) {
            if (!first) {
                result.append(Component.literal(", "));
            }
            result.append(interestLabel(bar.label()));
            first = false;
        }
        return result;
    }

    private int interestValue(String label) {
        for (PolenProfileView.InterestBar bar : this.profile.interestBars()) {
            if (bar.label().equals(label)) {
                return bar.value();
            }
        }
        return 0;
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static Component pair(Component label, Component value) {
        return Component.empty()
                .append(label.copy())
                .append(Component.literal(": "))
                .append(value.copy());
    }

    private static Component interestLabel(String label) {
        return Component.translatable("screen.polen.profile.interest." + label.toLowerCase().replace(' ', '_'));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
