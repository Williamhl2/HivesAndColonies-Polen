package com.hivesandcolonies.hccharacters.character.polen.client.screen;

import com.hivesandcolonies.hccharacters.character.polen.client.profile.PolenProfileClientActions;
import com.hivesandcolonies.hccharacters.character.polen.client.profile.PolenProfileView;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.identity.PolenWorldAffinity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PolenProfileScreen extends Screen {
    private static final int PANEL_WIDTH = 430;
    private static final int PANEL_HEIGHT = 292;
    private static final int PANEL_COLOR = 0xFF181026;
    private static final int PANEL_BORDER = 0xFF8C6FD1;
    private static final int SECTION_COLOR = 0xCC271E3C;
    private static final int TEXT = 0xFFEFEAFB;
    private static final int MUTED_TEXT = 0xFFC7B9DD;
    private static final int GOLD = 0xFFFFD86A;
    private static final int BAR_BG = 0xFF15101F;
    private static final int BAR_FILL = 0xFFE6C34A;
    private static final int BAR_LOW = 0xFFD86565;
    private static final int TAB_IDLE = 0xFF21182F;
    private static final int TAB_HOVER = 0xFF35244F;
    private static final int TAB_ACTIVE = 0xFF4C3675;

    private final PolenProfileView profile;
    private int panelX;
    private int panelY;
    private Tab activeTab = Tab.PROFILE;

    public PolenProfileScreen(PolenProfileView profile) {
        super(Component.translatable("screen.polen.profile.title"));
        this.profile = profile;
    }

    @Override
    protected void init() {
        this.panelX = Math.max(8, (this.width - PANEL_WIDTH) / 2);
        this.panelY = Math.max(8, (this.height - PANEL_HEIGHT) / 2);
        this.clearWidgets();
        this.addRenderableWidget(Button.builder(Component.literal("×"), button -> this.onClose())
                .bounds(this.panelX + PANEL_WIDTH - 24, this.panelY + 8, 16, 16)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable(this.profile.trustWalkActive() ? "screen.polen.profile.action.stay" : "screen.polen.profile.action.follow"),
                        button -> PolenProfileClientActions.send(this.profile.entityId(), PolenProfileClientActions.FOLLOW_TOGGLE))
                .bounds(this.panelX + 16, this.panelY + PANEL_HEIGHT - 32, 118, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.polen.profile.action.set_bed"),
                        button -> PolenProfileClientActions.send(this.profile.entityId(), PolenProfileClientActions.SET_NEARBY_BED_AS_HOME))
                .bounds(this.panelX + 142, this.panelY + PANEL_HEIGHT - 32, 140, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.polen.profile.action.return_home"),
                        button -> PolenProfileClientActions.send(this.profile.entityId(), PolenProfileClientActions.RETURN_HOME))
                .bounds(this.panelX + 290, this.panelY + PANEL_HEIGHT - 32, 112, 20)
                .build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally empty: the profile is an overlay, not a vanilla blurred menu.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x22000000);
        drawPanel(graphics);
        drawHeader(graphics);
        drawTabs(graphics, mouseX, mouseY);

        switch (this.activeTab) {
            case PROFILE -> drawProfileTab(graphics);
            case NEEDS -> drawNeedsTab(graphics);
            case ACTIONS -> drawActionsTab(graphics);
            case HOME -> drawHomeTab(graphics);
            case MEMORIES -> drawMemoriesTab(graphics);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        renderHoverTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Tab tab : Tab.values()) {
                if (isInside(mouseX, mouseY, tabX(tab), tabY(), tabWidth(tab), 18)) {
                    this.activeTab = tab;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        graphics.drawString(this.font, affinityTitle(this.profile.affinity()), x, y + 13, GOLD, false);

        if (!this.profile.charmStack().isEmpty()) {
            int equipmentX = this.panelX + PANEL_WIDTH - 148;
            int equipmentY = this.panelY + 14;
            int iconX = charmIconX();
            int iconY = charmIconY();
            graphics.drawString(this.font, Component.translatable("screen.polen.profile.equipment"), equipmentX, equipmentY, MUTED_TEXT, false);
            graphics.drawString(this.font, this.profile.charmStack().getHoverName(), equipmentX, equipmentY + 13, GOLD, false);
            graphics.renderItem(this.profile.charmStack(), iconX, iconY);
        }
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        for (Tab tab : Tab.values()) {
            boolean hovered = isInside(mouseX, mouseY, tabX(tab), tabY(), tabWidth(tab), 18);
            drawTab(graphics, tabX(tab), tabY(), tabWidth(tab), tab, this.activeTab == tab, hovered);
        }
    }

    private void drawTab(GuiGraphics graphics, int x, int y, int width, Tab tab, boolean active, boolean hovered) {
        int color = active ? TAB_ACTIVE : hovered ? TAB_HOVER : TAB_IDLE;
        int textColor = active || hovered ? TEXT : MUTED_TEXT;
        graphics.fill(x, y, x + width, y + 18, color);
        if (hovered && !active) {
            graphics.fill(x, y + 17, x + width, y + 18, PANEL_BORDER);
        }
        graphics.drawString(this.font, Component.translatable(tab.translationKey), x + 7, y + 5, textColor, false);
    }

    private void drawProfileTab(GuiGraphics graphics) {
        drawIdentityCard(graphics);
        drawInterests(graphics);
    }

    private void drawIdentityCard(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 80;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 54, SECTION_COLOR);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.affinity"), x + 10, y + 9, MUTED_TEXT, false);
        graphics.drawString(this.font, affinityLabel(this.profile.affinity()), x + 74, y + 9, GOLD, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.story"), x + 10, y + 24, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.story.awakening"), x + 74, y + 24, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.mood"), x + 210, y + 9, MUTED_TEXT, false);
        graphics.drawString(this.font, moodLabel(), x + 262, y + 9, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.task"), x + 210, y + 24, MUTED_TEXT, false);
        graphics.drawString(this.font, taskLabel(), x + 262, y + 24, TEXT, false);
    }

    private void drawInterests(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 150;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 82, SECTION_COLOR);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.interests"), x + 10, y + 8, TEXT, false);

        int rowY = y + 24;
        for (PolenProfileView.InterestBar bar : this.profile.interestBars()) {
            drawInterestBar(graphics, x + 10, rowY, interestLabel(bar.label()), bar.value());
            rowY += 10;
        }
    }

    private void drawNeedsTab(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 80;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 162, SECTION_COLOR);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.needs.title"), x + 10, y + 9, GOLD, false);
        int rowY = y + 28;
        drawNeedBar(graphics, x + 10, rowY, Component.translatable("screen.polen.profile.need.safety"), this.profile.safety()); rowY += 14;
        drawNeedBar(graphics, x + 10, rowY, Component.translatable("screen.polen.profile.need.social"), this.profile.social()); rowY += 14;
        drawNeedBar(graphics, x + 10, rowY, Component.translatable("screen.polen.profile.need.curiosity"), this.profile.curiosity()); rowY += 14;
        drawNeedBar(graphics, x + 10, rowY, Component.translatable("screen.polen.profile.need.rest"), this.profile.rest()); rowY += 14;
        drawNeedBar(graphics, x + 10, rowY, Component.translatable("screen.polen.profile.need.magic"), this.profile.magic());

        graphics.drawString(this.font, Component.translatable("screen.polen.profile.needs.current"), x + 10, y + 110, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.needs.hint." + this.profile.strongestNeedKey()), x + 104, y + 110, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.gifts.wants"), x + 10, y + 128, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.gifts.hint." + this.profile.giftHintKey()), x + 104, y + 128, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.gifts.dislikes"), x + 10, y + 144, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.gifts.dislikes.basic"), x + 104, y + 144, TEXT, false);
    }

    private void drawActionsTab(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 80;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 162, SECTION_COLOR);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.actions.title"), x + 10, y + 9, GOLD, false);
        graphics.drawString(this.font, Component.translatable(this.profile.trustWalkActive() ? "screen.polen.profile.actions.follow.active" : "screen.polen.profile.actions.follow.ready"), x + 10, y + 34, TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.hasHome() ? "screen.polen.profile.actions.home.assigned" : "screen.polen.profile.actions.home.missing"), x + 10, y + 54, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.actions.bottom_hint"), x + 10, y + 78, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.actions.follow_hint"), x + 10, y + 102, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.actions.bed_hint"), x + 10, y + 118, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.actions.return_hint"), x + 10, y + 134, MUTED_TEXT, false);
    }

    private void drawHomeTab(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 80;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 150, SECTION_COLOR);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.home.title"), x + 10, y + 10, GOLD, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.home.residence"), x + 10, y + 32, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.hasHome() ? "screen.polen.profile.home.assigned" : "screen.polen.profile.home.none"), x + 112, y + 32, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.home.comfort"), x + 10, y + 48, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.hasHome() ? "screen.polen.profile.home.safe_bed" : "screen.polen.profile.home.unknown"), x + 112, y + 48, TEXT, false);
        graphics.drawString(this.font, Component.translatable(this.profile.hasHome() ? "screen.polen.profile.home.hint_assigned" : "screen.polen.profile.home.hint"), x + 10, y + 78, MUTED_TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.home.bed_instruction"), x + 10, y + 98, MUTED_TEXT, false);
    }

    private void drawMemoriesTab(GuiGraphics graphics) {
        int x = this.panelX + 16;
        int y = this.panelY + 80;
        int w = PANEL_WIDTH - 32;
        graphics.fill(x, y, x + w, y + 150, SECTION_COLOR);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.memories.title"), x + 10, y + 10, GOLD, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.memories.first_awakening"), x + 10, y + 34, TEXT, false);
        graphics.drawString(this.font, Component.translatable("screen.polen.profile.memories.hint"), x + 10, y + 58, MUTED_TEXT, false);
    }

    private void drawInterestBar(GuiGraphics graphics, int x, int y, Component label, int value) {
        int labelWidth = 86;
        int barWidth = 220;
        int barHeight = 5;
        graphics.drawString(this.font, label, x, y - 1, MUTED_TEXT, false);
        int bx = x + labelWidth;
        graphics.fill(bx, y, bx + barWidth, y + barHeight, BAR_BG);
        int fill = Math.max(0, Math.min(barWidth, value * barWidth / 100));
        graphics.fill(bx, y, bx + fill, y + barHeight, BAR_FILL);
        graphics.drawString(this.font, Component.literal(value + "%"), bx + barWidth + 8, y - 2, TEXT, false);
    }

    private void drawNeedBar(GuiGraphics graphics, int x, int y, Component label, int value) {
        int labelWidth = 86;
        int barWidth = 220;
        int barHeight = 6;
        graphics.drawString(this.font, label, x, y - 1, MUTED_TEXT, false);
        int bx = x + labelWidth;
        graphics.fill(bx, y, bx + barWidth, y + barHeight, BAR_BG);
        int fill = Math.max(0, Math.min(barWidth, value * barWidth / 100));
        graphics.fill(bx, y, bx + fill, y + barHeight, value < 35 ? BAR_LOW : BAR_FILL);
        graphics.drawString(this.font, Component.literal(value + "%"), bx + barWidth + 8, y - 2, TEXT, false);
    }

    private void renderHoverTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isInside(mouseX, mouseY, charmIconX(), charmIconY(), 16, 16) && !this.profile.charmStack().isEmpty()) {
            graphics.renderTooltip(this.font, Component.translatable("screen.polen.profile.tooltip.charm." + this.profile.affinity().name().toLowerCase()), mouseX, mouseY);
            return;
        }

        for (Tab tab : Tab.values()) {
            if (isInside(mouseX, mouseY, tabX(tab), tabY(), tabWidth(tab), 18)) {
                graphics.renderTooltip(this.font, Component.translatable(tab.tooltipKey), mouseX, mouseY);
                return;
            }
        }
    }

    private int charmIconX() {
        return this.panelX + PANEL_WIDTH - 56;
    }

    private int charmIconY() {
        return this.panelY + 34;
    }

    private int tabY() {
        return this.panelY + 52;
    }

    private int tabX(Tab tab) {
        int start = this.panelX + 16;
        return switch (tab) {
            case PROFILE -> start;
            case NEEDS -> start + 68;
            case ACTIONS -> start + 126;
            case HOME -> start + 198;
            case MEMORIES -> start + 252;
        };
    }

    private int tabWidth(Tab tab) {
        return switch (tab) {
            case PROFILE -> 64;
            case NEEDS -> 54;
            case ACTIONS -> 68;
            case HOME -> 50;
            case MEMORIES -> 76;
        };
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static Component affinityTitle(PolenWorldAffinity affinity) {
        return Component.translatable("screen.polen.profile.affinity_title." + affinity.name().toLowerCase());
    }

    private static Component affinityLabel(PolenWorldAffinity affinity) {
        return Component.translatable("screen.polen.profile.affinity." + affinity.name().toLowerCase());
    }

    private static Component interestLabel(String label) {
        return Component.translatable("screen.polen.profile.interest." + label.toLowerCase().replace(' ', '_'));
    }

    private Component moodLabel() {
        return Component.translatable("screen.polen.profile.mood." + this.profile.mood().name().toLowerCase());
    }

    private Component taskLabel() {
        return Component.translatable("screen.polen.profile.task." + this.profile.task().name().toLowerCase());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Tab {
        PROFILE("screen.polen.profile.tab.profile", "screen.polen.profile.tooltip.tab.profile"),
        NEEDS("screen.polen.profile.tab.needs", "screen.polen.profile.tooltip.tab.needs"),
        ACTIONS("screen.polen.profile.tab.actions", "screen.polen.profile.tooltip.tab.actions"),
        HOME("screen.polen.profile.tab.home", "screen.polen.profile.tooltip.tab.home"),
        MEMORIES("screen.polen.profile.tab.memories", "screen.polen.profile.tooltip.tab.memories");

        private final String translationKey;
        private final String tooltipKey;

        Tab(String translationKey, String tooltipKey) {
            this.translationKey = translationKey;
            this.tooltipKey = tooltipKey;
        }
    }
}
