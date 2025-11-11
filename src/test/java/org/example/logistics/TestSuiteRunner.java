package org.example.logistics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSuiteRunner {

    @Test
    void contextLoads() {
        // Test que le contexte Spring se charge correctement
        assertTrue(true);
    }

    @Test
    void testJUnit5Configuration() {
        // Vérification que JUnit 5 fonctionne
        assertNotNull(this);
        assertEquals(2, 1 + 1);
    }

    @Test
    void testMockitoConfiguration() {
        // Test basique pour vérifier que Mockito est disponible
        assertDoesNotThrow(() -> {
            org.mockito.Mockito.mock(String.class);
        });
    }
}