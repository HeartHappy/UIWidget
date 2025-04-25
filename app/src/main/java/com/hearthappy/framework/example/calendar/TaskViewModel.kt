package com.hearthappy.framework.example.calendar

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.hearthappy.basic.tools.TimeTools
import com.hearthappy.framework.example.calendar.model.MultiTask
import java.util.Date

class TaskViewModel : ViewModel() {

    val projects = mutableListOf<MultiTask>().apply {
        add(MultiTask(TimeTools.strToMillis("2024-12-01 00:00:00"), TimeTools.strToMillis("2024-12-05 00:00:00"), "张三", completeRate = 20, firebaseId = "111", color = Color.RED))
        add(MultiTask(TimeTools.strToMillis("2024-12-02 13:30:00"), TimeTools.strToMillis("2024-12-07 00:00:00"), "李四", completeRate = 30, firebaseId = "222", color = Color.YELLOW))
        add(MultiTask(TimeTools.strToMillis("2024-12-05 00:00:00"), TimeTools.strToMillis("2024-12-08 00:00:00"), "王五", completeRate = 50, firebaseId = "333", color = Color.BLUE))
        add(MultiTask(TimeTools.strToMillis("2024-12-09 00:00:00"), TimeTools.strToMillis("2024-12-18 00:00:00"), "赵六", completeRate = 80, firebaseId = "444"))
        add(MultiTask(TimeTools.strToMillis("2024-12-18 00:00:00"), TimeTools.strToMillis("2024-12-28 00:00:00"), "陈七", completeRate = 100, firebaseId = "555"))
        add(MultiTask(TimeTools.strToMillis("2024-12-29 00:00:00"), TimeTools.strToMillis("2024-12-31 00:00:00"), "郑八", completeRate = 90, firebaseId = "666"))
        add(MultiTask(TimeTools.strToMillis("2025-01-01 00:00:00"), TimeTools.strToMillis("2025-01-05 00:00:00"), "刘九", completeRate = 95, firebaseId = "777"))
        add(MultiTask(TimeTools.strToMillis("2025-01-07 00:00:00"), TimeTools.strToMillis("2025-01-09 00:00:00"), "龙十", completeRate = 100, firebaseId = "888"))
        add(MultiTask(TimeTools.strToMillis("2025-01-11 00:00:00"), TimeTools.strToMillis("2025-01-14 00:00:00"), "魏一", completeRate = 100, firebaseId = "999"))
        add(MultiTask(TimeTools.strToMillis("2025-01-15 00:00:00"), TimeTools.strToMillis("2025-01-25 00:00:00"), "高二", completeRate = 100, firebaseId = "101010"))
    }

    fun get1WData(): MutableList<MultiTask> {
        val totalSize = 10
        val mutableListOf = mutableListOf<MultiTask>()
        for (i in 0 until totalSize) {
            projects.forEachIndexed { index, project ->
                mutableListOf.add(project.copy(name = project.name.plus("$i+$index")))
            }
        }
        return mutableListOf
    }

    fun timeInRange(today: Int, tasks: List<MultiTask>) {
        val tasksInRange = tasks.filter { task -> task.startTimeMillis <= today && task.endTimeMillis >= today }
        tasksInRange.forEach { task ->
            println("任务负责人: ${task.name}, 开始时间: ${Date(task.startTimeMillis)}, 结束时间: ${Date(task.endTimeMillis)}")
        }
    }
}