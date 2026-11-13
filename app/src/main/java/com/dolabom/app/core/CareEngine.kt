package com.dolabom.app.core

import kotlin.math.roundToInt

/**
 * 뒷좌석 인식 결과와 차량 신호를 합쳐 지금 상태를 판단한다.
 * 안드로이드에 의존하지 않는 순수 로직이라 단위 테스트로 검증한다.
 */
object CareEngine {

    /** 이 온도부터 더위를 의심한다. */
    const val HOT_CABIN_C = 28f

    /** 이 온도부터 방치 상황을 긴급으로 올린다. */
    const val DANGER_CABIN_C = 32f

    /** 방치 후 이 시간이 지나면 가족에게 알린다. */
    const val FAMILY_ALERT_AFTER_SEC = 60

    /** 방치 후 이 시간이 지나면 긴급 신고를 안내한다. */
    const val EMERGENCY_AFTER_SEC = 180

    /** 주차 후 운전자가 내렸는데 뒷좌석에 동승자가 남아 있고 뒷문이 닫혀 있는 상태. */
    fun isLeftBehind(obs: RearObservation, vehicle: VehicleSnapshot): Boolean =
        obs.occupantPresent &&
            vehicle.parked &&
            !vehicle.driverPresent &&
            vehicle.rearDoorOpen != true

    fun leftBehindStage(seconds: Int, cabinTempC: Float?): Int = when {
        seconds >= EMERGENCY_AFTER_SEC || (cabinTempC != null && cabinTempC >= DANGER_CABIN_C) -> 3
        seconds >= FAMILY_ALERT_AFTER_SEC || (cabinTempC != null && cabinTempC >= HOT_CABIN_C) -> 2
        else -> 1
    }

    fun assess(
        rider: Rider,
        obs: RearObservation,
        vehicle: VehicleSnapshot,
        leftBehindSeconds: Int = 0,
    ): Assessment {
        val who = Korean.subject(rider.name)
        val temp = vehicle.cabinTempC

        if (!obs.occupantPresent) {
            return Assessment(CareState.COMFORTABLE, Severity.INFO, "뒷좌석이 비어 있어요", "")
        }

        // 1. 하차 후 방치: 가장 먼저 본다.
        if (isLeftBehind(obs, vehicle)) {
            return when (val stage = leftBehindStage(leftBehindSeconds, temp)) {
                1 -> Assessment(
                    CareState.LEFT_BEHIND, Severity.WARNING,
                    "$who 차에 남아 있어요",
                    "$who 아직 차 안에 있어요. 확인해 주세요.",
                    SuggestionKind.RETURN_TO_CAR, stage,
                )
                2 -> Assessment(
                    CareState.LEFT_BEHIND, Severity.WARNING,
                    "$who 차에 남아 있어요",
                    "응답이 없어 가족에게도 알렸어요. 지금 차로 돌아가 주세요.",
                    SuggestionKind.RETURN_TO_CAR, stage,
                )
                else -> Assessment(
                    CareState.LEFT_BEHIND, Severity.EMERGENCY,
                    "지금 바로 차로 돌아가세요",
                    "$who 차에 오래 남아 있어요. 지금 바로 돌아가거나 119에 도움을 요청하세요.",
                    SuggestionKind.RETURN_TO_CAR, stage,
                )
            }
        }

        // 2. 무반응
        if (!obs.responsive) {
            return Assessment(
                CareState.UNRESPONSIVE, Severity.EMERGENCY,
                "$who 반응이 없어요",
                "$who 반응이 없어요. 안전한 곳에 차를 세우고 확인해 주세요.",
                if (rider.type == RiderType.CHILD) SuggestionKind.EMERGENCY_ROOM else SuggestionKind.VET_24H,
            )
        }

        // 3. 주행 중 안전띠 풀림 (아이)
        if (rider.type == RiderType.CHILD && !obs.beltFastened && !vehicle.parked) {
            return Assessment(
                CareState.BELT_UNFASTENED, Severity.WARNING,
                "안전띠가 풀렸어요",
                "${rider.name}의 안전띠가 풀렸어요. 안전한 곳에 세우고 다시 매 주세요.",
                SuggestionKind.CHECK_BELT,
            )
        }

        // 4. 더위
        val distressed = obs.motion == MotionCue.PANTING || obs.sound == SoundCue.CRYING
        if (temp != null && temp >= HOT_CABIN_C && distressed) {
            return Assessment(
                CareState.HOT, Severity.NOTICE,
                "더워하는 것 같아요",
                "실내가 ${temp.roundToInt()}도예요. $who 더워하는 것 같아요.",
                SuggestionKind.COOL_CABIN,
            )
        }

        // 5. 멀미 의심
        val motionSick = when (rider.type) {
            RiderType.PET -> obs.motion == MotionCue.PANTING && obs.sound == SoundCue.WHINING
            RiderType.CHILD -> obs.motion == MotionCue.RESTLESS && obs.sound == SoundCue.CRYING
        }
        if (motionSick) {
            return Assessment(
                CareState.MOTION_SICK, Severity.NOTICE,
                "멀미하는 것 같아요",
                "$who 멀미하는 것 같아요. 잠깐 쉬어 갈까요?",
                if (rider.type == RiderType.PET) SuggestionKind.PET_WALK else SuggestionKind.REST_STOP,
            )
        }

        // 6. 잠듦
        if (obs.motion == MotionCue.ASLEEP) {
            return Assessment(CareState.ASLEEP, Severity.INFO, "잠들었어요", "$who 잠들었어요.")
        }

        // 7. 그 밖의 불편 신호
        if (obs.sound == SoundCue.CRYING) {
            return Assessment(CareState.RESTLESS, Severity.NOTICE, "울고 있어요", "$who 울고 있어요.")
        }
        if (obs.sound == SoundCue.WHINING || obs.sound == SoundCue.BARKING) {
            return Assessment(CareState.RESTLESS, Severity.NOTICE, "불안해하는 것 같아요", "$who 불안해하는 것 같아요.")
        }
        if (obs.motion == MotionCue.RESTLESS || obs.motion == MotionCue.PANTING) {
            return Assessment(CareState.RESTLESS, Severity.INFO, "지루해하는 것 같아요", "$who 지루해하는 것 같아요.")
        }

        return Assessment(CareState.COMFORTABLE, Severity.INFO, "편안해요", "$who 다시 편안해졌어요.")
    }
}
