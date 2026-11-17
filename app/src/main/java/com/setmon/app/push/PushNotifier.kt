package com.setmon.app.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.setmon.app.MainActivity
import com.setmon.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 서버는 data-only 메시지만 보내서 시스템이 알림을 대신 띄워 주지 않는다.
 * 앱이 꺼져 있어도 onMessageReceived 가 불리므로 여기서 직접 알림을 만든다.
 */
@Singleton
class PushNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    /** @param data FCM data (type, date, title, body) */
    fun show(data: Map<String, String>) {
        val type = data["type"]
        val date = data["date"]
        val target = PushTarget.of(type, date)
        if (target == null) {
            Log.d(PushTokenRegistrar.TAG, "모르는 알림이라 무시: $data")
            return
        }
        if (!canNotify()) {
            Log.w(PushTokenRegistrar.TAG, "알림 권한이 없어 표시하지 못함: $type")
            return
        }
        createChannels()

        val channelId = if (target is PushTarget.Capture) CHANNEL_CAPTURE else CHANNEL_VLOG_DONE
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(data["title"] ?: context.getString(R.string.push_default_title))
            .setContentText(data["body"].orEmpty())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(tapIntent(type!!, date))
            .build()

        // 같은 종류는 하나만 보이게 id 를 종류별로 고정한다 (새 알림이 이전 것을 바꾼다)
        val id = if (target is PushTarget.Capture) ID_CAPTURE else ID_VLOG_DONE
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            Log.w(PushTokenRegistrar.TAG, "알림 권한이 없어 표시하지 못함", e)
        }
    }

    private fun canNotify(): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** 이미 있으면 아무 일도 안 한다. 이름은 휴대폰 설정 > 앱 > 알림 에 보인다 */
    private fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_CAPTURE,
                context.getString(R.string.push_channel_capture),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_VLOG_DONE,
                context.getString(R.string.push_channel_vlog_done),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }

    /** 탭하면 MainActivity 를 열고(이미 떠 있으면 그대로 쓰고) type·date 를 넘긴다 */
    private fun tapIntent(type: String, date: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(PushTarget.EXTRA_TYPE, type)
            putExtra(PushTarget.EXTRA_DATE, date)
        }
        return PendingIntent.getActivity(
            context,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val CHANNEL_CAPTURE = "capture"
        const val CHANNEL_VLOG_DONE = "vlog_done"
        const val ID_CAPTURE = 1
        const val ID_VLOG_DONE = 2
    }
}
