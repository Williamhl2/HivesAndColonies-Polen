package com.hivesandcolonies.hccharacters.character.polen.client.profile;

import java.util.List;

public final class PolenProfileHelpState {
    private final String giftHintKey;
    private final String focusHintKey;
    private final String homeStatusDetailKey;
    private final List<PolenProfileInterestMetric> interestMetrics;
    private final List<PolenProfileInterestMetric> topInterests;
    private final List<PolenProfileMemoryEntry> memoryEntries;

    private PolenProfileHelpState(
            String giftHintKey,
            String focusHintKey,
            String homeStatusDetailKey,
            List<PolenProfileInterestMetric> interestMetrics,
            List<PolenProfileInterestMetric> topInterests,
            List<PolenProfileMemoryEntry> memoryEntries
    ) {
        this.giftHintKey = giftHintKey;
        this.focusHintKey = focusHintKey;
        this.homeStatusDetailKey = homeStatusDetailKey;
        this.interestMetrics = interestMetrics;
        this.topInterests = topInterests;
        this.memoryEntries = memoryEntries;
    }

    public static PolenProfileHelpState from(
            List<PolenProfileInterestMetric> interestMetrics,
            List<PolenProfileMemoryEntry> memoryEntries,
            boolean hasHome,
            int safety,
            int social,
            int curiosity,
            int rest,
            int magic
    ) {
        String strongestInterest = interestMetrics.isEmpty() ? "" : interestMetrics.get(0).label();
        String giftHintKey;
        if (!strongestInterest.isEmpty()) {
            giftHintKey = switch (strongestInterest) {
                case "bees" -> "bees";
                case "magic" -> "source";
                case "food" -> "food";
                case "colonies", "decoration" -> "home";
                case "exploration" -> "nature";
                default -> "";
            };
        } else {
            int lowest = safety;
            String strongestNeedKey = "safety";
            if (rest < lowest) {
                lowest = rest;
                strongestNeedKey = "rest";
            }
            if (social < lowest) {
                lowest = social;
                strongestNeedKey = "social";
            }
            if (curiosity < lowest) {
                lowest = curiosity;
                strongestNeedKey = "curiosity";
            }
            if (magic < lowest) {
                strongestNeedKey = "magic";
            }

            giftHintKey = switch (strongestNeedKey) {
                case "safety", "rest" -> "home";
                case "social" -> "food";
                case "curiosity" -> "nature";
                case "magic" -> "source";
                default -> "bees";
            };
        }

        String focusHintKey = switch (strongestInterest) {
            case "bees" -> "screen.polen.profile.focus.bees";
            case "magic" -> "screen.polen.profile.focus.magic";
            case "colonies", "decoration" -> "screen.polen.profile.focus.home";
            case "food" -> "screen.polen.profile.focus.food";
            case "exploration" -> "screen.polen.profile.focus.nature";
            default -> "screen.polen.profile.focus.general";
        };

        return new PolenProfileHelpState(
                giftHintKey,
                focusHintKey,
                hasHome
                        ? "screen.polen.profile.actions.home.assigned"
                        : "screen.polen.profile.actions.home.missing",
                List.copyOf(interestMetrics),
                List.copyOf(interestMetrics.subList(0, Math.min(2, interestMetrics.size()))),
                List.copyOf(memoryEntries)
        );
    }

    public String giftHintKey() {
        return this.giftHintKey;
    }

    public String focusHintKey() {
        return this.focusHintKey;
    }

    public String homeStatusDetailKey() {
        return this.homeStatusDetailKey;
    }

    public List<PolenProfileInterestMetric> interestMetrics() {
        return this.interestMetrics;
    }

    public List<PolenProfileInterestMetric> topInterests() {
        return this.topInterests;
    }

    public List<PolenProfileMemoryEntry> memoryEntries() {
        return this.memoryEntries;
    }
}
