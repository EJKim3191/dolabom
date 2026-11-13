package com.dolabom.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripMonitorTest {

    private val child = Rider(RiderType.CHILD, "하준")
    private val driving = VehicleSnapshot(cabinTempC = 23f)
    private val parkedAlone = VehicleSnapshot(
        parked = true, driverPresent = false, rearDoorOpen = false, cabinTempC = 23f,
    )
    private val calm = RearObservation()
    private val sick = RearObservation(sound = SoundCue.CRYING, motion = MotionCue.RESTLESS)

    @Test
    fun calmTripShowsNoAlertAndSaysNothing() {
        val monitor = TripMonitor()
        val update = monitor.update(child, calm, driving, 0)
        assertEquals(CareState.COMFORTABLE, update.assessment.state)
        assertFalse(update.showAlert)
        assertNull(update.spoken)
        assertTrue(update.changed)
        assertFalse(monitor.update(child, calm, driving, 1).changed)
    }

    @Test
    fun motionSicknessRaisesAnAlertWithARestStop() {
        val monitor = TripMonitor()
        monitor.update(child, calm, driving, 0)
        val update = monitor.update(child, sick, driving, 5)
        assertEquals(CareState.MOTION_SICK, update.assessment.state)
        assertEquals(SuggestionKind.REST_STOP, update.assessment.suggestion)
        assertTrue(update.showAlert)
        assertEquals(update.assessment.spoken, update.spoken)
    }

    @Test
    fun dismissedAlertStaysClosedWhileTheStateLasts() {
        val monitor = TripMonitor()
        val first = monitor.update(child, sick, driving, 0)
        monitor.dismiss(first.assessment)
        assertFalse(monitor.update(child, sick, driving, 1).showAlert)
        assertFalse(monitor.update(child, sick, driving, 50).showAlert)
    }

    @Test
    fun alertComesBackWhenTheStateReturns() {
        val monitor = TripMonitor()
        val first = monitor.update(child, sick, driving, 0)
        monitor.dismiss(first.assessment)
        monitor.update(child, calm, driving, 10)
        assertTrue(monitor.update(child, sick, driving, 20).showAlert)
    }

    @Test
    fun leftBehindTimerCountsFromTheMomentItStarts() {
        val monitor = TripMonitor()
        monitor.update(child, calm, driving, 100)
        val start = monitor.update(child, calm, parkedAlone, 110)
        assertEquals(CareState.LEFT_BEHIND, start.assessment.state)
        assertEquals(0, start.leftBehindSeconds)
        assertEquals(1, start.assessment.leftBehindStage)

        val later = monitor.update(child, calm, parkedAlone, 140)
        assertEquals(30, later.leftBehindSeconds)
        assertEquals(1, later.assessment.leftBehindStage)
    }

    @Test
    fun leftBehindEscalatesAndReopensADismissedAlert() {
        val monitor = TripMonitor()
        val stage1 = monitor.update(child, calm, parkedAlone, 0)
        monitor.dismiss(stage1.assessment)
        assertFalse(monitor.update(child, calm, parkedAlone, 30).showAlert)

        val stage2 = monitor.update(child, calm, parkedAlone, CareEngine.FAMILY_ALERT_AFTER_SEC.toLong())
        assertEquals(2, stage2.assessment.leftBehindStage)
        assertTrue(stage2.showAlert)
        assertTrue(stage2.changed)

        val stage3 = monitor.update(child, calm, parkedAlone, CareEngine.EMERGENCY_AFTER_SEC.toLong())
        assertEquals(3, stage3.assessment.leftBehindStage)
        assertEquals(Severity.EMERGENCY, stage3.assessment.severity)
        assertTrue(stage3.showAlert)
    }

    @Test
    fun leftBehindTimerRestartsAfterTheDriverReturns() {
        val monitor = TripMonitor()
        monitor.update(child, calm, parkedAlone, 0)
        monitor.update(child, calm, parkedAlone, 100)
        val back = monitor.update(child, calm, parkedAlone.copy(driverPresent = true), 110)
        assertEquals(0, back.leftBehindSeconds)
        assertEquals(CareState.COMFORTABLE, back.assessment.state)

        val again = monitor.update(child, calm, parkedAlone, 200)
        assertEquals(0, again.leftBehindSeconds)
        assertEquals(1, again.assessment.leftBehindStage)
    }

    @Test
    fun openRearDoorIsNotLeftBehind() {
        val monitor = TripMonitor()
        val update = monitor.update(child, calm, parkedAlone.copy(rearDoorOpen = true), 0)
        assertEquals(CareState.COMFORTABLE, update.assessment.state)
        assertFalse(update.showAlert)
    }

    @Test
    fun resetForgetsTheTimerAndDismissals() {
        val monitor = TripMonitor()
        val first = monitor.update(child, calm, parkedAlone, 0)
        monitor.dismiss(first.assessment)
        monitor.update(child, calm, parkedAlone, 50)
        monitor.reset()
        val fresh = monitor.update(child, calm, parkedAlone, 500)
        assertEquals(0, fresh.leftBehindSeconds)
        assertTrue(fresh.showAlert)
        assertEquals(fresh.assessment.spoken, fresh.spoken)
    }
}
