package com.dolabom.app.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dolabom.app.core.Clock
import com.dolabom.app.core.DemoScenario
import com.dolabom.app.core.DemoScenarios
import com.dolabom.app.core.Korean
import com.dolabom.app.core.Rider
import com.dolabom.app.core.RiderType
import com.dolabom.app.core.Suggestions
import com.dolabom.app.core.TripMonitor
import com.dolabom.app.data.Announcer
import com.dolabom.app.data.RecordingNavigator
import com.dolabom.app.data.SimulatedRearSeatSensor
import com.dolabom.app.data.SimulatedVehicleDataSource
import com.dolabom.app.data.TtsAnnouncer
import com.dolabom.app.data.VehicleDataSource
import com.dolabom.app.di.VehicleSourceFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 화면 상태를 만든다.
 * 뒷좌석 인식 값과 차량 신호를 받아 1초마다 [TripMonitor]에 넘기고, 결과를 화면과 음성으로 내보낸다.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val simulatedVehicle = SimulatedVehicleDataSource()
    private val vehicleSource: VehicleDataSource = VehicleSourceFactory.create(app, simulatedVehicle)
    private val sensor = SimulatedRearSeatSensor()
    private val navigator = RecordingNavigator()
    private val announcer: Announcer = TtsAnnouncer(app)
    private val monitor = TripMonitor()

    private val _state = MutableStateFlow(UiState(vehicleSourceLabel = vehicleSource.label))
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var tripJob: Job? = null
    private var tripStartSec = 0L

    /** 시연에서 "1분 건너뛰기"로 앞당긴 시간 */
    private var skippedSec = 0L
    private var applyingScenario = false

    init {
        vehicleSource.start()
        viewModelScope.launch {
            vehicleSource.status.collect { status ->
                _state.update { it.copy(vehicleStatus = status) }
            }
        }
    }

    // ── 동승자 선택 ─────────────────────────────────────────────

    fun selectRiderType(type: RiderType) {
        _state.update { current ->
            // 직접 적은 이름은 그대로 두고, 기본 이름일 때만 종류에 맞게 바꾼다.
            val typedByUser = current.riderName.isNotBlank() &&
                current.riderName != defaultRiderName(current.riderType)
            current.copy(
                riderType = type,
                riderName = if (typedByUser) current.riderName else defaultRiderName(type),
            )
        }
    }

    fun setRiderName(name: String) {
        _state.update { it.copy(riderName = name.take(MAX_NAME_LENGTH)) }
    }

    fun startTrip() {
        val current = _state.value
        val rider = Rider(
            type = current.riderType,
            name = current.riderName.trim().ifEmpty { defaultRiderName(current.riderType) },
        )
        sensor.reset()
        simulatedVehicle.reset()
        monitor.reset()
        skippedSec = 0L
        tripStartSec = nowSec()
        _state.update {
            it.copy(
                started = true,
                rider = rider,
                riderName = rider.name,
                assessment = null,
                alertVisible = false,
                leftBehindSeconds = 0,
                waypoint = null,
                log = emptyList(),
            )
        }
        addLog("${Korean.objectOf(rider.name)} 지켜보기 시작")

        tripJob?.cancel()
        tripJob = viewModelScope.launch {
            launch { sensor.observation.collect { refresh() } }
            launch { vehicleSource.snapshot.collect { refresh() } }
            while (true) {
                delay(TICK_MILLIS)
                refresh()
            }
        }
    }

    fun endTrip() {
        tripJob?.cancel()
        tripJob = null
        _state.update {
            it.copy(started = false, rider = null, assessment = null, alertVisible = false, leftBehindSeconds = 0)
        }
    }

    // ── 알림 화면 ──────────────────────────────────────────────

    /** 제안을 받아들인다. 장소가 있는 제안이면 경유지를 요청한다. */
    fun acceptSuggestion() {
        val assessment = _state.value.assessment ?: return
        val place = assessment.suggestion?.let { Suggestions.place(it) }
        if (place != null && navigator.addWaypoint(place)) {
            _state.update { it.copy(waypoint = place) }
            addLog("경유지 요청 · ${place.name}")
            speak("${Korean.objectOf(place.name)} 경유지로 추가할게요.")
        } else {
            addLog("확인 · ${assessment.headline}")
        }
        monitor.dismiss(assessment)
        refresh()
    }

    fun dismissAlert() {
        val assessment = _state.value.assessment ?: return
        monitor.dismiss(assessment)
        addLog("알림 닫음 · ${assessment.headline}")
        refresh()
    }

    // ── 시연 패널 ──────────────────────────────────────────────

    fun applyScenario(scenario: DemoScenario) {
        val rider = _state.value.rider ?: return
        // 차량 신호와 인식 값을 함께 바꾼 뒤 한 번만 판단해서, 중간 상태가 안내되지 않게 한다.
        applyingScenario = true
        try {
            simulatedVehicle.update { DemoScenarios.vehicle(scenario) }
            sensor.set(DemoScenarios.observation(scenario, rider.type))
        } finally {
            applyingScenario = false
        }
        refresh()
    }

    fun adjustCabinTemp(deltaC: Float) {
        simulatedVehicle.update { current ->
            val base = current.cabinTempC ?: DemoScenarios.NORMAL_CABIN_C
            current.copy(cabinTempC = (base + deltaC).coerceIn(MIN_CABIN_C, MAX_CABIN_C))
        }
    }

    fun toggleParked() {
        simulatedVehicle.update { it.copy(parked = !it.parked) }
    }

    fun toggleDriverPresent() {
        simulatedVehicle.update { it.copy(driverPresent = !it.driverPresent) }
    }

    fun toggleRearDoor() {
        simulatedVehicle.update { it.copy(rearDoorOpen = it.rearDoorOpen != true) }
    }

    /** 방치 단계를 기다리지 않고 보여 주기 위해 시간을 1분 앞당긴다. */
    fun skipMinute() {
        skippedSec += 60L
        addLog("시연 · 1분 건너뜀")
        refresh()
    }

    fun toggleVoice() {
        _state.update { it.copy(voiceOn = !it.voiceOn) }
    }

    // ── 내부 ──────────────────────────────────────────────────

    private fun refresh() {
        if (applyingScenario) return
        val rider = _state.value.rider ?: return
        val observation = sensor.observation.value
        val vehicle = vehicleSource.snapshot.value
        val update = monitor.update(rider, observation, vehicle, nowSec())

        val spoken = update.spoken
        if (spoken != null) {
            speak(spoken)
        } else if (update.changed) {
            addLog("상태 · ${update.assessment.headline}")
        }
        _state.update {
            it.copy(
                observation = observation,
                vehicle = vehicle,
                assessment = update.assessment,
                leftBehindSeconds = update.leftBehindSeconds,
                alertVisible = update.showAlert,
            )
        }
    }

    private fun speak(line: String) {
        if (_state.value.voiceOn) announcer.say(line)
        addLog("음성 · $line")
    }

    private fun addLog(text: String) {
        val entry = LogEntry(Clock.mmss(nowSec() - tripStartSec), text)
        _state.update { it.copy(log = (listOf(entry) + it.log).take(MAX_LOG_ENTRIES)) }
    }

    private fun nowSec(): Long = SystemClock.elapsedRealtime() / 1000L + skippedSec

    override fun onCleared() {
        tripJob?.cancel()
        vehicleSource.stop()
        announcer.shutdown()
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val MAX_NAME_LENGTH = 12
        const val MAX_LOG_ENTRIES = 30
        const val MIN_CABIN_C = 10f
        const val MAX_CABIN_C = 45f
    }
}
