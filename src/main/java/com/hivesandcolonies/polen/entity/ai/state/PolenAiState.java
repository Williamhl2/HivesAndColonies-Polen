package com.hivesandcolonies.polen.entity.ai.state;

import com.hivesandcolonies.polen.entity.ai.intent.PolenIntentState;
import com.hivesandcolonies.polen.entity.ai.need.PolenNeedState;
import com.hivesandcolonies.polen.util.PolenNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class PolenAiState {

    private BlockPos favoriteFlowerPos;
    private BlockPos favoriteHivePos;
    private BlockPos favoriteSourcePos;
    private BlockPos restingPos;
    private BlockPos dangerousSpotPos;
    private BlockPos activeLightPos;
    private long dangerousSpotUntilGameTime;
    private long activeLightUntilGameTime;
    private long lastAmbientDialogueGameTime;
    private final PolenNeedState needState = new PolenNeedState();
    private final PolenIntentState intentState = new PolenIntentState();

    public void save(
            CompoundTag tag,
            String favoriteFlowerKey,
            String favoriteHiveKey,
            String favoriteSourceKey,
            String restingKey,
            String dangerousSpotKey,
            String dangerousSpotUntilKey,
            String activeLightKey,
            String activeLightUntilKey,
            String needsKey,
            String intentKey
    ) {
        PolenNbtHelper.saveBlockPos(tag, favoriteFlowerKey, this.favoriteFlowerPos);
        PolenNbtHelper.saveBlockPos(tag, favoriteHiveKey, this.favoriteHivePos);
        PolenNbtHelper.saveBlockPos(tag, favoriteSourceKey, this.favoriteSourcePos);
        PolenNbtHelper.saveBlockPos(tag, restingKey, this.restingPos);
        PolenNbtHelper.saveBlockPos(tag, dangerousSpotKey, this.dangerousSpotPos);
        tag.putLong(dangerousSpotUntilKey, this.dangerousSpotUntilGameTime);
        PolenNbtHelper.saveBlockPos(tag, activeLightKey, this.activeLightPos);
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
            String dangerousSpotKey,
            String dangerousSpotUntilKey,
            String activeLightKey,
            String activeLightUntilKey,
            String needsKey,
            String intentKey
    ) {
        this.favoriteFlowerPos = PolenNbtHelper.loadBlockPos(tag, favoriteFlowerKey);
        this.favoriteHivePos = PolenNbtHelper.loadBlockPos(tag, favoriteHiveKey);
        this.favoriteSourcePos = PolenNbtHelper.loadBlockPos(tag, favoriteSourceKey);
        this.restingPos = PolenNbtHelper.loadBlockPos(tag, restingKey);
        this.dangerousSpotPos = PolenNbtHelper.loadBlockPos(tag, dangerousSpotKey);
        this.dangerousSpotUntilGameTime = Math.max(0L, tag.getLong(dangerousSpotUntilKey));
        this.activeLightPos = PolenNbtHelper.loadBlockPos(tag, activeLightKey);
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

    public PolenNeedState getNeedState() {
        return this.needState;
    }

    public PolenIntentState getIntentState() {
        return this.intentState;
    }
}
