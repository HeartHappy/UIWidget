package com.hearthappy.framework.example.ripple

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.hearthappy.framework.R
import com.hearthappy.framework.databinding.ItemWaterRippleBinding
import com.hearthappy.uiwidget.ripple.WaterRippleView

class WaterRippleAdapter(val context: Context, private var urlsData: List<String>) : WaterRippleView.Adapter() {
    override fun onCreateViewHolder(parent: ViewGroup): WaterRippleView.ViewHolder {
        return ViewHolder(ItemWaterRippleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: WaterRippleView.ViewHolder, position: Int) {
        (holder as ViewHolder).viewBinding.apply {
            Glide.with(context).load(urlsData[position]).placeholder(R.mipmap.ic_launcher).into(rivImage)
            tvTitle.text = String.format("position:$position")
        }
    }

    override fun getItemCount(): Int {
        return urlsData.size
    }

    fun addData(urls2: MutableList<String>) {
        urlsData = urlsData.plus(urls2)
        notifyDataSetChanged()
    }


    inner class ViewHolder(val viewBinding: ItemWaterRippleBinding) : WaterRippleView.ViewHolder(viewBinding.root)

}