package com.dolabom.app.core

/** 이름 뒤에 붙는 조사를 받침에 맞게 고른다. */
object Korean {

    fun hasFinalConsonant(word: String): Boolean {
        val last = word.trim().lastOrNull() ?: return false
        if (last in '가'..'힣') return (last.code - 0xAC00) % 28 != 0
        // 한글이 아닌 글자는 받침이 없는 것으로 본다.
        return false
    }

    /** "콩이" → "콩이가", "밤" → "밤이" */
    fun subject(word: String): String = word + if (hasFinalConsonant(word)) "이" else "가"

    /** "콩이" → "콩이를", "쉼터" → "쉼터를", "병원" → "병원을" */
    fun objectOf(word: String): String = word + if (hasFinalConsonant(word)) "을" else "를"
}
