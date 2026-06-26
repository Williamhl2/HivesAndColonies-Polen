package com.hivesandcolonies.hccharacters.common.npc.relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class NpcRewardPool {
    private static final int ROLL_DENOMINATOR = 100_000;
    private static final int UNIQUE_ROLLS = 1;
    private static final int LEGENDARY_ROLLS = 19;
    private static final int RARE_ROLLS = 80;

    private final List<Entry> rare = new ArrayList<>();
    private final List<Entry> legendary = new ArrayList<>();
    private final List<Entry> unique = new ArrayList<>();

    public NpcRewardPool rare(Function<Player, ItemStack> factory, String message) {
        this.rare.add(new Entry(factory, message));
        return this;
    }

    public NpcRewardPool legendary(Function<Player, ItemStack> factory, String message) {
        this.legendary.add(new Entry(factory, message));
        return this;
    }

    public NpcRewardPool unique(Function<Player, ItemStack> factory, String message) {
        this.unique.add(new Entry(factory, message));
        return this;
    }

    public NpcRewardResult roll(Player player, RandomSource random) {
        int roll = random.nextInt(ROLL_DENOMINATOR);
        if (roll < UNIQUE_ROLLS) {
            return this.pick(NpcRewardTier.UNIQUE, this.unique, player, random);
        }
        if (roll < UNIQUE_ROLLS + LEGENDARY_ROLLS) {
            return this.pick(NpcRewardTier.LEGENDARY, this.legendary, player, random);
        }
        if (roll < UNIQUE_ROLLS + LEGENDARY_ROLLS + RARE_ROLLS) {
            return this.pick(NpcRewardTier.RARE, this.rare, player, random);
        }
        return NpcRewardResult.none();
    }

    private NpcRewardResult pick(NpcRewardTier tier, List<Entry> entries, Player player, RandomSource random) {
        if (entries.isEmpty()) {
            return NpcRewardResult.none();
        }
        Entry entry = entries.get(random.nextInt(entries.size()));
        ItemStack stack = entry.factory.apply(player);
        if (stack == null || stack.isEmpty()) {
            return NpcRewardResult.none();
        }
        return new NpcRewardResult(tier, stack, entry.message);
    }

    public static String oddsText() {
        return "99.900% nada especial, 0.080% rara, 0.019% legendaria, 0.001% unica";
    }

    private record Entry(Function<Player, ItemStack> factory, String message) {}
}
