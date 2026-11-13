package com.dolabom.app.ui

import com.dolabom.app.core.MotionCue
import com.dolabom.app.core.RiderType
import com.dolabom.app.core.Severity
import com.dolabom.app.core.SoundCue
import kotlin.math.roundToInt

internal fun riderTypeLabel(type: RiderType): String = when (type) {
    RiderType.CHILD -> "아이"
    RiderType.PET -> "반려동물"
}

/** 이름을 따로 적지 않았을 때 부르는 말 */
internal fun defaultRiderName(type: RiderType): String = when (type) {
    RiderType.CHILD -> "아이"
    RiderType.PET -> "강아지"
}

internal fun soundLabel(sound: SoundCue): String = when (sound) {
    SoundCue.QUIET -> "조용함"
    SoundCue.CRYING -> "울음"
    SoundCue.WHINING -> "낑낑거림"
    SoundCue.BARKING -> "짖음"
}

internal fun motionLabel(motion: MotionCue): String = when (motion) {
    MotionCue.STILL -> "차분함"
    MotionCue.ASLEEP -> "잠듦"
    MotionCue.RESTLESS -> "뒤척임"
    MotionCue.PANTING -> "헐떡임"
}

internal fun severityLabel(severity: Severity): String = when (severity) {
    Severity.INFO -> "안내"
    Severity.NOTICE -> "알림"
    Severity.WARNING -> "경고"
    Severity.EMERGENCY -> "긴급"
}

internal fun temperatureLabel(cabinTempC: Float?): String =
    if (cabinTempC == null) "알 수 없음" else "${cabinTempC.roundToInt()}℃"

internal fun doorLabel(rearDoorOpen: Boolean?): String = when (rearDoorOpen) {
    null -> "알 수 없음"
    true -> "열림"
    false -> "닫힘"
}
