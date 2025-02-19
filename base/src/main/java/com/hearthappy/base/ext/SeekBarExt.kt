package com.hearthappy.base.ext

import android.widget.SeekBar

fun SeekBar.addListener(onProgressChanged: (progress: Int) -> Unit, onStart: () -> Unit = {}, onStop: () -> Unit = {}) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged.invoke(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            onStart.invoke()
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            onStop.invoke()
        }
    })
}