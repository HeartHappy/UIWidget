package com.hearthappy.base.ext

import android.widget.ImageView
import com.google.android.material.tabs.TabLayout
import com.hearthappy.base.model.TabItem

/**
 * Created Date: 2024/12/14
 * @author ChenRui
 * ClassDescription：监听扩展，用于简化代码 ，提高可读性
 */
fun TabLayout.addListener(onSelect: (TabLayout.Tab) -> Unit, onUnSelect: (TabLayout.Tab) -> Unit, onReselected: (TabLayout.Tab) -> Unit = {}) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            onSelect(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            onUnSelect(tab)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            onReselected(tab)
        }
    })
}


fun TabLayout.initTabs(items: MutableList<TabItem>) {
    items.forEachIndexed { index, tabItem ->
        val tabAt = getTabAt(index)
        val imageView = tabAt?.customView as? ImageView
        imageView?.setImageResource(if (index == 0) tabItem.selectRes else tabItem.unSelectRes)
    }
}

fun TabLayout.addCustomListener(items: MutableList<TabItem>, onSelect: (TabLayout.Tab) -> Unit, onUnSelect: (TabLayout.Tab) -> Unit = {}, onReselected: (TabLayout.Tab) -> Unit = {}) {
    addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            val imageView = tab.customView as ImageView
            imageView.setImageResource(items[tab.position].selectRes)
            onSelect(tab)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            val imageView = tab.customView as ImageView
            imageView.setImageResource(items[tab.position].unSelectRes)
            onUnSelect(tab)
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            onReselected(tab)
        }
    })
}
