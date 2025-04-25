package org.example;

public class TestUtils {
    public static void assertTrue(boolean b) {
        if (!b) {
            throw new RuntimeException("Failed assertion");
        }
    }
}
