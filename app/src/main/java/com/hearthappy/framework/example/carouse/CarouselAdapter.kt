package com.hearthappy.framework.example.carouse

import com.hearthappy.basic.AbsBaseAdapter
import com.hearthappy.framework.databinding.FragmentCarouseBinding

class CarouselAdapter : AbsBaseAdapter<FragmentCarouseBinding, String>() {

    override fun FragmentCarouseBinding.bindViewHolder(data: String, position: Int) {
        tvTitle.text = position.toString()
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}