package com.dolabom.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnnouncementPolicyTest {

    private val pet = Rider(RiderType.PET, "콩이")
    private val driving = VehicleSnapshot(cabinTempC = 23f)

    private val calm = CareEngine.assess(pet, RearObservation(), driving)
    private val asleep = CareEngine.assess(pet, RearObservation(motion = MotionCue.ASLEEP), driving)
    private val sick = CareEngine.assess(
        pet, RearObservation(sound = SoundCue.WHINING, motion = MotionCue.PANTING), driving,
    )
    private val unresponsive = CareEngine.assess(pet, RearObservation(responsive = false), driving)

    @Test
    fun staysQuietWhenTripStartsCalm() {
        val policy = AnnouncementPolicy()
        assertNull(policy.next(calm, 0))
        assertNull(policy.next(calm, 1))
    }

    @Test
    fun speaksOnceWhenStateChanges() {
        val policy = AnnouncementPolicy()
        policy.next(calm, 0)
        assertEquals(asleep.spoken, policy.next(asleep, 5))
        assertNull(policy.next(asleep, 6))
        assertNull(policy.next(asleep, 60))
    }

    @Test
    fun saysComfortableAgainOnlyAfterANotice() {
        val policy = AnnouncementPolicy()
        policy.next(calm, 0)
        policy.next(asleep, 5)
        // 잠에서 깨어 편안해진 것은 굳이 알리지 않는다.
        assertNull(policy.next(calm, 10))
        policy.next(sick, 20)
        assertEquals(calm.spoken, policy.next(calm, 30))
    }

    @Test
    fun repeatsWarningsOnAnInterval() {
        val policy = AnnouncementPolicy(repeatWarningEverySec = 30)
        assertEquals(unresponsive.spoken, policy.next(unresponsive, 100))
        assertNull(policy.next(unresponsive, 110))
        assertEquals(unresponsive.spoken, policy.next(unresponsive, 130))
        assertNull(policy.next(unresponsive, 140))
    }

    @Test
    fun doesNotRepeatNotices() {
        val policy = AnnouncementPolicy(repeatWarningEverySec = 30)
        assertEquals(sick.spoken, policy.next(sick, 0))
        assertNull(policy.next(sick, 100))
    }

    @Test
    fun leftBehindStageChangeIsANewAnnouncement() {
        val policy = AnnouncementPolicy()
        val left = VehicleSnapshot(parked = true, driverPresent = false, rearDoorOpen = false, cabinTempC = 22f)
        val stage1 = CareEngine.assess(pet, RearObservation(), left, 0)
        val stage2 = CareEngine.assess(pet, RearObservation(), left, CareEngine.FAMILY_ALERT_AFTER_SEC)
        assertEquals(stage1.spoken, policy.next(stage1, 0))
        assertEquals(stage2.spoken, policy.next(stage2, 60))
    }

    @Test
    fun resetForgetsHistory() {
        val policy = AnnouncementPolicy()
        policy.next(sick, 0)
        policy.reset()
        assertNull(policy.next(calm, 1))
    }
}
