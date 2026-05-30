package com.hivesandcolonies.polen.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

public final class PolenNbtHelper {

    private PolenNbtHelper() {
    }

    public static void saveBlockPos(CompoundTag tag, String key, BlockPos pos) {
        if (pos == null) {
            return;
        }

        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        tag.put(key, posTag);
    }

    public static BlockPos loadBlockPos(CompoundTag tag, String key) {
        if (!tag.contains(key, CompoundTag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag posTag = tag.getCompound(key);
        return new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
    }
}
