package com.dolabom.app.pleos

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.DoorArea
import ai.pleos.playground.vehicle.constant.area.DoorValue
import ai.pleos.playground.vehicle.listener.DoorPositionListener
import android.content.Context
import android.util.Log
import com.dolabom.app.core.VehicleSnapshot
import com.dolabom.app.data.SimulatedVehicleDataSource
import com.dolabom.app.data.VehicleDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Pleos Vehicle SDK로 뒷문 열림 상태를 읽는다.
 *
 * 지금 SDK에서 받는 값은 뒷문 상태뿐이다. 주차 여부, 운전자 탑승, 실내 온도는
 * 아직 연결하지 않아서 [simulated]의 값을 그대로 쓴다.
 * 호출 방식은 Pleos 공식 샘플(pleos-sample-vehicle-app)의 Door Position 예제를 따랐다.
 */
class PleosVehicleDataSource(
    context: Context,
    private val simulated: SimulatedVehicleDataSource,
) : VehicleDataSource {

    override val label: String = "Pleos Vehicle SDK(뒷문) + 가짜 데이터"

    private val vehicle = Vehicle(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** 뒷문 영역 이름 → 열림 여부. 값을 한 번도 못 받았으면 비어 있다. */
    private val rearDoors = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val _status = MutableStateFlow("연결 전")
    override val status: StateFlow<String> = _status.asStateFlow()

    override val snapshot: StateFlow<VehicleSnapshot> =
        combine(simulated.snapshot, rearDoors) { base, doors ->
            if (doors.isEmpty()) base else base.copy(rearDoorOpen = doors.values.any { it })
        }.stateIn(scope, SharingStarted.Eagerly, simulated.snapshot.value)

    private val subscriptions = mutableListOf<Pair<DoorArea, DoorPositionListener>>()
    private var initialized = false

    override fun start() {
        if (initialized) return
        try {
            vehicle.initialize()
            initialized = true
            _status.value = "연결됨 · 뒷문 상태 대기 중"
        } catch (e: Exception) {
            report("Vehicle SDK 연결", e)
            return
        }
        for (code in REAR_DOOR_AREAS) watchDoor(code)
    }

    private fun watchDoor(code: String) {
        // 차종에 따라 없는 영역일 수 있어 이름으로 찾고, 없으면 건너뛴다.
        val area = try {
            DoorArea(DoorValue.valueOf(code))
        } catch (e: Exception) {
            Log.w(TAG, "지원하지 않는 문 영역: $code", e)
            return
        }
        val listener = object : DoorPositionListener {
            override fun onPositionUpdated(pos: Int?) {
                onDoorPosition(code, pos)
            }

            override fun onFailed(e: Exception) {
                report("뒷문 상태 알림", e)
            }
        }
        try {
            val door = vehicle.getDoor()
            door.getCurrentDoorPosition(
                area = area,
                onComplete = { pos -> onDoorPosition(code, pos) },
                onFailed = { e -> report("뒷문 상태 읽기", e) },
            )
            door.registerDoorPosition(area, listener)
            subscriptions += area to listener
        } catch (e: Exception) {
            report("뒷문 상태 구독", e)
        }
    }

    private fun onDoorPosition(code: String, position: Int?) {
        if (position == null) return
        // 공식 샘플 기준으로 0은 닫힘, 그보다 크면 열림이다.
        rearDoors.update { it + (code to (position > 0)) }
        val anyOpen = rearDoors.value.values.any { it }
        _status.value = "연결됨 · 뒷문 " + if (anyOpen) "열림" else "닫힘"
    }

    private fun report(what: String, e: Exception) {
        Log.w(TAG, "$what 실패", e)
        _status.value = "$what 실패: ${e.message ?: e.javaClass.simpleName}"
    }

    override fun stop() {
        if (initialized) {
            try {
                val door = vehicle.getDoor()
                for ((area, listener) in subscriptions) door.unregisterDoorPosition(area, listener)
                subscriptions.clear()
                vehicle.release()
            } catch (e: Exception) {
                Log.w(TAG, "Vehicle SDK 해제 실패", e)
            }
            initialized = false
        }
        scope.cancel()
    }

    private companion object {
        const val TAG = "PleosVehicle"
        val REAR_DOOR_AREAS = listOf("ROW_2_LEFT", "ROW_2_RIGHT")
    }
}
