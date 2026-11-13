package com.dolabom.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CareEngineTest {

    private val pet = Rider(RiderType.PET, "콩이")
    private val child = Rider(RiderType.CHILD, "서준")
    private val driving = VehicleSnapshot(parked = false, driverPresent = true, rearDoorOpen = false, cabinTempC = 23f)

    @Test
    fun calmRiderIsComfortable() {
        val a = CareEngine.assess(pet, RearObservation(), driving)
        assertEquals(CareState.COMFORTABLE, a.state)
        assertEquals(Severity.INFO, a.severity)
        assertFalse(a.needsAttention)
    }

    @Test
    fun emptySeatIsNotAnAlert() {
        val a = CareEngine.assess(pet, RearObservation(occupantPresent = false), driving)
        assertEquals(CareState.COMFORTABLE, a.state)
        assertEquals("", a.spoken)
    }

    @Test
    fun pantingAndWhiningPetIsMotionSick() {
        val obs = RearObservation(sound = SoundCue.WHINING, motion = MotionCue.PANTING)
        val a = CareEngine.assess(pet, obs, driving)
        assertEquals(CareState.MOTION_SICK, a.state)
        assertEquals(SuggestionKind.PET_WALK, a.suggestion)
        assertTrue(a.needsAttention)
        assertEquals("콩이가 멀미하는 것 같아요. 잠깐 쉬어 갈까요?", a.spoken)
    }

    @Test
    fun cryingAndRestlessChildGetsRestStop() {
        val obs = RearObservation(sound = SoundCue.CRYING, motion = MotionCue.RESTLESS)
        val a = CareEngine.assess(child, obs, driving)
        assertEquals(CareState.MOTION_SICK, a.state)
        assertEquals(SuggestionKind.REST_STOP, a.suggestion)
        assertEquals("서준이 멀미하는 것 같아요. 잠깐 쉬어 갈까요?", a.spoken)
    }

    @Test
    fun sleepingRiderIsInfoOnly() {
        val a = CareEngine.assess(child, RearObservation(motion = MotionCue.ASLEEP), driving)
        assertEquals(CareState.ASLEEP, a.state)
        assertNull(a.suggestion)
        assertFalse(a.needsAttention)
    }

    @Test
    fun unbuckledChildWhileDrivingIsWarning() {
        val a = CareEngine.assess(child, RearObservation(beltFastened = false), driving)
        assertEquals(CareState.BELT_UNFASTENED, a.state)
        assertEquals(Severity.WARNING, a.severity)
        assertEquals(SuggestionKind.CHECK_BELT, a.suggestion)
    }

    @Test
    fun unbuckledBeltIsIgnoredForPetsAndWhenParked() {
        val obs = RearObservation(beltFastened = false)
        assertEquals(CareState.COMFORTABLE, CareEngine.assess(pet, obs, driving).state)
        val parkedWithDriver = driving.copy(parked = true)
        assertEquals(CareState.COMFORTABLE, CareEngine.assess(child, obs, parkedWithDriver).state)
    }

    @Test
    fun hotCabinWithPantingIsHot() {
        val obs = RearObservation(motion = MotionCue.PANTING)
        val a = CareEngine.assess(pet, obs, driving.copy(cabinTempC = 29.4f))
        assertEquals(CareState.HOT, a.state)
        assertEquals(SuggestionKind.COOL_CABIN, a.suggestion)
        assertEquals("실내가 29도예요. 콩이가 더워하는 것 같아요.", a.spoken)
    }

    @Test
    fun unknownTemperatureNeverReportsHot() {
        val obs = RearObservation(motion = MotionCue.PANTING)
        val a = CareEngine.assess(pet, obs, driving.copy(cabinTempC = null))
        assertEquals(CareState.RESTLESS, a.state)
    }

    @Test
    fun unresponsiveRiderIsEmergencyWithRightDestination() {
        val obs = RearObservation(responsive = false)
        val petResult = CareEngine.assess(pet, obs, driving)
        assertEquals(CareState.UNRESPONSIVE, petResult.state)
        assertEquals(Severity.EMERGENCY, petResult.severity)
        assertEquals(SuggestionKind.VET_24H, petResult.suggestion)
        assertEquals(SuggestionKind.EMERGENCY_ROOM, CareEngine.assess(child, obs, driving).suggestion)
    }

    @Test
    fun leftBehindEscalatesWithTime() {
        val left = VehicleSnapshot(parked = true, driverPresent = false, rearDoorOpen = false, cabinTempC = 22f)
        val obs = RearObservation()
        assertEquals(1, CareEngine.assess(child, obs, left, 0).leftBehindStage)
        assertEquals(2, CareEngine.assess(child, obs, left, CareEngine.FAMILY_ALERT_AFTER_SEC).leftBehindStage)
        val third = CareEngine.assess(child, obs, left, CareEngine.EMERGENCY_AFTER_SEC)
        assertEquals(3, third.leftBehindStage)
        assertEquals(Severity.EMERGENCY, third.severity)
    }

    @Test
    fun leftBehindEscalatesWithHeat() {
        val obs = RearObservation()
        val warm = VehicleSnapshot(parked = true, driverPresent = false, rearDoorOpen = false, cabinTempC = 28f)
        val hot = warm.copy(cabinTempC = 33f)
        assertEquals(2, CareEngine.assess(pet, obs, warm, 5).leftBehindStage)
        assertEquals(3, CareEngine.assess(pet, obs, hot, 5).leftBehindStage)
    }

    @Test
    fun leftBehindNeedsParkedCarAbsentDriverAndClosedDoor() {
        val obs = RearObservation()
        val left = VehicleSnapshot(parked = true, driverPresent = false, rearDoorOpen = false)
        assertTrue(CareEngine.isLeftBehind(obs, left))
        assertFalse(CareEngine.isLeftBehind(obs, left.copy(parked = false)))
        assertFalse(CareEngine.isLeftBehind(obs, left.copy(driverPresent = true)))
        assertFalse(CareEngine.isLeftBehind(obs, left.copy(rearDoorOpen = true)))
        assertFalse(CareEngine.isLeftBehind(obs.copy(occupantPresent = false), left))
        // 뒷문 상태를 모르면 닫힌 것으로 보고 놓치지 않는다.
        assertTrue(CareEngine.isLeftBehind(obs, left.copy(rearDoorOpen = null)))
    }

    @Test
    fun leftBehindOutranksEverythingElse() {
        val left = VehicleSnapshot(parked = true, driverPresent = false, rearDoorOpen = false)
        val obs = RearObservation(responsive = false, motion = MotionCue.ASLEEP)
        assertEquals(CareState.LEFT_BEHIND, CareEngine.assess(pet, obs, left).state)
    }
}
