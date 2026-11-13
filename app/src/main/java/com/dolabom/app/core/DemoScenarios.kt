package com.dolabom.app.core

/** 시연 패널에서 고르는 상황. 실제 인식이 붙기 전까지 가짜 입력을 만드는 데 쓴다. */
enum class DemoScenario(val label: String) {
    COMFORTABLE("편안함"),
    ASLEEP("잠듦"),
    BORED("지루함"),
    MOTION_SICK("멀미"),
    HOT("더위"),
    BELT_UNFASTENED("안전띠 풀림"),
    UNRESPONSIVE("무반응"),
    LEFT_BEHIND("하차 후 방치"),
}

object DemoScenarios {

    /** 평소 실내 온도 */
    const val NORMAL_CABIN_C = 23f

    /** 더위 상황에서 쓰는 실내 온도 */
    const val HOT_SCENARIO_CABIN_C = 30f

    /** 동승자 종류에 맞는 상황만 고른다. 안전띠 풀림은 아이에게만 해당한다. */
    fun available(type: RiderType): List<DemoScenario> =
        DemoScenario.entries.filter { it != DemoScenario.BELT_UNFASTENED || type == RiderType.CHILD }

    /** 이 상황에서 뒷좌석 인식이 내놓을 값 */
    fun observation(scenario: DemoScenario, type: RiderType): RearObservation = when (scenario) {
        DemoScenario.COMFORTABLE, DemoScenario.LEFT_BEHIND -> RearObservation()
        DemoScenario.ASLEEP -> RearObservation(motion = MotionCue.ASLEEP)
        DemoScenario.BORED -> RearObservation(motion = MotionCue.RESTLESS)
        DemoScenario.MOTION_SICK -> when (type) {
            RiderType.CHILD -> RearObservation(sound = SoundCue.CRYING, motion = MotionCue.RESTLESS)
            RiderType.PET -> RearObservation(sound = SoundCue.WHINING, motion = MotionCue.PANTING)
        }
        DemoScenario.HOT -> when (type) {
            RiderType.CHILD -> RearObservation(sound = SoundCue.CRYING)
            RiderType.PET -> RearObservation(motion = MotionCue.PANTING)
        }
        DemoScenario.BELT_UNFASTENED -> RearObservation(beltFastened = false)
        DemoScenario.UNRESPONSIVE -> RearObservation(responsive = false)
    }

    /** 이 상황에서 차량 신호가 내놓을 값 */
    fun vehicle(scenario: DemoScenario): VehicleSnapshot = when (scenario) {
        DemoScenario.LEFT_BEHIND -> VehicleSnapshot(
            parked = true,
            driverPresent = false,
            rearDoorOpen = false,
            cabinTempC = NORMAL_CABIN_C,
        )
        DemoScenario.HOT -> driving(HOT_SCENARIO_CABIN_C)
        else -> driving(NORMAL_CABIN_C)
    }

    private fun driving(cabinTempC: Float) = VehicleSnapshot(
        parked = false,
        driverPresent = true,
        rearDoorOpen = false,
        cabinTempC = cabinTempC,
    )
}
