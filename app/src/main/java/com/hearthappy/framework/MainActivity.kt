package com.hearthappy.framework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.framework.databinding.ActivityMainBinding
import com.hearthappy.framework.example.calendar.CalendarActivity
import com.hearthappy.framework.example.image.RoundImageActivity
import com.hearthappy.framework.example.ninegrid.NineGridActivity
import com.hearthappy.framework.example.numberroll.NumberRollActivity
import com.hearthappy.framework.example.pk.PkActivity
import com.hearthappy.framework.example.ripple.WaterRippleActivity
import com.hearthappy.framework.example.turntable.TurntableActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding.apply {
            val exampleAdapter = ExampleAdapter(this@MainActivity)
            rvExampleList.adapter = exampleAdapter
            rvExampleList.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            exampleAdapter.initData(getExampleData())
        }
    }

    private fun getExampleData(): MutableList<ExampleBean> {
        return mutableListOf<ExampleBean>().apply {
            add(ExampleBean(TurntableActivity::class.java, "Turntable"))
            add(ExampleBean(CalendarActivity::class.java, "Calendar"))
            add(ExampleBean(PkActivity::class.java, "PK"))
            add(ExampleBean(NumberRollActivity::class.java, "NumberRoll"))
            add(ExampleBean(NineGridActivity::class.java, "NineGrid"))
            add(ExampleBean(WaterRippleActivity::class.java, "WaterRipple"))
            add(ExampleBean(RoundImageActivity::class.java, "RoundImage"))
        }
    }
}