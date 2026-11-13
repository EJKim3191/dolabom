package com.dolabom.app.core

/**
 * 언제 소리 내어 알릴지 정한다.
 * 상태가 바뀔 때 한 번 말하고, 경고 이상은 일정 간격으로 다시 말한다.
 * 처음부터 편안한 상태였다면 굳이 말하지 않는다.
 */
class AnnouncementPolicy(private val repeatWarningEverySec: Int = 30) {

    private var lastKey: String? = null
    private var lastSeverity: Severity = Severity.INFO
    private var lastSpokenAtSec: Long = NEVER

    /** 지금 말할 문장을 돌려준다. 말할 것이 없으면 null. */
    fun next(assessment: Assessment, nowSec: Long): String? {
        val changed = assessment.key != lastKey
        val hadPrevious = lastKey != null
        val previousSeverity = lastSeverity

        val line: String? = when {
            changed && assessment.state == CareState.COMFORTABLE ->
                if (hadPrevious && previousSeverity >= Severity.NOTICE) assessment.spoken else null
            changed -> assessment.spoken
            assessment.severity >= Severity.WARNING &&
                lastSpokenAtSec != NEVER &&
                nowSec - lastSpokenAtSec >= repeatWarningEverySec -> assessment.spoken
            else -> null
        }

        if (changed) {
            lastKey = assessment.key
            lastSeverity = assessment.severity
        }
        val result = line?.takeIf { it.isNotBlank() }
        if (result != null) lastSpokenAtSec = nowSec
        return result
    }

    fun reset() {
        lastKey = null
        lastSeverity = Severity.INFO
        lastSpokenAtSec = NEVER
    }

    private companion object {
        const val NEVER = Long.MIN_VALUE
    }
}
