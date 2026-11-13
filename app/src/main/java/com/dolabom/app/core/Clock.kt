package com.dolabom.app.core

object Clock {

    /** 초를 "분:초"로 바꾼다. 예: 75 → "01:15" */
    fun mmss(totalSeconds: Long): String {
        val safe = if (totalSeconds < 0) 0 else totalSeconds
        val minutes = safe / 60
        val seconds = safe % 60
        return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
    }
}
