package com.catalogoapp.consultoras

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.catalogoapp.consultoras.navigation.CatalogoAppNavHost
import com.catalogoapp.consultoras.ui.theme.CatalogoAppTheme
import com.catalogoapp.consultoras.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window

                    window.statusBarColor = android.graphics.Color.WHITE

                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true

                    window.navigationBarColor = android.graphics.Color.parseColor("#121212")

                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
                }
            }

            CatalogoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CatalogoAppNavHost(authViewModel = authViewModel)
                }
            }
        }
    }
}