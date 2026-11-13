package com.dolabom.app.di

import android.content.Context
import com.dolabom.app.data.SimulatedVehicleDataSource
import com.dolabom.app.data.VehicleDataSource
import com.dolabom.app.pleos.PleosVehicleDataSource

/** pleos 빌드: Pleos Vehicle SDK가 주는 신호를 쓰고, 나머지는 가짜 데이터로 채운다. */
object VehicleSourceFactory {

    fun create(context: Context, simulated: SimulatedVehicleDataSource): VehicleDataSource =
        PleosVehicleDataSource(context, simulated)
}
