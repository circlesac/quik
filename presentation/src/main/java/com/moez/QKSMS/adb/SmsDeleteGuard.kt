/*
 * Copyright (C) 2026
 *
 * This file is part of QUIK.
 *
 * QUIK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.octoshrimpy.quik.adb

import java.security.MessageDigest

internal data class SmsFingerprintRecord(
    val id: Long?,
    val address: String,
    val body: String,
    val date: Long?,
    val dateSent: Long?,
    val read: Long?,
    val status: Long?,
    val threadId: Long?,
    val type: Long?
)

internal object SmsDeleteGuard {
    private val fingerprintSelection = Regex("^fingerprint=([0-9a-fA-F]{64})$")

    fun parseFingerprint(selection: String?, selectionArgs: Array<out String>?): String {
        require(selectionArgs.isNullOrEmpty()) { "selection arguments are not supported" }
        return fingerprintSelection.matchEntire(selection ?: "")
            ?.groupValues
            ?.get(1)
            ?.lowercase()
            ?: throw IllegalArgumentException("delete requires one full SHA-256 fingerprint")
    }

    fun fingerprint(record: SmsFingerprintRecord): String {
        val canonical = buildString {
            append("{\"_id\":").appendJsonNumber(record.id)
            append(",\"address\":").appendJsonString(record.address)
            append(",\"body\":").appendJsonString(record.body)
            append(",\"date\":").appendJsonNumber(record.date)
            append(",\"date_sent\":").appendJsonNumber(record.dateSent)
            append(",\"read\":").appendJsonNumber(record.read)
            append(",\"status\":").appendJsonNumber(record.status)
            append(",\"thread_id\":").appendJsonNumber(record.threadId)
            append(",\"type\":").appendJsonNumber(record.type)
            append('}')
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
    }

    private fun StringBuilder.appendJsonNumber(value: Long?) {
        append(value ?: "null")
    }

    private fun StringBuilder.appendJsonString(value: String) {
        append('"')
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                '\b' -> append("\\b")
                '\u000c' -> append("\\f")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (character.code < 0x20) {
                    append("\\u")
                    append(character.code.toString(16).padStart(4, '0'))
                } else {
                    append(character)
                }
            }
        }
        append('"')
    }
}
