package com.example.medicinereminder.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.medicinereminder.HomeActivity;
import com.example.medicinereminder.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "medicine_reminder";
    private static final String CHANNEL_NAME = "Medicine Reminders";
    private static final String CHANNEL_DESCRIPTION = "Notifications for medication reminders";
    private static final int NOTIFICATION_ID = 1001;

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
        // Gửi broadcast để cập nhật UI ngay lập tức
        Intent broadcastIntent = new Intent("com.example.medicinereminder.NOTIFICATION_ADDED");
        broadcastIntent.setPackage(context.getPackageName()); // Đảm bảo chỉ gửi trong app
        context.sendBroadcast(broadcastIntent);
    }

    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }

    public void showMissedDoseReminder(String medicationName, String dosage, String scheduledTime, int retryCount) {
        String title = retryCount == 1 ? "Nhắc nhở liều thuốc đã bỏ lỡ" : "Nhắc nhở liều thuốc cuối cùng";
        String message = (retryCount == 1 ? "Bạn đã quên uống liều thuốc " : "Nhắc nhở cuối cùng: ") + 
                        medicationName + " (" + dosage + ") vào lúc " + scheduledTime;

        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        notificationManager.notify(NOTIFICATION_ID + 2000 + retryCount, builder.build());
        // Gửi broadcast để cập nhật UI ngay lập tức
        Intent broadcastIntent = new Intent("com.example.medicinereminder.NOTIFICATION_ADDED");
        broadcastIntent.setPackage(context.getPackageName()); // Đảm bảo chỉ gửi trong app
        context.sendBroadcast(broadcastIntent);
    }
}