package com.soltrak.data.models

data class IVPoint(
    val index: Int,
    val i: String,
    val v: String
) {

    val l: Double = try {
        val iDouble = i.toDouble()
        val vDouble = v.toDouble()
        iDouble * vDouble
    } catch (e: NumberFormatException) {
        System.err.println("NumberFormatException in IVPoint calculation: i='$i', v='$v' - ${e.message}")
        0.0
    }
}