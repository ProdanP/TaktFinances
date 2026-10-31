package com.prodan.taktfinances

import android.database.ContentObserver
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.prodan.taktfinances.theme.ThemeContrast

private const val CONTRAST_LEVEL = "contrast_level"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val contrast = rememberThemeContrast()
            App(contrast = contrast)
        }
    }
}

@Composable
private fun rememberThemeContrast(): ThemeContrast {
    val context = LocalContext.current
    var contrast by remember { mutableStateOf(context.readThemeContrast()) }
    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                contrast = context.readThemeContrast()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(CONTRAST_LEVEL),
                false,
                observer,
            )
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                context.contentResolver.unregisterContentObserver(observer)
            }
        }
    }
    return contrast
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
