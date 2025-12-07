package io.github.swiftcut.services

import android.app.Service
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class LoggerService : Service() {

    companion object {
        private const val MAX_LOG_SIZE = 4L * 1024L * 1024L
        private const val LOG_TAG = "swiftcut"
    }

    private var logcat: Process? = null
    private var errorThread: Thread? = null
    private var outputThread: Thread? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try {
            Runtime.getRuntime().exec(arrayOf("logcat", "-c")).waitFor()
            logcat = Runtime.getRuntime().exec(arrayOf("logcat"))

            val baseDir = externalMediaDirs.firstOrNull()?.absolutePath
                ?: filesDir.absolutePath

            val logDir = File("$baseDir/logs")
            if (!logDir.exists()) logDir.mkdirs()

            val lastFile = File(logDir, "last.txt")
            val currentFile = File(logDir, "current.txt")

            if (lastFile.exists()) lastFile.delete()
            if (currentFile.exists()) currentFile.renameTo(lastFile)

            val outputStream = FileOutputStream(currentFile)

            errorThread = startStreamCopyThread(logcat!!.errorStream, outputStream)
            outputThread = startStreamCopyThread(logcat!!.inputStream, outputStream)

            Log.i(LOG_TAG, "Started logger service")
            logDeviceInfo()

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Failed to start logger service: ${e.message}")
            stopSelf()
        }
    }
    
    private fun startStreamCopyThread(input: InputStream, output: FileOutputStream): Thread {
        val thread = Thread {
            try {
                val buffer = ByteArray(4096)
                var total = 0L

                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break

                    synchronized(output) {
                        if (total + read > MAX_LOG_SIZE) break
                        output.write(buffer, 0, read)
                        output.flush()
                    }

                    total += read
                }
            } catch (_: Exception) {
            }
        }

        thread.isDaemon = true
        thread.start()
        return thread
    }

    private fun logDeviceInfo() {
        Log.i(LOG_TAG, "----------------------")
        Log.i(LOG_TAG, "Android SDK: ${Build.VERSION.SDK_INT}")
        Log.i(LOG_TAG, "Device: ${Build.DEVICE}")
        Log.i(LOG_TAG, "Model: ${Build.MANUFACTURER} ${Build.MODEL}")
        Log.i(LOG_TAG, "ABIs: ${Build.SUPPORTED_ABIS.contentToString()}")

        try {
            val info: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            Log.i(LOG_TAG, "")
            Log.i(LOG_TAG, "Package: ${info.packageName}")
            Log.i(LOG_TAG, "Install location: ${info.installLocation}")
            Log.i(LOG_TAG, "App version: ${info.versionName} (${info.versionCode})")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error obtaining package info: $e")
        }

        Log.i(LOG_TAG, "----------------------")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        try { Thread.sleep(1000) } catch (_: Exception) {}
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Log.i(LOG_TAG, "Logger service terminating")

        try { logcat?.destroy() } catch (_: Exception) {}
        try { errorThread?.interrupt() } catch (_: Exception) {}
        try { outputThread?.interrupt() } catch (_: Exception) {}

        super.onDestroy()
    }
}
