package com.hivesandcolonies.characters.entity.ai.navigation.safety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolenDangerMemoryMathTest {

    @Test
    void dangerousMemorySpotUsesHorizontalRadius() {
        assertTrue(PolenDangerMemoryMath.isDangerousMemorySpot(10, 40, 10, 12, 40, 12, 3.0D));
        assertFalse(PolenDangerMemoryMath.isDangerousMemorySpot(10, 40, 10, 20, 40, 20, 3.0D));
    }

    @Test
    void dangerousMemorySpotIgnoresFarVerticalDifference() {
        assertFalse(PolenDangerMemoryMath.isDangerousMemorySpot(10, 40, 10, 10, 50, 10, 6.0D));
    }
}
