package io.github.swiftcut

object NativeLib {
    init {
        System.loadLibrary("swiftcut_backend")
    }

    external fun extractThumbnails(videoPath: String, outDir: String): Int
}
