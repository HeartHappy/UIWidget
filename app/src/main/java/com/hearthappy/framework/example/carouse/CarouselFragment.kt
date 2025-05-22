package com.hearthappy.framework.example.carouse

import android.os.Bundle
import com.hearthappy.basic.AbsBaseFragment
import com.hearthappy.framework.databinding.FragmentCarouseBinding

class CarouselFragment : AbsBaseFragment<FragmentCarouseBinding>() {
    override fun FragmentCarouseBinding.initView(savedInstanceState: Bundle?) {
        arguments?.getInt("index")?.let { //            tvTitle.text = "$it"
        }
    }

    override fun FragmentCarouseBinding.initData() {
    }

    override fun FragmentCarouseBinding.initListener() {
    }

    override fun FragmentCarouseBinding.initViewModelListener() {
    }

    companion object {
        fun newInstance(index: Int): CarouselFragment {
            return CarouselFragment().apply {
                arguments = Bundle().apply {
                    putInt("index", index)
                }
            }
        }
    }
}