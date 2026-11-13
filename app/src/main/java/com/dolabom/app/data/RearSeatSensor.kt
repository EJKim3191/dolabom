package com.dolabom.app.data

import com.dolabom.app.core.RearObservation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** 뒷좌석 인식 결과를 공급한다. 실제 영상과 소리 인식이 붙으면 이 인터페이스를 구현한다. */
interface RearSeatSensor {
    val observation: StateFlow<RearObservation>
}

/** 시연 패널이 값을 넣는 가짜 센서. */
class SimulatedRearSeatSensor : RearSeatSensor {

    private val _observation = MutableStateFlow(RearObservation())
    override val observation: StateFlow<RearObservation> = _observation.asStateFlow()

    fun set(value: RearObservation) {
        _observation.value = value
    }

    fun update(transform: (RearObservation) -> RearObservation) {
        _observation.update(transform)
    }

    fun reset() {
        _observation.value = RearObservation()
    }
}
