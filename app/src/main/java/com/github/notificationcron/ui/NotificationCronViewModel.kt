package com.github.notificationcron.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.github.notificationcron.data.computeNextExecution
import com.github.notificationcron.data.local.AppDatabase
import com.github.notificationcron.data.local.NotificationCronDao
import com.github.notificationcron.data.model.NotificationCron
import com.github.notificationcron.data.removeAlarm
import com.github.notificationcron.data.scheduleAlarm
import com.github.notificationcron.data.scheduleNextAlarms
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationCronViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationCronDao: NotificationCronDao

    val allNotificationCrons: LiveData<List<NotificationCron>>

    init {
        val database = AppDatabase.getDatabase(application)
        notificationCronDao = database.notificationCronDao()
        allNotificationCrons = notificationCronDao.findAllOrderedAndLive()
    }

    fun create(context: Context, notificationCron: NotificationCron) = viewModelScope.launch(Dispatchers.IO) {
        computeNextExecution(notificationCron)
        val id = notificationCronDao.insert(notificationCron)
        val newNotificationCron = notificationCron.copy(id = id)
        scheduleAlarm(context, newNotificationCron)
    }

    fun update(context: Context, notificationCron: NotificationCron) = viewModelScope.launch(Dispatchers.IO) {
        removeAlarm(context, notificationCron.id)

        computeNextExecution(notificationCron)
        notificationCronDao.update(notificationCron)
        scheduleAlarm(context, notificationCron)
    }

    fun delete(context: Context, notificationCron: NotificationCron) = viewModelScope.launch(Dispatchers.IO) {
        removeAlarm(context, notificationCron.id)
        notificationCronDao.delete(notificationCron)
    }

    fun repairSchedule(context: Context) = viewModelScope.launch(Dispatchers.IO) {
        scheduleNextAlarms(context)
    }
}