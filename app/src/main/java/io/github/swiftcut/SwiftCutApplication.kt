package io.github.swiftcut

import android.app.Application
import android.content.Context
import android.content.Intent
import io.github.swiftcut.services.LoggerService

class SwiftCutApplication : Application() {
    override fun onCreate() {
		super.onCreate()
        NativeLib.initialize((this as Context).filesDir.absolutePath)
		startService(Intent(this, LoggerService::class.java))
	}
}
