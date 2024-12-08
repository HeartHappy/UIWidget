package com.hearthappy.uiwidget

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.hearthappy.uiwidget.databinding.ActivityMainBinding
import com.hearthappy.uiwidget.example.CalendarActivity
import com.hearthappy.uiwidget.example.TurntableActivity

class MainActivity : AppCompatActivity() {

    lateinit var viewBinding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        viewBinding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewBinding.apply {
            val exampleAdapter = ExampleAdapter(this@MainActivity)
            rvExampleList.adapter=exampleAdapter
            rvExampleList.layoutManager=LinearLayoutManager(this@MainActivity,LinearLayoutManager.VERTICAL,false)
            exampleAdapter.initData(getExampleData())
        }

    }

    private fun getExampleData(): MutableList<ExampleBean> {
       return mutableListOf<ExampleBean>().apply {
            add(ExampleBean(TurntableActivity::class.java,"Turntable"))
            add(ExampleBean(CalendarActivity::class.java,"Calendar"))
        }
    }
}