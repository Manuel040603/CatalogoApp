package com.catalogoapp.consultoras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.catalogoapp.consultoras.navigation.CatalogoAppNavHost
import com.catalogoapp.consultoras.ui.theme.CatalogoAppTheme
import com.catalogoapp.consultoras.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.safeDrawingPadding

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatalogoAppTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    CatalogoAppNavHost(authViewModel = authViewModel)
                }
            }
        }
    }
}
