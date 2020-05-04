package com.sarcobjects.a500mts;

import com.google.android.gms.maps.model.LatLng;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleSnapToRoadTest {

    private GoogleSnapToRoad underTest;

    @Before
    public void setUp() {
        underTest = new GoogleSnapToRoad(null);
    }


    @Test
    public void testCalculatePaths_Center() {
        underTest = new GoogleSnapToRoad(4);

        LatLng latLng = new LatLng(0.0, 0.0);
        List<LatLng> latLngs = underTest.calculatePaths(latLng, 500);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(latLngs.stream().mapToDouble(l -> l.latitude).toArray())
                .as("for latitude of 0,0")
                .usingComparatorWithPrecision(0.0001d)
                .containsExactly(0.00449d, 0.0d, -0.00449d, 0.0d);

        softly.assertThat(latLngs.stream().mapToDouble(l -> l.longitude).toArray())
                .as("for longitude of 0,0")
                .usingComparatorWithPrecision(0.0001d)
                .containsExactly(0.0d, 0.00449d, 0.0d, -0.00449d);
        softly.assertAll();
    }

    @Test
    public void testCalculatePaths_BuenosAires() {
        underTest = new GoogleSnapToRoad(4);

        LatLng latLng = new LatLng(-34.60370d, -58.38160d);
        List<LatLng> latLngs = underTest.calculatePaths(latLng, 500);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(latLngs.stream().mapToDouble(l -> l.latitude).toArray())
                .as("for latitude of Buenos Aires")
                .usingComparatorWithPrecision(0.0001d)
                .containsExactly(-34.59921d, -34.60370d, -34.60819d, -34.60370d);

        softly.assertThat(latLngs.stream().mapToDouble(l -> l.longitude).toArray())
                .as("for longitude of Buenos Aires")
                .usingComparatorWithPrecision(0.0001d)
                .containsExactly(-58.38160d, -58.37303d, -58.38160d, -58.39017d);
        softly.assertAll();
    }

    @Test
    public void testFormatPath_empty() {
        List<LatLng> latLngs = new ArrayList<>();

        String path = underTest.formatPaths(latLngs);

        assertThat(path).isEqualTo("&path=");
    }

    @Test
    public void testFormatPath_2() {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(0.0,0.0));
        latLngs.add(new LatLng(5.55,6.66));

        String path = underTest.formatPaths(latLngs);

        assertThat(path).isEqualTo("&path=0.0,0.0|5.55,6.66");
    }

    @Test
    public void testFormatPath_1() {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(5.55,6.66));

        String path = underTest.formatPaths(latLngs);

        assertThat(path).isEqualTo("&path=5.55,6.66");
    }


    @Test
    public void testFndAngles() {
        double[] angles = underTest.findAngles(4);

        assertThat(angles)
                .hasSize(4)
                .usingComparatorWithPrecision(1.0E-5)
                .containsExactly(0.0, 1.570796327, 3.141592654, 4.71238898);
    }


    @Test
    public void testFndAngles_AllTo20() {

        AtomicInteger atomic = new AtomicInteger();
        for (int i = 1; i <20; i++) {
            double[] angles = underTest.findAngles(i);
            assertThat(angles)
                    .hasSize(i)
                    .satisfies(a -> {
                        assertThat(a[0]).isEqualTo(0.0);
                        assertThat(a[atomic.getAndAdd(1)]).isLessThan(6.28);
                    });
        }
    }
}