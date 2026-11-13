package com.dolabom.app.ui

import com.dolabom.app.core.Assessment
import com.dolabom.app.core.Place
import com.dolabom.app.core.RearObservation
import com.dolabom.app.core.Rider
import com.dolabom.app.core.RiderType
import com.dolabom.app.core.VehicleSnapshot

enum class Screen { SETUP, MONITOR, ALERT }

data class LogEntry(val clock: String, val text: String)

data class UiState(
    val started: Boolean = false,
    val riderType: RiderType = RiderType.CHILD,
    val riderName: String = defaultRiderName(RiderType.CHILD),
    /** 지켜보는 중인 동승자. 주행을 시작하면 정해진다. */
    val rider: Rider? = null,
    val observation: RearObservation = RearObservation(),
    val vehicle: VehicleSnapshot = VehicleSnapshot(),
    val assessment: Assessment? = null,
    val leftBehindSeconds: Int = 0,
    val alertVisible: Boolean = false,
    /** 받아들인 제안으로 요청한 경유지 */
    val waypoint: Place? = null,
    /** 최근 기록이 앞에 온다. */
    val log: List<LogEntry> = emptyList(),
    val voiceOn: Boolean = true,
    val vehicleSourceLabel: String = "",
    val vehicleStatus: String = "",
) {
    val screen: Screen
        get() = when {
            !started -> Screen.SETUP
            alertVisible && assessment != null -> Screen.ALERT
            else -> Screen.MONITOR
        }
}
