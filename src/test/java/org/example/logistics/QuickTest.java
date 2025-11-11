package org.example.logistics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuickTest {

    @Test
    void testBasicAssertion() {
        assertEquals(2, 1 + 1);
        assertTrue(true);
        assertNotNull("test");
    }
}