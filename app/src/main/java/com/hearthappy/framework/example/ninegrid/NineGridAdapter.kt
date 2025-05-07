package com.hearthappy.framework.example.ninegrid

import android.content.Context
import android.widget.Toast
import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.ext.loadUrl
import com.hearthappy.framework.databinding.ItemNineBinding

class NineGridAdapter(val context: Context) : AbsSpecialAdapter<ItemNineBinding, List<String>>() {

    override fun ItemNineBinding.bindViewHolder(data: List<String>, position: Int) {
        tvTitle.text = "position:$position"
        (ngv initialize data).onBindView { r, i, d, p ->
            i.loadUrl(d)
            r.setOnClickListener {
                Toast.makeText(context, "position:$p", Toast.LENGTH_SHORT).show()
            }
        }
    }
}