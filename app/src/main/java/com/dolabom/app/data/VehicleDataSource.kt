package com.dolabom.app.data

import com.dolabom.app.core.VehicleSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 차량 신호를 공급한다. 가짜 데이터와 Pleos Vehicle SDK 구현이 같은 모양을 쓴다. */
interface VehicleDataSource {
    /** 화면에 보여줄 데이터 출처 이름 */
    val label: String

    val snapshot: StateFlow<VehicleSnapshot>

    /** 연결 상태 한 줄 */
    val status: StateFlow<String>

    fun start() {}

    fun stop() {}
}

/** 시연 패널이 값을 바꾸는 가짜 차량. SDK가 아직 주지 않는 신호의 기본값으로도 쓰인다. */
class SimulatedVehicleDataSource : VehicleDataSource {

    override val label: String = "가짜 데이터"

    private val _snapshot = MutableStateFlow(DEFAULT)
    override val snapshot: StateFlow<VehicleSnapshot> = _snapshot.asStateFlow()

    private val _status = MutableStateFlow("시연 패널로 조작")
    override val status: StateFlow<String> = _status.asStateFlow()

    fun update(transform: (VehicleSnapshot) -> VehicleSnapshot) {
        _snapshot.update(transform)
    }

    fun reset() {
        _snapshot.value = DEFAULT
    }

    private companion object {
        val DEFAULT = VehicleSnapshot(parked = false, driverPresent = true, rearDoorOpen = false, cabinTempC = 23f)
    }
}
