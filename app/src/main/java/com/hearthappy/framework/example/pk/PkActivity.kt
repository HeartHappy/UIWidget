package com.hearthappy.framework.example.pk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hearthappy.framework.databinding.ActivityPkBinding
import com.hearthappy.uiwidget.utils.dp2px
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

            indicatorView.setIndicatorCount(10)
            indicatorView.setIndicatorWidth(4f.dp2px())
            indicatorView.setIndicatorHeight(4f.dp2px())
            indicatorView.setIndicatorRadius(2f.dp2px())
            indicatorView.setSelectedIndicatorRadius(2f.dp2px())
            indicatorView.setIndicatorSpacing(10f.dp2px())
            indicatorView.setSelectedIndicatorWidth(22f.dp2px())
            indicatorView.setSelectedIndicatorHeight(4f.dp2px())
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

            lifecycleScope.launch{
                var count=0
                repeat(Int.MAX_VALUE){
                    delay(1000)
                    withContext(Dispatchers.Main){
                        indicatorView.setSelectedIndex(count%10)
                    }
                    count++
                }
            }
        }
    }
}