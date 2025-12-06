package io.github.swiftcut.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileInputStream

object PPMLoader {

    fun loadPPM(file: File): Bitmap? {
        val input = FileInputStream(file)

        val header = input.bufferedReader().readLine()
        if (header != "P6") return null

        var sizeLine = input.bufferedReader().readLine()
        while (sizeLine.startsWith("#"))
            sizeLine = input.bufferedReader().readLine()

        val (width, height) = sizeLine.split(" ").map { it.toInt() }

        val maxVal = input.bufferedReader().readLine().toInt()
        if (maxVal != 255) return null

        val pixelData = input.readBytes()
        input.close()

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        var idx = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val r = pixelData[idx].toInt() and 0xFF
                val g = pixelData[idx + 1].toInt() and 0xFF
                val b = pixelData[idx + 2].toInt() and 0xFF
                bmp.setPixel(x, y, (0xFF shl 24) or (r shl 16) or (g shl 8) or b)
                idx += 3
            }
        }
        return bmp
    }
}
