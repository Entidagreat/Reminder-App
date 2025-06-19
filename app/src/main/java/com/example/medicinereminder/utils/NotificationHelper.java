package com.example.medicinereminder.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.medicinereminder.EducationalReminderActivity;
import com.example.medicinereminder.HomeActivity;
import com.example.medicinereminder.R;
import com.example.medicinereminder.receivers.EducationalReminderReceiver;

public class NotificationHelper {
    private static final String CHANNEL_ID = "medicine_reminder";
    private static final String CHANNEL_NAME = "Medicine Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for medication reminders";
    private static final int NOTIFICATION_ID = 1001;

    // Educational reminders channel
    private static final String EDU_CHANNEL_ID = "educational_reminder";
    private static final String EDU_CHANNEL_NAME = "Educational Reminders";
    private static final String EDU_CHANNEL_DESCRIPTION = "Notifications for educational reminders";
    private static final int EDU_NOTIFICATION_BASE_ID = 2000;

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create medicine reminder channel
            NotificationChannel medicineChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            medicineChannel.setDescription(CHANNEL_DESCRIPTION);
            medicineChannel.enableLights(true);
            medicineChannel.enableVibration(true);
            notificationManager.createNotificationChannel(medicineChannel);
            
            // Create educational reminder channel
            NotificationChannel eduChannel = new NotificationChannel(
                    EDU_CHANNEL_ID,
                    EDU_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            eduChannel.setDescription(EDU_CHANNEL_DESCRIPTION);
            eduChannel.enableLights(true);
            eduChannel.enableVibration(true);
            notificationManager.createNotificationChannel(eduChannel);
        }
    }

    public void showMedicationReminder(String medicationName, String dosage, long medicationId) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("medication_id", medicationId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) medicationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications) // Cần tạo icon này
                .setContentTitle(context.getString(R.string.medication_reminder))
                .setContentText("Time to take " + medicationName + " (" + dosage + ")")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("It's time to take your " + medicationName + " (" + dosage + "). Don't forget to mark it as taken!"));

        notificationManager.notify((int) medicationId, builder.build());
    }

    public void showRefillReminder(String medicationName, int pillsLeft) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Refill Reminder")
                .setContentText(medicationName + " is running low (" + pillsLeft + " pills left)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID + 1000, builder.build());
    }

    public void showEducationalReminder(String title, String message, long reminderId) {
        Intent intent = new Intent(context, EducationalReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("reminder_id", reminderId);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EDU_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(EDU_NOTIFICATION_BASE_ID + (int) reminderId, builder.build());
    }

    // Schedule an educational reminder notification
    public void scheduleEducationalReminderNotification(int reminderId, String title, String description, String type, long reminderTime) {
        Intent intent = new Intent(context, EducationalReminderReceiver.class);
        intent.putExtra("reminder_id", reminderId);
        intent.putExtra("reminder_title", title);
        intent.putExtra("reminder_description", description);
        intent.putExtra("reminder_type", type);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId + EDU_NOTIFICATION_BASE_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            }
        }
    }
    
    // Show educational reminder notification
    public void showEducationalReminderNotification(int reminderId, String title, String description, String type) {
        Intent intent = new Intent(context, EducationalReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("reminder_id", reminderId);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, EDU_CHANNEL_ID)
                .setSmallIcon(getNotificationIconForEducationalType(type))
                .setContentTitle(context.getString(R.string.educational_reminder_notification))
                .setContentText(title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(description))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        notificationManager.notify(reminderId + EDU_NOTIFICATION_BASE_ID, builder.build());
    }
    
    private int getNotificationIconForEducationalType(String type) {
        switch (type) {
            case "Study Session":
                return R.drawable.ic_study;
            case "Assignment Deadline":
                return R.drawable.ic_assignment;
            case "Exam Date":
                return R.drawable.ic_exam;
            case "Language Practice":
                return R.drawable.ic_language;
            default:
                return R.drawable.ic_education;
        }
    }

    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}