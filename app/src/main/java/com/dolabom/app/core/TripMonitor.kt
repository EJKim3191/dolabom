package com.dolabom.app.core

/**
 * 주행 한 번 동안의 흐름을 관리한다.
 * 방치 시간 계산, 음성 안내 시점, 알림 화면을 띄울지 여부를 한곳에서 정한다.
 */
class TripMonitor(private val policy: AnnouncementPolicy = AnnouncementPolicy()) {

    data class Update(
        val assessment: Assessment,
        /** 방치 상태가 이어진 시간(초). 방치가 아니면 0 */
        val leftBehindSeconds: Int,
        /** 지금 소리 내어 읽을 문장. 없으면 null */
        val spoken: String?,
        /** 알림 화면을 띄워야 하는지 */
        val showAlert: Boolean,
        /** 직전과 다른 상태인지. 기록을 남길 때 쓴다 */
        val changed: Boolean,
    )

    private var leftBehindSinceSec: Long? = null
    private var lastKey: String? = null
    private var dismissedKey: String? = null

    fun update(rider: Rider, obs: RearObservation, vehicle: VehicleSnapshot, nowSec: Long): Update {
        val since = if (CareEngine.isLeftBehind(obs, vehicle)) leftBehindSinceSec ?: nowSec else null
        leftBehindSinceSec = since
        val seconds = if (since == null) 0 else (nowSec - since).coerceIn(0L, MAX_SECONDS).toInt()

        val assessment = CareEngine.assess(rider, obs, vehicle, seconds)
        val changed = assessment.key != lastKey
        lastKey = assessment.key

        // 닫아 둔 알림은 같은 상태가 이어지는 동안만 닫힌 채로 둔다.
        // 방치 단계가 올라가면 상태 키가 바뀌므로 다시 뜬다.
        if (dismissedKey != assessment.key) dismissedKey = null

        return Update(
            assessment = assessment,
            leftBehindSeconds = seconds,
            spoken = policy.next(assessment, nowSec),
            showAlert = assessment.needsAttention && dismissedKey == null,
            changed = changed,
        )
    }

    /** 운전자가 알림을 확인하고 닫았다. 같은 상태가 이어지는 동안은 다시 띄우지 않는다. */
    fun dismiss(assessment: Assessment) {
        dismissedKey = assessment.key
    }

    fun reset() {
        leftBehindSinceSec = null
        lastKey = null
        dismissedKey = null
        policy.reset()
    }

    private companion object {
        const val MAX_SECONDS = 24L * 60 * 60
    }
}
