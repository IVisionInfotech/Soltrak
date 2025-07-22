package com.soltrak.utils


object UtilityHelp {

    fun StringToByteArray(hex: String): ByteArray {
        require(!(hex == null || hex.length % 2 != 0)) { "" }

        val len = hex.length
        val result = ByteArray(len / 2)

        var i = 0
        while (i < len) {
            result[i / 2] = hex.substring(i, i + 2).toInt(16).toByte()
            i += 2
        }

        return result
    }

    fun ByteArrayToString(bytes: ByteArray?): String {
        if (bytes == null) {
            return ""
        }

        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02X", b))
        }

        return sb.toString()
    }
}
