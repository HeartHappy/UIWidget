package com.hearthappy.framework.example.calendar.model

import com.hearthappy.basic.tools.TimeTools.DAY_MILLIS


data class MultiTask(var startTimeMillis: Long = System.currentTimeMillis(), var endTimeMillis: Long = System.currentTimeMillis() + 7 * DAY_MILLIS, var name: String = "Task", var completeRate: Int = 0, var color: Int = 0, var taskOwners: MutableList<User> = mutableListOf(), var toDoList: MutableList<ToDo> = mutableListOf(), var firebaseId: String = "") {
    fun newRefTask(): MultiTask {
        val newTask = this.copy(toDoList = mutableListOf(), taskOwners = mutableListOf())
        this.toDoList.map { toDo ->
            newTask.toDoList.add(toDo)
        }
        this.taskOwners.map { user ->
            newTask.taskOwners.add(user)
        }
        return newTask
    }
}
