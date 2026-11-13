package com.dolabom.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoScenariosTest {

    private val child = Rider(RiderType.CHILD, "하준")
    private val pet = Rider(RiderType.PET, "콩이")

    private fun stateOf(scenario: DemoScenario, rider: Rider): CareState = CareEngine.assess(
        rider,
        DemoScenarios.observation(scenario, rider.type),
        DemoScenarios.vehicle(scenario),
    ).state

    @Test
    fun everyScenarioLeadsToItsStateForAChild() {
        assertEquals(CareState.COMFORTABLE, stateOf(DemoScenario.COMFORTABLE, child))
        assertEquals(CareState.ASLEEP, stateOf(DemoScenario.ASLEEP, child))
        assertEquals(CareState.RESTLESS, stateOf(DemoScenario.BORED, child))
        assertEquals(CareState.MOTION_SICK, stateOf(DemoScenario.MOTION_SICK, child))
        assertEquals(CareState.HOT, stateOf(DemoScenario.HOT, child))
        assertEquals(CareState.BELT_UNFASTENED, stateOf(DemoScenario.BELT_UNFASTENED, child))
        assertEquals(CareState.UNRESPONSIVE, stateOf(DemoScenario.UNRESPONSIVE, child))
        assertEquals(CareState.LEFT_BEHIND, stateOf(DemoScenario.LEFT_BEHIND, child))
    }

    @Test
    fun everyScenarioLeadsToItsStateForAPet() {
        assertEquals(CareState.COMFORTABLE, stateOf(DemoScenario.COMFORTABLE, pet))
        assertEquals(CareState.ASLEEP, stateOf(DemoScenario.ASLEEP, pet))
        assertEquals(CareState.RESTLESS, stateOf(DemoScenario.BORED, pet))
        assertEquals(CareState.MOTION_SICK, stateOf(DemoScenario.MOTION_SICK, pet))
        assertEquals(CareState.HOT, stateOf(DemoScenario.HOT, pet))
        assertEquals(CareState.UNRESPONSIVE, stateOf(DemoScenario.UNRESPONSIVE, pet))
        assertEquals(CareState.LEFT_BEHIND, stateOf(DemoScenario.LEFT_BEHIND, pet))
    }

    @Test
    fun beltScenarioIsOfferedOnlyForChildren() {
        assertTrue(DemoScenario.BELT_UNFASTENED in DemoScenarios.available(RiderType.CHILD))
        assertFalse(DemoScenario.BELT_UNFASTENED in DemoScenarios.available(RiderType.PET))
        assertEquals(DemoScenario.entries.size, DemoScenarios.available(RiderType.CHILD).size)
    }

    @Test
    fun clockFormatsMinutesAndSeconds() {
        assertEquals("00:00", Clock.mmss(0))
        assertEquals("01:15", Clock.mmss(75))
        assertEquals("12:05", Clock.mmss(725))
        assertEquals("00:00", Clock.mmss(-3))
    }
}
