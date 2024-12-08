package com.hearthappy.uiwidget.example

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hearthappy.uiwidget.R
import com.hearthappy.uiwidget.databinding.ActivityTurntableBinding

class TurntableActivity : AppCompatActivity() {
    lateinit var viewBinding:ActivityTurntableBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding=ActivityTurntableBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewBinding.apply {
            turntableView.setOnClickListener {
                turntableView.startLottery()
            }
        }
    }
}