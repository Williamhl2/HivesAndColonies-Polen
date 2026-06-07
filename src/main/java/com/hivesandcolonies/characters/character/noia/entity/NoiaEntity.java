package com.hivesandcolonies.characters.character.noia.entity;

import com.hivesandcolonies.characters.common.entity.SimpleCharacterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class NoiaEntity extends SimpleCharacterEntity {
    public NoiaEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
