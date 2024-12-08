package com.hearthappy.uiwidget

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hearthappy.uiwidget.base.KBaseAdapter
import com.hearthappy.uiwidget.databinding.ItemExampleBinding


class ExampleAdapter(private val context:Context):KBaseAdapter<ItemExampleBinding,ExampleBean>() {
    override fun initViewBinding(parent: ViewGroup, viewType: Int): ItemExampleBinding {
        return ItemExampleBinding.inflate(LayoutInflater.from(context),parent,false)
    }

    override fun ItemExampleBinding.bindViewHolder(data: ExampleBean, position: Int) {
        tvExampleTitle.text=data.title
        root.setOnClickListener {
            context.startActivity(Intent(context,data.clazz))
        }
    }
}