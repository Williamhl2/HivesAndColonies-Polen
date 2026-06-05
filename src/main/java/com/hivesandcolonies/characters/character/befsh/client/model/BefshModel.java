package com.hivesandcolonies.characters.character.befsh.client.model;
import com.hivesandcolonies.characters.character.befsh.entity.BefshEntity;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;

public class BefshModel extends PlayerModel<BefshEntity> {
    public BefshModel(ModelPart root) {
        super(root, false);
    }

    @Override
    public void setupAnim(
            BefshEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.hat.copyFrom(this.head);
        this.jacket.copyFrom(this.body);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
    }
    
}
