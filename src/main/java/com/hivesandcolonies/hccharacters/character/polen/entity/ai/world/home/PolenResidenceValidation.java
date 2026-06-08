package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

public record PolenResidenceValidation(
        PolenResidenceTarget target,
        String failureTranslationKey
) {
    public boolean isSuccess() {
        return this.target != null;
    }

    public static PolenResidenceValidation success(PolenResidenceTarget target) {
        return new PolenResidenceValidation(target, "");
    }

    public static PolenResidenceValidation failure(String failureTranslationKey) {
        return new PolenResidenceValidation(null, failureTranslationKey == null ? "" : failureTranslationKey);
    }
}
