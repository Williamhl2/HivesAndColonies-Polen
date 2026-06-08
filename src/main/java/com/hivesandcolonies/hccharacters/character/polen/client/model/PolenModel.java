package com.hivesandcolonies.hccharacters.character.polen.client.model;

import com.hivesandcolonies.hccharacters.character.polen.client.animation.PolenGesturePoseApplier;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public class PolenModel extends PlayerModel<PolenEntity> {
    public PolenModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setupAnim(
            PolenEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        PolenGesturePoseApplier.apply(this, entity, limbSwing, limbSwingAmount, ageInTicks);
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }
}
