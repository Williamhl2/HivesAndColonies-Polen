package com.hivesandcolonies.hccharacters.character.polen.client.profile;

public final class PolenProfileActionState {
    private final String followButtonLabelKey;
    private final boolean canAskToFollow;
    private final boolean canReturnHome;
    private final String followSummaryKey;
    private final String homeSummaryKey;
    private final String giftSummaryKey;
    private final String trustWalkStateKey;
    private final String nextActionKey;

    private PolenProfileActionState(
            String followButtonLabelKey,
            boolean canAskToFollow,
            boolean canReturnHome,
            String followSummaryKey,
            String homeSummaryKey,
            String giftSummaryKey,
            String trustWalkStateKey,
            String nextActionKey
    ) {
        this.followButtonLabelKey = followButtonLabelKey;
        this.canAskToFollow = canAskToFollow;
        this.canReturnHome = canReturnHome;
        this.followSummaryKey = followSummaryKey;
        this.homeSummaryKey = homeSummaryKey;
        this.giftSummaryKey = giftSummaryKey;
        this.trustWalkStateKey = trustWalkStateKey;
        this.nextActionKey = nextActionKey;
    }

    public static PolenProfileActionState from(boolean trustWalkActive, boolean trustWalkUnlocked, boolean hasHome, boolean giftsOnCooldown) {
        String trustWalkStateKey = trustWalkActive
                ? "screen.polen.profile.actions.follow.active"
                : (trustWalkUnlocked
                ? "screen.polen.profile.actions.follow.ready"
                : "screen.polen.profile.actions.follow.locked");

        String nextActionKey;
        if (!trustWalkUnlocked) {
            nextActionKey = "screen.polen.profile.next_action.raise_trust";
        } else if (!hasHome) {
            nextActionKey = "screen.polen.profile.next_action.assign_bed";
        } else if (giftsOnCooldown) {
            nextActionKey = "screen.polen.profile.next_action.wait_gifts";
        } else {
            nextActionKey = "screen.polen.profile.next_action.memory";
        }

        return new PolenProfileActionState(
                trustWalkActive
                        ? "screen.polen.profile.action.stay"
                        : "screen.polen.profile.action.follow",
                trustWalkUnlocked || trustWalkActive,
                hasHome,
                trustWalkActive
                        ? "screen.polen.profile.state.follow.active"
                        : (trustWalkUnlocked
                        ? "screen.polen.profile.state.follow.ready"
                        : "screen.polen.profile.state.follow.locked"),
                hasHome
                        ? "screen.polen.profile.state.home.assigned"
                        : "screen.polen.profile.state.home.missing",
                giftsOnCooldown
                        ? "screen.polen.profile.state.gifts.cooldown"
                        : "screen.polen.profile.state.gifts.ready",
                trustWalkStateKey,
                nextActionKey
        );
    }

    public String followButtonLabelKey() {
        return this.followButtonLabelKey;
    }

    public boolean canAskToFollow() {
        return this.canAskToFollow;
    }

    public boolean canReturnHome() {
        return this.canReturnHome;
    }

    public String followSummaryKey() {
        return this.followSummaryKey;
    }

    public String homeSummaryKey() {
        return this.homeSummaryKey;
    }

    public String giftSummaryKey() {
        return this.giftSummaryKey;
    }

    public String trustWalkStateKey() {
        return this.trustWalkStateKey;
    }

    public String nextActionKey() {
        return this.nextActionKey;
    }
}
