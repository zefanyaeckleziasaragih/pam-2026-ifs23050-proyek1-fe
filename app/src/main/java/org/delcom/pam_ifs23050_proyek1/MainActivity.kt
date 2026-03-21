package org.delcom.pam_ifs23050_proyek1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import org.delcom.pam_ifs23050_proyek1.ui.WatchListApp
import org.delcom.pam_ifs23050_proyek1.ui.theme.WatchListTheme
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.AuthViewModel
import org.delcom.pam_ifs23050_proyek1.ui.viewmodels.MovieViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val movieViewModel: MovieViewModel by viewModels()
    private val authViewModel: AuthViewModel   by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        authViewModel.loadTokenFromPreferences()
        setContent {
            WatchListTheme {
                WatchListApp(
                    movieViewModel = movieViewModel,
                    authViewModel  = authViewModel
                )
            }
        }
    }
}
