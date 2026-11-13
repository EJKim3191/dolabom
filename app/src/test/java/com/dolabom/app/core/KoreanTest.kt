package com.dolabom.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KoreanTest {

    @Test
    fun detectsFinalConsonant() {
        assertTrue(Korean.hasFinalConsonant("밤"))
        assertTrue(Korean.hasFinalConsonant("서준"))
        assertFalse(Korean.hasFinalConsonant("콩이"))
        assertFalse(Korean.hasFinalConsonant("아이"))
        assertFalse(Korean.hasFinalConsonant(""))
        assertFalse(Korean.hasFinalConsonant("Max"))
    }

    @Test
    fun picksSubjectParticle() {
        assertEquals("콩이가", Korean.subject("콩이"))
        assertEquals("밤이", Korean.subject("밤"))
    }

    @Test
    fun picksObjectParticle() {
        assertEquals("졸음쉼터를", Korean.objectOf("졸음쉼터"))
        assertEquals("24시 동물병원을", Korean.objectOf("24시 동물병원"))
        assertEquals("응급실을", Korean.objectOf("응급실"))
    }

    @Test
    fun everySuggestionHasTextAndAcceptLabel() {
        for (kind in SuggestionKind.values()) {
            assertTrue(Suggestions.title(kind).isNotBlank())
            assertTrue(Suggestions.acceptLabel(kind).isNotBlank())
        }
    }
}
