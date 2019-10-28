package social.admire.admire

import android.graphics.Bitmap
import android.os.AsyncTask

object TasksController {

    val LOW_PRIORITY_TASKS = ArrayList<AsyncTask<String, Void, Bitmap>>()
    val MID_PRIORITY_TASKS = ArrayList<AsyncTask<String, Void, Bitmap>>()
    val HIGH_PRIORITY_TASKS = ArrayList<AsyncTask<String, Void, Bitmap>>()

    var is_task = false

    var last_tasks_group = ""

    fun addNewTask (priority: String, new_task: AsyncTask<String, Void, Bitmap>) {
        when(priority) {
            "HIGH_PRIORITY_TASKS" -> {
                HIGH_PRIORITY_TASKS.add(new_task)
            }
            "MID_PRIORITY_TASKS" -> {
                MID_PRIORITY_TASKS.add(new_task)
            }
            "LOW_PRIORITY_TASKS" -> {
                LOW_PRIORITY_TASKS.add(new_task)
            }
        }
        startTask()
    }

    fun sliceTask() {
        when(last_tasks_group) {
            "HIGH_PRIORITY_TASKS" -> {
                if(HIGH_PRIORITY_TASKS.size > 0)
                    HIGH_PRIORITY_TASKS.removeAt(0)
            }
            "MID_PRIORITY_TASKS" -> {
                if(MID_PRIORITY_TASKS.size > 0)
                    MID_PRIORITY_TASKS.removeAt(0)
            }
            "LOW_PRIORITY_TASKS" -> {
                if(LOW_PRIORITY_TASKS.size > 0)
                    LOW_PRIORITY_TASKS.removeAt(0)
            }
        }
        is_task = false
        startTask()
    }

    fun startTask() {
        if(!is_task) {
            if (HIGH_PRIORITY_TASKS.size > 0) {
                last_tasks_group = "HIGH_PRIORITY_TASKS"
                is_task = true
                HIGH_PRIORITY_TASKS.get(0).execute()
            } else if (MID_PRIORITY_TASKS.size > 0) {
                last_tasks_group = "MID_PRIORITY_TASKS"
                is_task = true
                MID_PRIORITY_TASKS.get(0).execute()
            } else if (LOW_PRIORITY_TASKS.size > 0) {
                last_tasks_group = "LOW_PRIORITY_TASKS"
                is_task = true
                LOW_PRIORITY_TASKS.get(0).execute()
            }
        }
    }

    fun clearTasks (which: String = "null") {
        when(which) {
            "HIGH_PRIORITY_TASKS" -> {
                HIGH_PRIORITY_TASKS.clear()
            }
            "MID_PRIORITY_TASKS" -> {
                MID_PRIORITY_TASKS.clear()
            }
            "LOW_PRIORITY_TASKS" -> {
                LOW_PRIORITY_TASKS.clear()
            }
            "null" -> {
                LOW_PRIORITY_TASKS.clear()
                MID_PRIORITY_TASKS.clear()
                HIGH_PRIORITY_TASKS.clear()
            }
        }
    }
}