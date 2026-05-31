package com.hivesandcolonies.polen.entity.ai.safety;

public final class PolenDangerMemoryMath {

    private PolenDangerMemoryMath() {
    }

    public static boolean isDangerousMemorySpot(
            int dangerX,
            int dangerY,
            int dangerZ,
            int x,
            int y,
            int z,
            double radius
    ) {
        double dx = (dangerX + 0.5D) - (x + 0.5D);
        double dy = (dangerY + 0.5D) - (y + 0.5D);
        double dz = (dangerZ + 0.5D) - (z + 0.5D);

        if (Math.abs(dy) > 3.0D) {
            return false;
        }

        return (dx * dx + dz * dz) <= (radius * radius);
    }
}
