package com.example.pokedex_kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pokedex_kmp.data.initializePokedexDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        initializePokedexDatabase(applicationContext)
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}
