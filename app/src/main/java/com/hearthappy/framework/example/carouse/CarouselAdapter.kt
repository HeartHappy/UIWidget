package com.hearthappy.framework.example.carouse

import com.hearthappy.basic.AbsSpecialAdapter
import com.hearthappy.basic.ext.loadUrl
import com.hearthappy.framework.databinding.FragmentCarouseBinding

class CarouselAdapter : AbsSpecialAdapter<FragmentCarouseBinding, String>() {

    override fun FragmentCarouseBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.loadUrl(data)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}