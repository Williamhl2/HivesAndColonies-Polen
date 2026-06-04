package com.hivesandcolonies.characters.entity.ai.world.identity;

import java.util.UUID;

/**
 * Persistent identity for the one Polen that belongs to a world.
 *
 * This is not an entity UUID replacement. The entity UUID tells Minecraft which
 * entity is loaded; this identity tells Polen systems which individual story is
 * being lived in this save.
 */
public record PolenIdentity(
        UUID identityId,
        long firstSpawnGameTime,
        long firstSpawnDay,
        long personalitySeed,
        String originDimension
) {
    public static PolenIdentity create(UUID entityUuid, long gameTime, long dayTime, String originDimension) {
        long seed = mix(entityUuid.getMostSignificantBits(), entityUuid.getLeastSignificantBits(), gameTime, dayTime);
        return new PolenIdentity(
                UUID.nameUUIDFromBytes((entityUuid.toString() + ":polen_identity").getBytes(java.nio.charset.StandardCharsets.UTF_8)),
                gameTime,
                Math.max(0L, dayTime / 24000L),
                seed,
                originDimension == null || originDimension.isBlank() ? "minecraft:overworld" : originDimension
        );
    }

    private static long mix(long a, long b, long c, long d) {
        long x = a ^ Long.rotateLeft(b, 21) ^ Long.rotateLeft(c, 37) ^ Long.rotateLeft(d, 11);
        x ^= (x >>> 33);
        x *= 0xff51afd7ed558ccdL;
        x ^= (x >>> 33);
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= (x >>> 33);
        return x;
    }
}
