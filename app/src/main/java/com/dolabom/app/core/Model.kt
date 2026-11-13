package com.dolabom.app.core

/** 뒷좌석에 누가 탔는지. */
enum class RiderType { CHILD, PET }

data class Rider(val type: RiderType, val name: String)

/** 소리 인식이 알려주는 단서. */
enum class SoundCue { QUIET, CRYING, WHINING, BARKING }

/** 영상 인식이 알려주는 단서. */
enum class MotionCue { STILL, ASLEEP, RESTLESS, PANTING }

/** 뒷좌석 인식 결과. 지금은 시연 패널이 값을 넣고, 이후 실제 영상과 소리 인식이 채운다. */
data class RearObservation(
    val occupantPresent: Boolean = true,
    val sound: SoundCue = SoundCue.QUIET,
    val motion: MotionCue = MotionCue.STILL,
    val beltFastened: Boolean = true,
    val responsive: Boolean = true,
)

/** 차량 신호. 값을 알 수 없으면 null이다. */
data class VehicleSnapshot(
    /** 시동이 꺼졌거나 주차 상태인지 */
    val parked: Boolean = false,
    val driverPresent: Boolean = true,
    val rearDoorOpen: Boolean? = false,
    val cabinTempC: Float? = null,
)

enum class CareState {
    COMFORTABLE,
    ASLEEP,
    RESTLESS,
    MOTION_SICK,
    HOT,
    BELT_UNFASTENED,
    UNRESPONSIVE,
    LEFT_BEHIND,
}

enum class Severity { INFO, NOTICE, WARNING, EMERGENCY }

enum class SuggestionKind {
    REST_STOP,
    PET_WALK,
    COOL_CABIN,
    CHECK_BELT,
    EMERGENCY_ROOM,
    VET_24H,
    RETURN_TO_CAR,
}

/** 한 시점의 판단 결과. 진단이 아니라 "확인해 보세요" 수준의 안내다. */
data class Assessment(
    val state: CareState,
    val severity: Severity,
    /** 화면에 크게 보여줄 한 줄 */
    val headline: String,
    /** 음성으로 읽어줄 한 줄. 비어 있으면 말하지 않는다. */
    val spoken: String,
    val suggestion: SuggestionKind? = null,
    /** 하차 후 방치 단계. 0=해당 없음, 1=운전자 알림, 2=가족 알림, 3=긴급 신고 안내 */
    val leftBehindStage: Int = 0,
) {
    /** 운전자의 확인이 필요한 상태인지 */
    val needsAttention: Boolean
        get() = severity >= Severity.WARNING || suggestion != null

    /** 같은 알림인지 구분하는 키 */
    val key: String
        get() = state.name + ":" + leftBehindStage
}

data class Place(val name: String, val detail: String)
