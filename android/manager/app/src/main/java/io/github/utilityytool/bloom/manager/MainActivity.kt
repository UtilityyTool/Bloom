package io.github.utilityytool.bloom.manager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import io.github.utilityytool.bloom.manager.data.AppState
import io.github.utilityytool.bloom.manager.ui.BloomApp
import io.github.utilityytool.bloom.manager.ui.theme.BloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val state = AppState(applicationContext)
        setContent {
            BloomTheme(mode = state.themeMode, wallpaperColors = state.wallpaperColors) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BloomApp(state)
                }
            }
        }
    }
}
