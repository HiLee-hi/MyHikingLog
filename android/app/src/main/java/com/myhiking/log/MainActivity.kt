package com.myhiking.log

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.myhiking.log.presentation.navigation.AppNavHost
import com.myhiking.log.presentation.ui.theme.MyHikingLogTheme
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d("APP_INIT", "MainActivity onCreate | coldStart=${savedInstanceState == null}")
        enableEdgeToEdge()
        // cold start deeplink
        handleAuthDeeplink(intent)
        setContent {
            MyHikingLogTheme {
                AppNavHost()
            }
        }
    }

    // deeplink while running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthDeeplink(intent)
    }

    private fun handleAuthDeeplink(intent: Intent?) {
        intent ?: return
        val uri = intent.data ?: return
        if (uri.scheme == "myhiking" && uri.host == "login-callback") {
            AppLogger.i("APP_INIT", "OAuth deeplink received: $uri")
            lifecycleScope.launch {
                supabaseClient.handleDeeplinks(intent)
            }
        }
    }
}
