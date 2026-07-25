package com.matolimp.app

import com.matolimp.app.domain.AnswerChecker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerCheckerTest {

    private fun spec(type: String, vararg accepted: String, extra: String = ""): String {
        val acc = accepted.joinToString(",") { "\"$it\"" }
        return """{"type":"$type","accepted":[$acc]$extra}"""
    }

    @Test fun integer_exact() {
        val s = spec("INTEGER", "5050")
        assertTrue(AnswerChecker.check(s, "5050"))
        assertTrue(AnswerChecker.check(s, " 5050 "))
        assertFalse(AnswerChecker.check(s, "5051"))
    }

    @Test fun rational_equivalent_forms() {
        val s = spec("RATIONAL", "9/10")
        assertTrue(AnswerChecker.check(s, "9/10"))
        assertTrue(AnswerChecker.check(s, "18/20"))   // сокращается
        assertTrue(AnswerChecker.check(s, "0.9"))      // десятичное == дробь
        assertTrue(AnswerChecker.check(s, "0,9"))      // запятая
        assertFalse(AnswerChecker.check(s, "1/10"))
    }

    @Test fun decimal_tolerance() {
        val s = spec("DECIMAL", "0.5", extra = ",\"tolerance\":1e-6")
        assertTrue(AnswerChecker.check(s, "0.5"))
        assertTrue(AnswerChecker.check(s, "1/2"))
        assertFalse(AnswerChecker.check(s, "0.6"))
    }

    @Test fun set_unordered() {
        val s = spec("SET", "2,3,5", extra = ",\"orderMatters\":false")
        assertTrue(AnswerChecker.check(s, "5,3,2"))
        assertTrue(AnswerChecker.check(s, "2 3 5"))
        assertFalse(AnswerChecker.check(s, "2,3"))
    }

    @Test fun empty_input_is_wrong() {
        assertFalse(AnswerChecker.check(spec("INTEGER", "1"), ""))
        assertFalse(AnswerChecker.check(null, "1"))
    }
}
