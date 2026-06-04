package com.hivesandcolonies.characters.character.polen.entity.ai.brain.state;

import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.characters.character.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.intent.PolenIntentState;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.need.PolenNeedState;
import com.hivesandcolonies.characters.character.polen.entity.ai.brain.task.PolenTaskState;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.affordance.PolenAffordanceType;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.home.PolenResidenceStage;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.observation.PolenObservationDisposition;
import com.hivesandcolonies.characters.character.polen.entity.ai.world.observation.PolenObservationFocus;
import com.hivesandcolonies.characters.common.util.CharacterNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class PolenAiState {

    private BlockPos favoriteFlowerPos;
    private BlockPos favoriteHivePos;
    private BlockPos favoriteSourcePos;
    private BlockPos restingPos;
    private BlockPos residenceAnchorPos;
    private BlockPos residenceUsePos;
    private BlockPos dangerousSpotPos;
    private BlockPos activeLightPos;
    private BlockPos searchTargetPos;
    private BlockPos observedPos;
    private BlockPos observationFocusPos;
    private BlockPos observationUsePos;
    private long dangerousSpotUntilGameTime;
    private long activeLightUntilGameTime;
    private long lastAmbientDialogueGameTime;
    private long lastThoughtDebugGameTime;
    private long nextQuietActivityAllowedGameTime;
    private long nextInterestAllowedGameTime;
    private PolenSearchType searchType = PolenSearchType.IDLE;
    private PolenSearchStatus searchStatus = PolenSearchStatus.IDLE;
    private String searchNote = "";
    private PolenObservationFocus observationFocus = PolenObservationFocus.NONE;
    private PolenObservationDisposition observationDisposition = PolenObservationDisposition.IDLE;
    private PolenAffordanceType observationAffordanceType;
    private PolenResidenceStage residenceStage = PolenResidenceStage.NONE;
    private String residenceContext = "";
    private String observationContext = "";
    private String observationNote = "";
    private String lastThoughtDebugSignature = "";
    private boolean debugThoughtsEnabled;
    private int lastQuietActivityType;
    private BlockPos lastQuietActivityPos;
    private BlockPos lastInterestTargetPos;
    private final PolenNeedState needState = new PolenNeedState();
    private final PolenIntentState intentState = new PolenIntentState();
    private final PolenTaskState taskState = new PolenTaskState();

    public void save(
            CompoundTag tag,
            String favoriteFlowerKey,
            String favoriteHiveKey,
            String favoriteSourceKey,
            String restingKey,
            String residenceAnchorKey,
            String residenceUseKey,
            String residenceContextKey,
            String residenceStageKey,
            String dangerousSpotKey,
            String dangerousSpotUntilKey,
            String activeLightKey,
            String activeLightUntilKey,
            String needsKey,
            String intentKey
    ) {
        CharacterNbtHelper.saveBlockPos(tag, favoriteFlowerKey, this.favoriteFlowerPos);
        CharacterNbtHelper.saveBlockPos(tag, favoriteHiveKey, this.favoriteHivePos);
        CharacterNbtHelper.saveBlockPos(tag, favoriteSourceKey, this.favoriteSourcePos);
        CharacterNbtHelper.saveBlockPos(tag, restingKey, this.restingPos);
        CharacterNbtHelper.saveBlockPos(tag, residenceAnchorKey, this.residenceAnchorPos);
        CharacterNbtHelper.saveBlockPos(tag, residenceUseKey, this.residenceUsePos);
        tag.putString(residenceContextKey, this.residenceContext);
        tag.putString(residenceStageKey, this.residenceStage.name());
        CharacterNbtHelper.saveBlockPos(tag, dangerousSpotKey, this.dangerousSpotPos);
        tag.putLong(dangerousSpotUntilKey, this.dangerousSpotUntilGameTime);
        CharacterNbtHelper.saveBlockPos(tag, activeLightKey, this.activeLightPos);
        tag.putLong(activeLightUntilKey, this.activeLightUntilGameTime);
        this.needState.save(tag, needsKey);
        this.intentState.save(tag, intentKey);
    }

    public void load(
            CompoundTag tag,
            String favoriteFlowerKey,
            String favoriteHiveKey,
            String favoriteSourceKey,
            String restingKey,
            String residenceAnchorKey,
            String residenceUseKey,
            String residenceContextKey,
            String residenceStageKey,
            String dangerousSpotKey,
            String dangerousSpotUntilKey,
            String activeLightKey,
            String activeLightUntilKey,
            String needsKey,
            String intentKey
    ) {
        this.favoriteFlowerPos = CharacterNbtHelper.loadBlockPos(tag, favoriteFlowerKey);
        this.favoriteHivePos = CharacterNbtHelper.loadBlockPos(tag, favoriteHiveKey);
        this.favoriteSourcePos = CharacterNbtHelper.loadBlockPos(tag, favoriteSourceKey);
        this.restingPos = CharacterNbtHelper.loadBlockPos(tag, restingKey);
        this.residenceAnchorPos = CharacterNbtHelper.loadBlockPos(tag, residenceAnchorKey);
        this.residenceUsePos = CharacterNbtHelper.loadBlockPos(tag, residenceUseKey);
        this.residenceContext = tag.getString(residenceContextKey);
        this.residenceStage = PolenResidenceStage.fromName(tag.getString(residenceStageKey));
        this.dangerousSpotPos = CharacterNbtHelper.loadBlockPos(tag, dangerousSpotKey);
        this.dangerousSpotUntilGameTime = Math.max(0L, tag.getLong(dangerousSpotUntilKey));
        this.activeLightPos = CharacterNbtHelper.loadBlockPos(tag, activeLightKey);
        this.activeLightUntilGameTime = Math.max(0L, tag.getLong(activeLightUntilKey));
        this.needState.load(tag, needsKey);
        this.intentState.load(tag, intentKey);
    }

    public BlockPos getFavoriteFlowerPos() {
        return this.favoriteFlowerPos;
    }

    public void setFavoriteFlowerPos(BlockPos favoriteFlowerPos) {
        this.favoriteFlowerPos = favoriteFlowerPos;
    }

    public BlockPos getFavoriteHivePos() {
        return this.favoriteHivePos;
    }

    public void setFavoriteHivePos(BlockPos favoriteHivePos) {
        this.favoriteHivePos = favoriteHivePos;
    }

    public BlockPos getFavoriteSourcePos() {
        return this.favoriteSourcePos;
    }

    public void setFavoriteSourcePos(BlockPos favoriteSourcePos) {
        this.favoriteSourcePos = favoriteSourcePos;
    }

    public BlockPos getRestingPos() {
        return this.restingPos;
    }

    public void setRestingPos(BlockPos restingPos) {
        this.restingPos = restingPos;
    }

    public BlockPos getResidenceAnchorPos() {
        return this.residenceAnchorPos;
    }

    public BlockPos getResidenceUsePos() {
        return this.residenceUsePos;
    }

    public String getResidenceContext() {
        return this.residenceContext;
    }

    public PolenResidenceStage getResidenceStage() {
        return this.residenceStage;
    }

    public void setResidenceState(
            BlockPos residenceAnchorPos,
            BlockPos residenceUsePos,
            String residenceContext,
            PolenResidenceStage residenceStage
    ) {
        this.residenceAnchorPos = residenceAnchorPos;
        this.residenceUsePos = residenceUsePos;
        this.residenceContext = residenceContext == null ? "" : residenceContext;
        this.residenceStage = residenceStage == null ? PolenResidenceStage.NONE : residenceStage;
    }

    public BlockPos getDangerousSpotPos() {
        return this.dangerousSpotPos;
    }

    public long getDangerousSpotUntilGameTime() {
        return this.dangerousSpotUntilGameTime;
    }

    public void setDangerousSpotState(BlockPos dangerousSpotPos, long dangerousSpotUntilGameTime) {
        this.dangerousSpotPos = dangerousSpotPos;
        this.dangerousSpotUntilGameTime = dangerousSpotUntilGameTime;
    }

    public BlockPos getActiveLightPos() {
        return this.activeLightPos;
    }

    public long getActiveLightUntilGameTime() {
        return this.activeLightUntilGameTime;
    }

    public void setActiveLightState(BlockPos activeLightPos, long activeLightUntilGameTime) {
        this.activeLightPos = activeLightPos;
        this.activeLightUntilGameTime = activeLightUntilGameTime;
    }

    public long getLastAmbientDialogueGameTime() {
        return this.lastAmbientDialogueGameTime;
    }

    public void setLastAmbientDialogueGameTime(long lastAmbientDialogueGameTime) {
        this.lastAmbientDialogueGameTime = lastAmbientDialogueGameTime;
    }

    public long getLastThoughtDebugGameTime() {
        return this.lastThoughtDebugGameTime;
    }

    public void setLastThoughtDebugGameTime(long lastThoughtDebugGameTime) {
        this.lastThoughtDebugGameTime = lastThoughtDebugGameTime;
    }

    public long getNextQuietActivityAllowedGameTime() {
        return this.nextQuietActivityAllowedGameTime;
    }

    public void setNextQuietActivityAllowedGameTime(long nextQuietActivityAllowedGameTime) {
        this.nextQuietActivityAllowedGameTime = nextQuietActivityAllowedGameTime;
    }

    public long getNextInterestAllowedGameTime() {
        return this.nextInterestAllowedGameTime;
    }

    public BlockPos getLastInterestTargetPos() {
        return this.lastInterestTargetPos;
    }

    public void setInterestCooldown(BlockPos lastInterestTargetPos, long nextInterestAllowedGameTime) {
        this.lastInterestTargetPos = lastInterestTargetPos == null ? null : lastInterestTargetPos.immutable();
        this.nextInterestAllowedGameTime = Math.max(0L, nextInterestAllowedGameTime);
    }

    public boolean isInterestTargetOnCooldown(BlockPos pos, long gameTime) {
        return pos != null
                && this.lastInterestTargetPos != null
                && gameTime < this.nextInterestAllowedGameTime
                && this.lastInterestTargetPos.distSqr(pos) <= 4.0D;
    }

    public String getLastThoughtDebugSignature() {
        return this.lastThoughtDebugSignature;
    }

    public void setLastThoughtDebugSignature(String lastThoughtDebugSignature) {
        this.lastThoughtDebugSignature = lastThoughtDebugSignature == null ? "" : lastThoughtDebugSignature;
    }

    public boolean isDebugThoughtsEnabled() {
        return this.debugThoughtsEnabled;
    }

    public void setDebugThoughtsEnabled(boolean debugThoughtsEnabled) {
        this.debugThoughtsEnabled = debugThoughtsEnabled;
    }

    public int getLastQuietActivityType() {
        return this.lastQuietActivityType;
    }

    public void setLastQuietActivityType(int lastQuietActivityType) {
        this.lastQuietActivityType = lastQuietActivityType;
    }

    public BlockPos getLastQuietActivityPos() {
        return this.lastQuietActivityPos;
    }

    public void setLastQuietActivityPos(BlockPos lastQuietActivityPos) {
        this.lastQuietActivityPos = lastQuietActivityPos;
    }

    public BlockPos getSearchTargetPos() {
        return this.searchTargetPos;
    }

    public BlockPos getObservedPos() {
        return this.observedPos;
    }

    public PolenSearchType getSearchType() {
        return this.searchType;
    }

    public PolenSearchStatus getSearchStatus() {
        return this.searchStatus;
    }

    public String getSearchNote() {
        return this.searchNote;
    }

    public PolenObservationFocus getObservationFocus() {
        return this.observationFocus;
    }

    public PolenObservationDisposition getObservationDisposition() {
        return this.observationDisposition;
    }

    public PolenAffordanceType getObservationAffordanceType() {
        return this.observationAffordanceType;
    }

    public BlockPos getObservationFocusPos() {
        return this.observationFocusPos;
    }

    public BlockPos getObservationUsePos() {
        return this.observationUsePos;
    }

    public String getObservationContext() {
        return this.observationContext;
    }

    public String getObservationNote() {
        return this.observationNote;
    }

    public void setSearchState(
            PolenSearchType searchType,
            PolenSearchStatus searchStatus,
            BlockPos searchTargetPos,
            BlockPos observedPos,
            String searchNote
    ) {
        this.searchType = searchType == null ? PolenSearchType.IDLE : searchType;
        this.searchStatus = searchStatus == null ? PolenSearchStatus.IDLE : searchStatus;
        this.searchTargetPos = searchTargetPos;
        this.observedPos = observedPos;
        this.searchNote = searchNote == null ? "" : searchNote;
    }

    public void clearSearchState() {
        setSearchState(PolenSearchType.IDLE, PolenSearchStatus.IDLE, null, null, "");
    }

    public void setObservationState(
            PolenObservationFocus observationFocus,
            PolenObservationDisposition observationDisposition,
            PolenAffordanceType observationAffordanceType,
            BlockPos observationFocusPos,
            BlockPos observationUsePos,
            String observationContext,
            String observationNote
    ) {
        this.observationFocus = observationFocus == null ? PolenObservationFocus.NONE : observationFocus;
        this.observationDisposition = observationDisposition == null
                ? PolenObservationDisposition.IDLE
                : observationDisposition;
        this.observationAffordanceType = observationAffordanceType;
        this.observationFocusPos = observationFocusPos;
        this.observationUsePos = observationUsePos;
        this.observationContext = observationContext == null ? "" : observationContext;
        this.observationNote = observationNote == null ? "" : observationNote;
    }

    public PolenNeedState getNeedState() {
        return this.needState;
    }

    public PolenIntentState getIntentState() {
        return this.intentState;
    }

    public PolenTaskState getTaskState() {
        return this.taskState;
    }
}
