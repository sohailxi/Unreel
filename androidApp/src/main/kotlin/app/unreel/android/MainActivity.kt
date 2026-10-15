package app.unreel.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.unreel.android.service.UnreelForegroundService
import app.unreel.android.theme.UnreelTheme
import app.unreel.android.ui.UnreelApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        try {
            UnreelForegroundService.start(this)
        } catch (_: Throwable) {
            // Safe fallback if service start is restricted
        }

        setContent {
            UnreelTheme {
                UnreelApp()
            }
        }
    }
}
