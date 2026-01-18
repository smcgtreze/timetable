package com.calendarfx.scheduler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for ConflictRuleProvider.
 * Note: Full Form testing is excluded as it requires JavaFX/FormsFX GUI initialization.
 */
class ConflictRuleProviderTest {

    @Test
    void testConflictRuleProviderCanBeInstantiated() {
        // This test just ensures the class exists and basic instantiation works
        assertDoesNotThrow(() -> {
            ConflictRuleProvider provider = new ConflictRuleProvider(
                java.util.Arrays.asList("9to5", "3to11"), 
                800, 
                600, 
                10
            );
            assertNotNull(provider);
        });
    }
}

