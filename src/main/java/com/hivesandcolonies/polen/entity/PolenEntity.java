package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;



public class PolenEntity extends PathfinderMob {

    public PolenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable("entity.polen.unknown_girl"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public void updateDisplayName(Player player) {
        if (PolenStoryFlagsManager.hasFlag(
                player,
                PolenStoryFlag.NAME_REVEALED
        )) {
            this.setCustomName(Component.translatable("entity.polen.polen"));
        } else {
            this.setCustomName(Component.translatable("entity.polen.unknown_girl"));
        }

        this.setCustomNameVisible(true);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            updateDisplayName(player);

            if (player instanceof ServerPlayer serverPlayer) {
                PolenAdvancementManager.grantFirstMeeting(serverPlayer);
            }

            int currentChapter = PolenChapterManager.getCurrentChapter(player);
            int affinity = PolenAffinityManager.getAffinity(player);

            if (currentChapter == PolenChapterManager.FOUNDATION
                    && !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.PLAYER_HAS_SHELTER)) {
                playShelterRecognitionDialogue(player);
                updateDisplayName(player);
                return InteractionResult.SUCCESS;
            }

            if (player instanceof ServerPlayer serverPlayer
                    && affinity >= PolenAffinityLevels.FIRST_TRUST) {
                PolenAdvancementManager.grantFirstTrust(serverPlayer);
            }

            if (!PolenStoryFlagsManager.hasFlag(
                player,
                PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL
            ) {
                playNameRevealDialogue(player);
                updateDisplayName(player);
                return InteractionResult.SUCCESS;
            }

            player.displayClientMessage(
                    PolenDialogueManager.getDialogue(
                            player,
                            currentChapter,
                            affinity,
                            this.getRandom()
                    ),
                    false
            );

            updateDisplayName(player);
        }

        return InteractionResult.SUCCESS;
    }

    private void playNameRevealDialogue(Player player) {
        player.displayClientMessage(
                Component.literal("???: Espera...")
                        .withStyle(ChatFormatting.GRAY),
                false
        );

        player.displayClientMessage(
                Component.literal("???: Hay algo que quiero decirte.")
                        .withStyle(ChatFormatting.GRAY),
                false
        );

        player.displayClientMessage(
                Component.literal("???: No sé por qué... pero siento que puedo confiar en ti.")
                        .withStyle(ChatFormatting.GRAY),
                false
        );

        player.displayClientMessage(
                Component.literal("???: Mi nombre es...")
                        .withStyle(ChatFormatting.GRAY),
                false
        );

        this.setCustomName(Component.translatable("entity.polen.polen"));
        this.setCustomNameVisible(true);

        player.displayClientMessage(
                Component.literal("Polen: Polen.")
                        .withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );

        player.displayClientMessage(
                Component.literal("Has descubierto su nombre: ")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("Polen").withStyle(ChatFormatting.LIGHT_PURPLE)),
                false
        );
        
        PolenStoryFlagsManager.setFlag(
                player,
                PolenStoryFlag.NAME_REVEALED
        );

        PolenStoryFlagsManager.setFlag(
                player,
                PolenStoryFlag.CHAPTER_0_COMPLETE
        );

        PolenChapterManager.setCurrentChapter(
                player,
                PolenChapterManager.FOUNDATION
        );

        
        
        if (player instanceof ServerPlayer serverPlayer) {
            PolenAdvancementManager.grantNameReveal(serverPlayer);
        }
    }

    private void playShelterRecognitionDialogue(Player player) {
    player.displayClientMessage(
            Component.literal("Polen: Este lugar...")
                    .withStyle(ChatFormatting.LIGHT_PURPLE),
            false
    );

    player.displayClientMessage(
            Component.literal("Polen: No parece un palacio, ni una colmena real...")
                    .withStyle(ChatFormatting.LIGHT_PURPLE),
            false
    );

    player.displayClientMessage(
            Component.literal("Polen: Pero se siente tranquilo.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE),
            false
    );

    player.displayClientMessage(
            Component.literal("Polen: Tal vez... podamos empezar aquí.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE),
            false
    );

    PolenStoryFlagsManager.setFlag(
            player,
            PolenStoryFlag.PLAYER_HAS_SHELTER
    );

    if (player instanceof ServerPlayer serverPlayer) {
        PolenAdvancementManager.grantPlayerHasShelter(serverPlayer);
    }
}

    
}