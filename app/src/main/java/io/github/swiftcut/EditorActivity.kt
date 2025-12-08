package io.github.swiftcut

import android.os.Bundle
import android.util.Log
import android.content.Context
import android.content.Intent
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.swiftcut.ui.editor.EditorScreen
import io.github.swiftcut.utils.Project

class EditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableFullScreenImmersive()
        val project = intent.getSerializableExtra("project", Project::class.java)
        setContent {
            SwiftCutTheme {
               EditorScreen(project!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun enableFullScreenImmersive() {
        with(window) {
            WindowCompat.setDecorFitsSystemWindows(this, false)
            val insetsController = WindowInsetsControllerCompat(this, decorView)
            insetsController.apply {
                hide(WindowInsetsCompat.Type.systemBars())
                systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            attributes.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enableFullScreenImmersive()
    }

    companion object {
        fun start(context: Context, project: Project) {
            val intent = Intent(context, EditorActivity::class.java)
            intent.putExtra("project", project)
            context.startActivity(intent)
        }
    }
}

