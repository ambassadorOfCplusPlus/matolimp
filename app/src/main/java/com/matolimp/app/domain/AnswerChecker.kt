package com.matolimp.app.domain

import com.matolimp.app.data.SeedAnswer
import kotlinx.serialization.json.Json
import kotlin.math.abs

/**
 * Каноническая проверка ответа для закрытых задач.
 * Сравнивает по смыслу, а не по строке: 1/2 == 0.5 == 0,5.
 */
object AnswerChecker {

    private val json = Json { ignoreUnknownKeys = true }

    fun check(specJson: String?, rawInput: String): Boolean {
        if (specJson.isNullOrBlank()) return false
        val spec = runCatching { json.decodeFromString<SeedAnswer>(specJson) }.getOrNull() ?: return false
        val input = rawInput.trim()
        if (input.isEmpty()) return false
        return spec.accepted.any { accepted -> equals(spec, accepted, input) }
    }

    private fun equals(spec: SeedAnswer, accepted: String, input: String): Boolean = when (spec.type.uppercase()) {
        "INTEGER" -> intOf(accepted) != null && intOf(accepted) == intOf(input)
        "RATIONAL" -> fractionOf(accepted)?.let { a -> fractionOf(input)?.let { b -> a == b } } ?: false
        "DECIMAL" -> numericValue(accepted)?.let { a -> numericValue(input)?.let { b -> abs(a - b) <= spec.tolerance } } ?: false
        "SET" -> setEquals(accepted, input, spec.orderMatters)
        "STRING" -> if (spec.caseSensitive) accepted.trim() == input.trim()
                    else accepted.trim().equals(input.trim(), ignoreCase = true)
        else -> accepted.trim().equals(input.trim(), ignoreCase = true)
    }

    private fun normalizeNumber(s: String): String =
        s.trim().replace(" ", "").replace(',', '.')

    private fun intOf(s: String): Long? = normalizeNumber(s).toLongOrNull()

    private fun decimalOf(s: String): Double? = normalizeNumber(s).toDoubleOrNull()

    /** Универсальное числовое значение: целое, десятичное или дробь a/b. */
    private fun numericValue(s: String): Double? {
        val t = normalizeNumber(s)
        if (t.contains('/')) {
            val f = fractionOf(t) ?: return null
            return f.first.toDouble() / f.second.toDouble()
        }
        return t.toDoubleOrNull()
    }

    /** Приводит "a/b" или целое/десятичное к несократимой дроби (num, den>0). */
    private fun fractionOf(s: String): Pair<Long, Long>? {
        val t = normalizeNumber(s)
        if (t.contains('/')) {
            val parts = t.split('/')
            if (parts.size != 2) return null
            val n = parts[0].toLongOrNull() ?: return null
            val d = parts[1].toLongOrNull() ?: return null
            if (d == 0L) return null
            return reduce(n, d)
        }
        // целое или десятичное
        t.toLongOrNull()?.let { return reduce(it, 1) }
        val dv = t.toDoubleOrNull() ?: return null
        // десятичное -> дробь через знаменатель 10^k (до 9 знаков)
        val decimals = t.substringAfter('.', "").length.coerceAtMost(9)
        val den = generateSequence(1L) { it * 10 }.elementAt(decimals)
        val num = Math.round(dv * den)
        return reduce(num, den)
    }

    private fun reduce(n: Long, d: Long): Pair<Long, Long> {
        var num = n
        var den = d
        if (den < 0) { num = -num; den = -den }
        val g = gcd(abs(num), abs(den)).coerceAtLeast(1)
        return num / g to den / g
    }

    private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

    private fun setEquals(accepted: String, input: String, orderMatters: Boolean): Boolean {
        val a = splitSet(accepted)
        val b = splitSet(input)
        if (a.size != b.size) return false
        return if (orderMatters) a == b else a.sorted() == b.sorted()
    }

    private fun splitSet(s: String): List<String> =
        s.split(',', ';', ' ')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { normalizeNumber(it) }
}
