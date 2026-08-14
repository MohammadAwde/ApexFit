package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import kotlin.random.Random

object NotificationHelper {

    private const val CHANNEL_MOTIVATION = "channel_motivation"
    private const val CHANNEL_WORKOUT = "channel_workout_reminders"
    private const val CHANNEL_HYDRATION = "channel_hydration"
    private const val CHANNEL_ACHIEVEMENT = "channel_achievements"

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val motivationChannel = NotificationChannel(
                CHANNEL_MOTIVATION,
                "Daily Motivation & Streaks",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily inspirational push notifications, streak alerts, and fitness boosts"
                enableVibration(true)
            }

            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for scheduled workout routines"
            }

            val hydrationChannel = NotificationChannel(
                CHANNEL_HYDRATION,
                "Hydration Reminders",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Periodic hydration and water intake reminders"
            }

            val achievementChannel = NotificationChannel(
                CHANNEL_ACHIEVEMENT,
                "Milestones & Social Badges",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Milestone celebrations, PR alerts, and kudos"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(
                listOf(motivationChannel, workoutChannel, hydrationChannel, achievementChannel)
            )
        }
    }

    private val motivationalQuotes = listOf(
        "🔥 \"Discipline is choosing between what you want now and what you want most.\"",
        "⚡ \"Your only limit is you. Crush today's workout and keep your streak alive!\"",
        "🏆 \"Small daily improvements over time lead to stunning results.\"",
        "💪 \"The pain of discipline is far less than the pain of regret. Let's move!\"",
        "🎯 \"Champions train when nobody is watching. Unlock your peak potential today.\"",
        "🚀 \"14-Day Streak on fire! 20 minutes today keeps you ahead of 99% of people.\""
    )

    fun sendMotivationalNotification(context: Context, title: String? = null, customQuote: String? = null) {
        val quote = customQuote ?: motivationalQuotes.random()
        val notifTitle = title ?: "⚡ ApexFit Daily Motivation"

        sendNotification(
            context = context,
            channelId = CHANNEL_MOTIVATION,
            notificationId = 1001,
            title = notifTitle,
            message = quote,
            iconRes = android.R.drawable.ic_dialog_info
        )
    }

    fun sendWorkoutReminder(context: Context, routineTitle: String = "Apex Full-Body Power") {
        sendNotification(
            context = context,
            channelId = CHANNEL_WORKOUT,
            notificationId = 1002,
            title = "🏋️ Time to Train: $routineTitle",
            message = "Your scheduled workout is ready. Gear up and let's crush your fitness goals!",
            iconRes = android.R.drawable.ic_lock_idle_alarm
        )
    }

    fun sendHydrationReminder(context: Context, currentWaterMl: Int, targetMl: Int) {
        val remaining = (targetMl - currentWaterMl).coerceAtLeast(0)
        sendNotification(
            context = context,
            channelId = CHANNEL_HYDRATION,
            notificationId = 1003,
            title = "💧 Hydration Check-In",
            message = "You've logged $currentWaterMl ml today. Drink 250ml now to hit your ${targetMl}ml goal!",
            iconRes = android.R.drawable.ic_menu_rotate
        )
    }

    fun sendAchievementNotification(context: Context, badgeName: String, description: String) {
        sendNotification(
            context = context,
            channelId = CHANNEL_ACHIEVEMENT,
            notificationId = 1004 + Random.nextInt(100),
            title = "🏆 Milestone Unlocked: $badgeName",
            message = description,
            iconRes = android.R.drawable.ic_dialog_info
        )
    }

    private fun sendNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        iconRes: Int
    ) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(iconRes)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not yet granted on Android 13+, app handles gracefully
        }
    }
}
