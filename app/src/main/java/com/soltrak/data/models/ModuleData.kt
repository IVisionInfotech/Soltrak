package com.soltrak.data.models

import com.google.gson.annotations.SerializedName

data class ModuleData(
    @SerializedName("Id") val id: String?,
    @SerializedName("SerialNo") val serialNo: String?,
    @SerializedName("ModuleNo") val moduleNo: String?,
    @SerializedName("ModType") val modType: String?,
    @SerializedName("PVMfgr") val pvMfgr: String?,
    @SerializedName("CellMfgr") val cellMfgr: String?,
    @SerializedName("IECLab") val iecLab: String?,
    @SerializedName("PVMfgMonYear") val pvMfgMonYear: String?,
    @SerializedName("CellMfgMonYear") val cellMfgMonYear: String?,
    @SerializedName("VOC") val voc: String?,
    @SerializedName("ISC") val isc: String?,
    @SerializedName("VMax") val vMax: String?,
    @SerializedName("IMax") val iMax: String?,
    @SerializedName("PMax") val pMax: String?,
    @SerializedName("FF") val ff: String?,
    @SerializedName("CapVoltage") val capVoltage: String?,
    @SerializedName("Insol") val insol: String?,
    @SerializedName("TempTest") val tempTest: String?,
    @SerializedName("TempCorr") val tempCorr: String?,
    @SerializedName("Cell_Eff") val cellEff: String?,
    @SerializedName("Module_Eff") val moduleEff: String?,
    @SerializedName("RSeries") val rSeries: String?,
    @SerializedName("RShunt") val rShunt: String?,
    @SerializedName("Status") val status: String?,
    @SerializedName("CreatedTS") val createdTS: String?,
    @SerializedName("ImportFlag") val importFlag: String?,
    @SerializedName("CreatedTSWT") val createdTSWT: String?,
    @SerializedName("UID") val uid: String?,
    val ivPoints: List<IVPoint> = emptyList()
)