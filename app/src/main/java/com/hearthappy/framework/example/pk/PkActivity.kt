package com.hearthappy.framework.example.pk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hearthappy.framework.databinding.ActivityPkBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PkActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPkBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        viewBinding.apply {
            var redCount = 0f
            var blueCount = 0f
            pkView.setValues(redCount, blueCount)

            lifecycleScope.launch {
                repeat(Int.MAX_VALUE) {
                    delay(1000)
                    pkView.post {
                        redCount += Random.nextInt(10)
                        blueCount += Random.nextInt(10)
                        pkView.setValues(redCount, blueCount)
                    }
                }
            }
        }
    }
}