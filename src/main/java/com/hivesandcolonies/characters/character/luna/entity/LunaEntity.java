package com.hivesandcolonies.characters.character.luna.entity;

import com.hivesandcolonies.characters.common.entity.SimpleCharacterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class LunaEntity extends SimpleCharacterEntity {
    public LunaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
