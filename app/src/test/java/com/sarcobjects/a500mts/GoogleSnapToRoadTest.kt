package com.sarcobjects.a500mts

import com.google.android.gms.maps.model.LatLng
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Mainly testing methods in abstract class SnapToRoad
 */
class GoogleSnapToRoadTest {
    private var underTest: GoogleSnapToRoad? = null

    @Before
    fun setUp() {
        underTest = GoogleSnapToRoad(null)
    }

    @Test
    fun testCalculatePaths_Center() {
        underTest = GoogleSnapToRoad(4)
        val latLng = LatLng(0.0, 0.0)
        val latLngs = underTest!!.calculatePaths(latLng, 500.0)
        val softly = SoftAssertions()
        softly.assertThat(latLngs.stream().mapToDouble { l: LatLng -> l.latitude }.toArray())
                .`as`("for latitude of 0,0")
                .usingComparatorWithPrecision(0.0001)
                .containsExactly(0.00449, 0.0, -0.00449, 0.0, 0.00449)
        softly.assertThat(latLngs.stream().mapToDouble { l: LatLng -> l.longitude }.toArray())
                .`as`("for longitude of 0,0")
                .usingComparatorWithPrecision(0.0001)
                .containsExactly(0.0, 0.00449, 0.0, -0.00449, 0.0)
        softly.assertAll()
    }

    @Test
    fun testCalculatePaths_BuenosAires() {
        underTest = GoogleSnapToRoad(4)
        val latLng = LatLng(-34.60370, -58.38160)
        val latLngs = underTest!!.calculatePaths(latLng, 500.0)
        val softly = SoftAssertions()
        softly.assertThat(latLngs.stream().mapToDouble { l: LatLng -> l.latitude }.toArray())
                .`as`("for latitude of Buenos Aires")
                .usingComparatorWithPrecision(0.001)
                .containsExactly(-34.59921, -34.60370, -34.60819, -34.60370, -34.59921)
        softly.assertThat(latLngs.stream().mapToDouble { l: LatLng -> l.longitude }.toArray())
                .`as`("for longitude of Buenos Aires")
                .usingComparatorWithPrecision(0.001)
                .containsExactly(-58.38160, -58.37625, -58.38160, -58.38695, -58.38160)
        softly.assertAll()
    }

    @Test
    fun testFormatPath_empty() {
        val latLngs: List<LatLng?> = ArrayList()
        val path = underTest!!.formatPaths(latLngs)
        Assertions.assertThat(path).isEmpty()
    }

    @Test
    fun testFormatPath_2() {
        val latLngs: MutableList<LatLng?> = ArrayList()
        latLngs.add(LatLng(0.0, 0.0))
        latLngs.add(LatLng(5.55, 6.66))
        val path = underTest!!.formatPaths(latLngs)
        Assertions.assertThat(path).isEqualTo("0.0,0.0|5.55,6.66")
    }

    @Test
    fun testFormatPath_1() {
        val latLngs: MutableList<LatLng?> = ArrayList()
        latLngs.add(LatLng(5.55, 6.66))
        val path = underTest!!.formatPaths(latLngs)
        Assertions.assertThat(path).isEqualTo("5.55,6.66")
    }

    @Test
    fun testFndAngles() {
        val angles = underTest!!.findAngles(4)
        Assertions.assertThat(angles)
                .hasSize(4)
                .usingComparatorWithPrecision(1.0E-5)
                .containsExactly(0.0, 1.570796327, 3.141592654, 4.71238898)
    }

    @Test
    fun testFndAngles_AllTo20() {
        val atomic = AtomicInteger()
        for (i in 1..19) {
            val angles = underTest!!.findAngles(i)
            Assertions.assertThat(angles)
                    .hasSize(i)
                    .satisfies { a: DoubleArray ->
                        Assertions.assertThat(a[0]).isEqualTo(0.0)
                        Assertions.assertThat(a[atomic.getAndAdd(1)]).isLessThan(6.28)
                    }
        }
    }
}