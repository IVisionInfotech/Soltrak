package com.soltrak.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val id: String,
    val serialNo: String?,
    val moduleNo: String?,
    val modType: String?,
    val pvMfgr: String?,
    val cellMfgr: String?,
    val iecLab: String?,
    val pvMfgMonYear: String?,
    val cellMfgMonYear: String?,
    val voc: String?,
    val isc: String?,
    val vMax: String?,
    val iMax: String?,
    val pMax: String?,
    val ff: String?,
    val capVoltage: String?,
    val insol: String?,
    val tempTest: String?,
    val tempCorr: String?,
    val cellEff: String?,
    val moduleEff: String?,
    val rSeries: String?,
    val rShunt: String?,
    val status: String?,
    val createdTS: String?,
    val importFlag: String?,
    val createdTSWT: String?,
    val uid: String?,
    val ivPointsJson: String
)