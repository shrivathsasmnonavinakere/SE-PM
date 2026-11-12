package com.annadhata;

import com.annadhata.util.GeoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {

    @Test
    @DisplayName("Haversine calculates accurate distance between two coordinates")
    void testHaversineDistance() {
        // MG Road to Indiranagar, Bangalore: ~4.5 km
        double distance = GeoUtils.haversineDistance(12.9716, 77.5946, 12.9784, 77.6408);
        assertTrue(distance > 4.0 && distance < 6.0, "Expected distance around 5 km, got: " + distance);
    }

    @Test
    @DisplayName("Haversine returns 0 for identical coordinates")
    void testSamePoint() {
        double distance = GeoUtils.haversineDistance(12.9716, 77.5946, 12.9716, 77.5946);
        assertEquals(0.0, distance, 0.01);
    }

    @Test
    @DisplayName("Haversine returns fallback distance for null coordinates")
    void testNullCoordinates() {
        double distance = GeoUtils.haversineDistance(null, 77.5946, 12.9716, 77.5946);
        assertEquals(999.0, distance);
    }
}
