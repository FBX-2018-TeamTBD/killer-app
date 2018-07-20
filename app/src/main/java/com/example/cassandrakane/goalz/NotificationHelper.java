package com.example.cassandrakane.goalz;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.example.cassandrakane.goalz.models.Goal;

public class NotificationHelper {

    Context context;
    AlarmManager alarmManager;

    public NotificationHelper(Context c) {
        context = c;
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setReminder(Goal goal) {
        // TODO implement
        createNotificationChannel(goal);

        PendingIntent pendingIntent = getPendingIntent(goal);
//        long futureInMillis = SystemClock.elapsedRealtime() + delay;
//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.HOUR_OF_DAY, 10);
//        calendar.set(Calendar.MINUTE, 32);
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
//                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    public void cancelReminder(Goal goal) {
        PendingIntent pendingIntent = getPendingIntent(goal);
        alarmManager.cancel(pendingIntent);
    }

    private void createNotificationChannel(Goal goal) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = goal.getTitle();
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(goal.getObjectId(), name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent getPendingIntent(Goal goal) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, goal.getIntId());
        intent.putExtra(NotificationReceiver.NOTIFICATION, createNotification(goal));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        return pendingIntent;
    }

    private Notification createNotification(Goal goal) {
        // TODO set icon as app icon
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, goal.getObjectId())
                .setSmallIcon(R.drawable.button_bg_round)
                .setContentTitle(goal.getTitle())
                .setContentText(goal.getDescription())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        return mBuilder.build();
    }
}
