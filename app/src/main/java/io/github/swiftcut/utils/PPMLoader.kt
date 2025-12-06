package io.github.swiftcut.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileInputStream

object PPMLoader {

    fun loadPPM(file: File?): Bitmap? {
        if (file == null) return null
        val bytes = file.readBytes()
        var index = 0

        fun readLine(): String {
            val sb = StringBuilder()
            while (index < bytes.size && bytes[index] != '\n'.code.toByte()) {
                sb.append(bytes[index].toInt().toChar())
                index++
            }
            index++
            return sb.toString()
        }

        val magic = readLine().trim()
        if (magic != "P6") return null

        var dims = readLine()
        while (dims.startsWith("#")) dims = readLine()

        val (w, h) = dims.trim().split(" ").map { it.toInt() }

        val maxVal = readLine().trim().toInt()

        val pixelCount = w * h * 3
        val pixelStart = index

        val pixelBytes = bytes.copyOfRange(pixelStart, pixelStart + pixelCount)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        var p = 0
        for (y in 0 until h) {
            for (x in 0 until w) {
                val r = pixelBytes[p++].toInt() and 0xFF
                val g = pixelBytes[p++].toInt() and 0xFF
                val b = pixelBytes[p++].toInt() and 0xFF
                bitmap.setPixel(x, y, Color.rgb(r, g, b))
            }
        }

        return bitmap
    }
}
