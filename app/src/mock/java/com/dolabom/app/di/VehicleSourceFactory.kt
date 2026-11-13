package com.dolabom.app.di

import android.content.Context
import com.dolabom.app.data.SimulatedVehicleDataSource
import com.dolabom.app.data.VehicleDataSource

/** mock 빌드: 차량 신호를 모두 가짜 데이터로 채운다. */
object VehicleSourceFactory {

    @Suppress("UNUSED_PARAMETER")
    fun create(context: Context, simulated: SimulatedVehicleDataSource): VehicleDataSource = simulated
}
