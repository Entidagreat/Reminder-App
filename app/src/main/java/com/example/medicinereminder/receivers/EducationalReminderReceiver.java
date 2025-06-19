package com.example.medicinereminder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.medicinereminder.utils.NotificationHelper;

public class EducationalReminderReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        int reminderId = intent.getIntExtra("reminder_id", 0);
        String title = intent.getStringExtra("reminder_title");
        String description = intent.getStringExtra("reminder_description");
        String type = intent.getStringExtra("reminder_type");
        
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.showEducationalReminderNotification(reminderId, title, description, type);
    }
}
