package io.github.swiftcut

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import io.github.swiftcut.ui.editor.EditorScreen

class EditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            SwiftCutTheme {
               EditorScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
