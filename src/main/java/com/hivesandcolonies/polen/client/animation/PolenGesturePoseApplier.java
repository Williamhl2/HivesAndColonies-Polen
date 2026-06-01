package com.hivesandcolonies.polen.client.animation;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.gesture.PolenGesture;
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
        float idleWave = Mth.sin(ageInTicks * 0.10F);
        float breathe = idleWave * 0.025F;
        float shoulderSway = Mth.sin(ageInTicks * 0.16F) * 0.04F;
        float walkAmount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float walkCycle = Mth.sin(limbSwing * 0.6F);

        stabilizeBasePose(model, breathe, shoulderSway);

        switch (polen.getGesture()) {
            case SINGING -> applySingingPose(model, idleWave, shoulderSway);
            case DRAWING -> applyDrawingPose(model, idleWave);
            case ATTUNING -> applyAttuningPose(model, shoulderSway);
            case ILLUMINATING -> applyIlluminatingPose(model, idleWave);
            case REFLECTING -> applyReflectingPose(model, idleWave);
            case CURIOUS -> applyCuriousPose(model, idleWave);
            case APPROACHING -> applyApproachingPose(model, walkCycle, walkAmount);
            case WITHDRAWN -> applyWithdrawnPose(model, idleWave);
            case STARTLED -> applyStartledPose(model, ageInTicks);
            case IDLE -> applyIdlePose(model, idleWave, walkAmount);
        }

        clampHead(model);
    }

    private static void stabilizeBasePose(HumanoidModel<PolenEntity> model, float breathe, float shoulderSway) {
        model.head.zRot = 0.0F;
        model.body.zRot = 0.0F;
        model.body.xRot += breathe;
        model.rightArm.zRot *= 0.35F;
        model.leftArm.zRot *= 0.35F;
        model.rightArm.yRot *= 0.55F;
        model.leftArm.yRot *= 0.55F;
        model.rightArm.xRot += shoulderSway;
        model.leftArm.xRot -= shoulderSway;
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
        model.rightArm.xRot -= 0.30F;
        model.leftArm.xRot -= 0.18F;
        model.rightArm.zRot += 0.05F;
        model.leftArm.zRot -= 0.03F;
        model.body.xRot += 0.03F;
        model.body.yRot += shoulderSway * 0.35F;
    }

    private static void applyDrawingPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.14F;
        model.rightArm.xRot = -1.10F + idleWave * 0.04F;
        model.rightArm.yRot = -0.22F;
        model.rightArm.zRot = 0.08F;
        model.leftArm.xRot = -0.58F;
        model.leftArm.yRot = 0.14F;
        model.leftArm.zRot = -0.06F;
        model.body.xRot += 0.10F;
    }

    private static void applyAttuningPose(HumanoidModel<PolenEntity> model, float shoulderSway) {
        model.head.xRot -= 0.08F;
        model.rightArm.xRot = -0.92F + shoulderSway;
        model.leftArm.xRot = -0.92F - shoulderSway;
        model.rightArm.yRot = -0.14F;
        model.leftArm.yRot = 0.14F;
        model.rightArm.zRot = 0.16F;
        model.leftArm.zRot = -0.16F;
        model.body.xRot += 0.05F;
    }

    private static void applyIlluminatingPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot -= 0.02F;
        model.rightArm.xRot = -1.15F;
        model.rightArm.yRot = -0.10F;
        model.rightArm.zRot = 0.12F;
        model.leftArm.xRot = -0.45F + idleWave * 0.03F;
        model.leftArm.yRot = 0.18F;
        model.leftArm.zRot = -0.08F;
        model.body.xRot += 0.07F;
    }

    private static void applyReflectingPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.10F;
        model.rightArm.xRot = -0.42F + idleWave * 0.02F;
        model.leftArm.xRot = -0.42F - idleWave * 0.02F;
        model.rightArm.yRot = -0.16F;
        model.leftArm.yRot = 0.16F;
        model.rightArm.zRot = 0.04F;
        model.leftArm.zRot = -0.04F;
        model.body.xRot += 0.08F;
    }

    private static void applyCuriousPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.05F;
        model.head.yRot += 0.08F + idleWave * 0.03F;
        model.rightArm.xRot -= 0.06F;
        model.leftArm.xRot -= 0.02F;
        model.body.xRot += 0.02F;
    }

    private static void applyApproachingPose(HumanoidModel<PolenEntity> model, float walkCycle, float walkAmount) {
        model.head.xRot -= 0.03F;
        model.rightArm.xRot += walkCycle * 0.18F * walkAmount;
        model.leftArm.xRot -= walkCycle * 0.18F * walkAmount;
        model.rightArm.zRot = 0.03F;
        model.leftArm.zRot = -0.03F;
        model.body.xRot += 0.03F;
    }

    private static void applyWithdrawnPose(HumanoidModel<PolenEntity> model, float idleWave) {
        model.head.xRot += 0.08F;
        model.rightArm.xRot = -0.18F + idleWave * 0.02F;
        model.leftArm.xRot = -0.18F - idleWave * 0.02F;
        model.rightArm.yRot = -0.10F;
        model.leftArm.yRot = 0.10F;
        model.rightArm.zRot = 0.10F;
        model.leftArm.zRot = -0.10F;
        model.body.xRot += 0.04F;
    }

    private static void applyStartledPose(HumanoidModel<PolenEntity> model, float ageInTicks) {
        float pulse = Mth.sin(ageInTicks * 0.45F) * 0.04F;
        model.head.xRot -= 0.06F;
        model.rightArm.xRot = -0.50F + pulse;
        model.leftArm.xRot = -0.50F - pulse;
        model.rightArm.yRot = -0.10F;
        model.leftArm.yRot = 0.10F;
        model.rightArm.zRot = 0.12F;
        model.leftArm.zRot = -0.12F;
        model.body.xRot += 0.05F;
    }

    private static void clampHead(HumanoidModel<PolenEntity> model) {
        model.head.xRot = Mth.clamp(model.head.xRot, MIN_HEAD_X, MAX_HEAD_X);
        model.head.yRot = Mth.clamp(model.head.yRot, -MAX_HEAD_Y, MAX_HEAD_Y);
        model.head.zRot = Mth.clamp(model.head.zRot, -MAX_HEAD_Z, MAX_HEAD_Z);
    }
}
