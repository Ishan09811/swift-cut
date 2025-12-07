package io.github.swiftcut

import android.app.Application
import android.content.Context
import android.content.Intent

class PandroidApplication : Application() {
    override fun onCreate() {
        NativeLib.initialize((this as Context).filesDir.absolutePath)
		    super.onCreate()
	  }
}
