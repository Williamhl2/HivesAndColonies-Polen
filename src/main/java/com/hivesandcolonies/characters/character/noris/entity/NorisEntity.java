package com.hivesandcolonies.characters.character.noris.entity;

import com.hivesandcolonies.characters.common.entity.SimpleCharacterEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class NorisEntity extends SimpleCharacterEntity {
    public NorisEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
