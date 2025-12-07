package io.github.swiftcut

object NativeLib {
    init {
        System.loadLibrary("swiftcut_backend")
    }

    external fun init(rootPath: String): Int
    external fun extractThumbnails(videoPath: String, outDir: String): Int
}
