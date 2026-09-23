package com.damianrdev.save.ui.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.damianrdev.save.ui.theme.SaveTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickShareActivity : ComponentActivity() {

    private val viewModel: QuickShareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            SaveTheme {
                QuickShareScreen(
                    viewModel = viewModel,
                    onDismiss = {
                        finish()
                        overridePendingTransition(0, android.R.anim.fade_out)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            viewModel.processSharedText(sharedText)
        }
    }
}
