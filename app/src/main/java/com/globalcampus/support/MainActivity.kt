package com.globalcampus.support

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.globalcampus.support.ui.navigation.NavGraph
import com.globalcampus.support.ui.theme.GlobalCampusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlobalCampusTheme {
                NavGraph()
            }
        }
    }
}