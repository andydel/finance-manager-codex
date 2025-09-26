package com.andydel.financemanager.data.local

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromEpoch(epoch: Long?): Instant? = epoch?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun toEpoch(instant: Instant?): Long? = instant?.toEpochMilli()
}
