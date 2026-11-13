package com.dolabom.app.data

import com.dolabom.app.core.Place

/** 내비게이션에 경유지를 넣는다. NaviHelper SDK 연동 전까지는 요청만 기록한다. */
interface Navigator {
    /** 요청이 받아들여졌으면 true */
    fun addWaypoint(place: Place): Boolean
}

class RecordingNavigator : Navigator {

    private val _requested = mutableListOf<Place>()

    /** 지금까지 요청된 경유지 */
    val requested: List<Place>
        get() = _requested.toList()

    override fun addWaypoint(place: Place): Boolean {
        _requested += place
        return true
    }
}
