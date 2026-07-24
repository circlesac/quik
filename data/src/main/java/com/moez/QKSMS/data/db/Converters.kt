package dev.octoshrimpy.quik.data.db

import androidx.room.TypeConverter

/**
 * Room type converters. Dependency-free (no moshi).
 *
 * List<String> is stored as a newline-joined String. Empty list round-trips
 * through the empty string.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        value?.joinToString("\n") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split("\n")
}
