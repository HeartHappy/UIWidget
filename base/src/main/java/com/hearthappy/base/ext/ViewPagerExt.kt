package com.social.lanyu.a_refactor.tools.core

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2


fun ViewPager.addListener(onSelect: (Int) -> Unit, onPageScrolled: (Int, Float, Int) -> Unit = { p, po, pop -> }, onPageScrollStateChanged: (Int) -> Unit = {}) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            onPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            onSelect(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            onPageScrollStateChanged(state)
        }
    })
}

fun ViewPager.addStateAdapter(fragmentManager: FragmentManager, count: Int, item: (Int) -> Fragment) {
    adapter = object : FragmentStatePagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return count
        }

        override fun getItem(position: Int): Fragment {
            return item(position)
        }
    }
}

fun ViewPager.addAdapter(fragmentManager: FragmentManager, count: Int, item: (Int) -> Fragment) {
    adapter = object : FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return count
        }

        override fun getItem(position: Int): Fragment {
            return item(position)
        }
    }
}

fun ViewPager2.addStateAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, count: Int, item: (Int) -> Fragment) {
    adapter = object : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = count
        override fun createFragment(position: Int): Fragment = item(position)
    }
}

fun ViewPager2.addListener(onSelect: (Int) -> Unit) {
    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onSelect(position)
        }
    })
}