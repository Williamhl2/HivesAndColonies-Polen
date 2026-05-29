package com.hivesandcolonies.polen.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.hivesandcolonies.polen.dialogue.PolenDialogueManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;

public class PolenEntity extends PathfinderMob {
    public PolenEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setCustomName(Component.translatable("entity.polen.polen"));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
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

        this.goalSelector.addGoal(1,
                new RandomStrollGoal(this, 0.8D));

        this.goalSelector.addGoal(2,
                new LookAtPlayerGoal(this, Player.class, 8.0F));

        this.goalSelector.addGoal(3,
                new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            int currentChapter =
                PolenChapterManager.getCurrentChapter(player);

            int affinity =
        PolenAffinityManager.getAffinity(player);
                
        player.displayClientMessage(
                PolenDialogueManager.getDialogue(
                        currentChapter,
                        affinity,
                        this.getRandom()
                ),
                false
        );
        }

        return InteractionResult.SUCCESS;
    }

}