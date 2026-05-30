package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.progression.PolenAdvancementManager;
import com.hivesandcolonies.polen.progression.PolenAffinityLevels;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.polen.story.PolenStoryEventManager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
    private static final String UNKNOWN_GIRL_KEY = "entity.polen.unknown_girl";
    private static final String POLEN_KEY = "entity.polen.polen";

    public PolenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable(UNKNOWN_GIRL_KEY));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    public void updateDisplayName() {
        if (this.level() instanceof ServerLevel serverLevel
                && PolenStoryFlagsManager.hasFlag(serverLevel, PolenStoryFlag.NAME_REVEALED)) {
            this.setCustomName(Component.translatable(POLEN_KEY));
        } else {
            this.setCustomName(Component.translatable(UNKNOWN_GIRL_KEY));
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
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.tickCount % 20 == 0) {
            updateDisplayName();
        }
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
            updateDisplayName();
            PolenPlayerRelationshipManager.recordInteraction(player);

            if (player instanceof ServerPlayer serverPlayer) {
                PolenAdvancementManager.grantFirstMeeting(serverPlayer);
            }

            int currentChapter = PolenChapterManager.getCurrentChapter(player);
            int affinity = PolenAffinityManager.getAffinity(player);

            // Funcion adelantada, se comenta para evitar que se active antes de tiempo
            // if (shouldPlayShelterRecognition(currentChapter, player)) {
            //     PolenStoryEventManager.playShelterRecognition(player);
            //     updateDisplayName();
            //     return InteractionResult.SUCCESS;
            // }

            if (player instanceof ServerPlayer serverPlayer
                    && affinity >= PolenAffinityLevels.FIRST_TRUST) {
                PolenAdvancementManager.grantFirstTrust(serverPlayer);
            }

            if (shouldRevealName(player, affinity)) {
                PolenStoryEventManager.playNameReveal(player);
                updateDisplayName();
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

            updateDisplayName();
        }

        return InteractionResult.SUCCESS;
    }

    // Función adelantada para reconocer el refugio, se activará en el capítulo de la fundación
    // private static boolean shouldPlayShelterRecognition(int currentChapter, Player player) {
    //     return currentChapter == PolenChapterManager.FOUNDATION
    //             && !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.PLAYER_HAS_SHELTER);
    // }

    private static boolean shouldRevealName(Player player, int affinity) {
        return !PolenStoryFlagsManager.hasFlag(player, PolenStoryFlag.NAME_REVEALED)
                && affinity >= PolenAffinityLevels.NAME_REVEAL;
    }
}
