package com.hearthappy.framework.example.numberroll

import com.hearthappy.base.AbsBaseActivity
import com.hearthappy.framework.databinding.ActivityNumberRollBinding

class NumberRollActivity : AbsBaseActivity<ActivityNumberRollBinding>() {
    override fun initViewBinding(): ActivityNumberRollBinding {
        return ActivityNumberRollBinding.inflate(layoutInflater)
    }

    override fun ActivityNumberRollBinding.initViewModelListener() {
    }

    override fun ActivityNumberRollBinding.initView() {
        numberRollView.setValue(4500)

    }

    override fun ActivityNumberRollBinding.initListener() {
        btnChangeValue.setOnClickListener {
            numberRollView.rollToValue(numberRollView.getValue().plus(1500))
        }
    }

    override fun ActivityNumberRollBinding.initData() {
    }
}