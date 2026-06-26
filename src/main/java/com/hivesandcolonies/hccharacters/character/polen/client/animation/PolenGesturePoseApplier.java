package com.hivesandcolonies.hccharacters.character.polen.client.animation;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

public final class PolenGesturePoseApplier {
    private static final float MAX_HEAD_X = 0.55F;
    private static final float MIN_HEAD_X = -0.60F;
    private static final float MAX_HEAD_Y = 0.85F;
    private static final float MAX_HEAD_Z = 0.10F;

    private PolenGesturePoseApplier() {
    }

    public static void apply(
            HumanoidModel<PolenEntity> model,
            PolenEntity polen,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks
    ) {
        if (polen.isSleeping()) {
            return;
        }

        float idleWave = Mth.sin(ageInTicks * 0.10F);
        float breathe = idleWave * 0.025F;
        float shoulderSway = Mth.sin(ageInTicks * 0.16F) * 0.04F;
        float walkAmount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);

        stabilizeBasePose(model, breathe, shoulderSway, limbSwing, walkAmount);

        switch (polen.getGesture()) {
            case SINGING -> applySingingPose(model, idleWave, shoulderSway);
            case DRAWING -> applyDrawingPose(model, idleWave);
            case ATTUNING -> applyAttuningPose(model, shoulderSway);
            case ILLUMINATING -> applyIlluminatingPose(model, idleWave);
            case REFLECTING -> applyReflectingPose(model, idleWave);
            case CURIOUS -> applyCuriousPose(model, idleWave);
            case APPROACHING -> applyApproachingPose(model, walkAmount);
            case WITHDRAWN -> applyWithdrawnPose(model, idleWave, walkAmount);
            case STARTLED -> applyStartledPose(model, ageInTicks, walkAmount);
            case IDLE -> applyIdlePose(model, idleWave, walkAmount);
        }

        clampHead(model);
    }

    private static void stabilizeBasePose(
            HumanoidModel<PolenEntity> model,
            float breathe,
            float shoulderSway,
            float limbSwing,
            float walkAmount
    ) {
        model.head.zRot = 0.0F;
        model.body.zRot = 0.0F;
        model.body.xRot += breathe;
        preserveNaturalWalkSwing(model, limbSwing, walkAmount);
        model.rightArm.zRot *= 0.35F;
        model.leftArm.zRot *= 0.35F;
        model.rightArm.yRot *= 0.55F;
        model.leftArm.yRot *= 0.55F;
        model.rightArm.xRot += shoulderSway;
        model.leftArm.xRot -= shoulderSway;
    }

    private static void preserveNaturalWalkSwing(
            HumanoidModel<PolenEntity> model,
            float limbSwing,
            float walkAmount
    ) {
        if (walkAmount <= 0.02F) {
            return;
        }

        float rightSwing = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.3F * walkAmount;
        float leftSwing = Mth.cos(limbSwing * 0.6662F) * 1.3F * walkAmount;
        float blend = Mth.clamp(walkAmount * 0.85F, 0.18F, 0.82F);

        model.rightArm.xRot = Mth.lerp(blend, model.rightArm.xRot, rightSwing);
        model.leftArm.xRot = Mth.lerp(blend, model.leftArm.xRot, leftSwing);
        model.rightArm.yRot = Mth.lerp(blend * 0.45F, model.rightArm.yRot, 0.0F);
        model.leftArm.yRot = Mth.lerp(blend * 0.45F, model.leftArm.yRot, 0.0F);
    }

    private static void applyIdlePose(HumanoidModel<PolenEntity> model, float idleWave, float walkAmount) {
        model.head.xRot += idleWave * 0.01F;
        model.body.xRot += 0.015F;
        model.rightArm.xRot -= 0.05F * (1.0F - walkAmount);
        model.leftArm.xRot -= 0.03F * (1.0F - walkAmount);
    }

    private static void applySingingPose(HumanoidModel<PolenEntity> model, float idleWave, float shoulderSway) {
        model.head.xRot -= 0.04F;
        model.head.yRot += idleWave * 0.04F;
        model.rightArm.xRot -= 0.22F;
        model.leftArm.xRot -= 0.14F;
        model.rightArm.yRot -= 0.04F;
        model.leftArm.yRot += 0.03F;
        model.rightArm.zRot += 0.02F;
        model.leftArm.zRot -= 0.02F;
        model.body.xRot += 0.03F;
        model.body.yRot += shoulderSway * 0.35F;
    }

    private static void applyDrawingPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.14F;
        model.rightArm.xRot = -0.96F + idleWave * 0.03F;
        model.rightArm.yRot = -0.14F;
        model.rightArm.zRot = 0.03F;
        model.leftArm.xRot = -0.46F;
        model.leftArm.yRot = 0.08F;
        model.leftArm.zRot = -0.03F;
        model.body.xRot += 0.10F;
    }

    private static void applyAttuningPose(HumanoidModel<PolenEntity> model, float shoulderSway) {
        model.head.xRot -= 0.08F;
        model.rightArm.xRot = -0.92F + shoulderSway;
        model.leftArm.xRot = -0.92F - shoulderSway;
        model.rightArm.yRot = -0.10F;
        model.leftArm.yRot = 0.10F;
        model.rightArm.zRot = 0.08F;
        model.leftArm.zRot = -0.08F;
        model.body.xRot += 0.05F;
    }

    private static void applyIlluminatingPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot -= 0.02F;
        model.rightArm.xRot = -1.02F;
        model.rightArm.yRot = -0.06F;
        model.rightArm.zRot = 0.05F;
        model.leftArm.xRot = -0.38F + idleWave * 0.02F;
        model.leftArm.yRot = 0.10F;
        model.leftArm.zRot = -0.04F;
        model.body.xRot += 0.07F;
    }

    private static void applyReflectingPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.10F;
        model.rightArm.xRot = -0.34F + idleWave * 0.02F;
        model.leftArm.xRot = -0.34F - idleWave * 0.02F;
        model.rightArm.yRot = -0.10F;
        model.leftArm.yRot = 0.10F;
        model.rightArm.zRot = 0.02F;
        model.leftArm.zRot = -0.02F;
        model.body.xRot += 0.08F;
    }

    private static void applyCuriousPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.05F;
        model.head.yRot += 0.08F + idleWave * 0.03F;
        model.rightArm.xRot -= 0.06F;
        model.leftArm.xRot -= 0.02F;
        model.body.xRot += 0.02F;
    }

    private static void applyApproachingPose(HumanoidModel<PolenEntity> model, float walkAmount) {
        model.head.xRot -= 0.03F;
        model.rightArm.xRot -= 0.04F * (1.0F - walkAmount);
        model.leftArm.xRot -= 0.02F * (1.0F - walkAmount);
        model.rightArm.yRot *= 0.2F;
        model.leftArm.yRot *= 0.2F;
        model.rightArm.zRot *= 0.2F;
        model.leftArm.zRot *= 0.2F;
        model.body.xRot += 0.03F;
    }

    private static void applyWithdrawnPose(HumanoidModel<PolenEntity> model, float idleWave, float walkAmount) {
        model.head.xRot += 0.08F;
        float guardedBias = 0.08F * (1.0F - walkAmount);
        model.rightArm.xRot += guardedBias + idleWave * 0.01F;
        model.leftArm.xRot += guardedBias - idleWave * 0.01F;
        model.rightArm.yRot = -0.04F;
        model.leftArm.yRot = 0.04F;
        model.rightArm.zRot = 0.02F;
        model.leftArm.zRot = -0.02F;
        model.body.xRot += 0.04F;
    }

    private static void applyStartledPose(HumanoidModel<PolenEntity> model, float ageInTicks, float walkAmount) {
        float pulse = Mth.sin(ageInTicks * 0.45F) * 0.04F;
        model.head.xRot -= 0.06F;
        float startledBias = 0.12F * (1.0F - walkAmount);
        model.rightArm.xRot += startledBias + pulse;
        model.leftArm.xRot += startledBias - pulse;
        model.rightArm.yRot = -0.05F;
        model.leftArm.yRot = 0.05F;
        model.rightArm.zRot = 0.03F;
        model.leftArm.zRot = -0.03F;
        model.body.xRot += 0.05F;
    }

    private static void clampHead(HumanoidModel<PolenEntity> model) {
        model.head.xRot = Mth.clamp(model.head.xRot, MIN_HEAD_X, MAX_HEAD_X);
        model.head.yRot = Mth.clamp(model.head.yRot, -MAX_HEAD_Y, MAX_HEAD_Y);
        model.head.zRot = Mth.clamp(model.head.zRot, -MAX_HEAD_Z, MAX_HEAD_Z);
    }
}
