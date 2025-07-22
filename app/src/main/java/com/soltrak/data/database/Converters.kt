package com.soltrak.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.soltrak.data.models.IVPoint

class Converters {
    @TypeConverter
    fun fromIVPointList(ivPoints: List<IVPoint>): String {
        return Gson().toJson(ivPoints)
    }

    @TypeConverter
    fun toIVPointList(ivPointsJson: String): List<IVPoint> {
        val type = object : TypeToken<List<IVPoint>>() {}.type
        return Gson().fromJson(ivPointsJson, type)
    }
}