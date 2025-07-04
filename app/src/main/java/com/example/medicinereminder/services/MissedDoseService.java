package com.example.medicinereminder.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import com.example.medicinereminder.models.DoseHistory;
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.models.NotificationItem;
import com.example.medicinereminder.utils.DatabaseHelper;
import com.example.medicinereminder.utils.NotificationHelper;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MissedDoseService extends Service {
    private static final int CHECK_INTERVAL = 10000; // 1 minute
    private static final int FIRST_REMINDER_DELAY = 60 * 1000; // 5 minutes
    // private static final int SECOND_REMINDER_DELAY = 10 * 60 * 1000; // 10 minutes total

    private Handler handler;
    private Runnable checkRunnable;
    private DatabaseHelper dbHelper;
    private NotificationHelper notificationHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);
        handler = new Handler(Looper.getMainLooper());
        setupPeriodicCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupPeriodicCheck() {
        checkRunnable = new Runnable() {
            @Override
            public void run() {
                checkMissedDoses();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(checkRunnable);
    }

    private void checkMissedDoses() {
        List<Medication> todaysMedications = dbHelper.getTodaysMedications();
        Calendar now = Calendar.getInstance();

        for (Medication medication : todaysMedications) {
            if (medication.getReminderTimes() != null) {
                for (String time : medication.getReminderTimes()) {
                    checkDoseTime(medication, time, now);
                }
            }
        }
    }

    private void checkDoseTime(Medication medication, String scheduleTime, Calendar now) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Calendar scheduleCalendar = Calendar.getInstance();
            scheduleCalendar.setTime(timeFormat.parse(scheduleTime));
            scheduleCalendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
            scheduleCalendar.set(Calendar.MONTH, now.get(Calendar.MONTH));
            scheduleCalendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));

            long timeDiff = now.getTimeInMillis() - scheduleCalendar.getTimeInMillis();

            // Check if dose was taken
            String todayStr = dateFormat.format(new Date());
            boolean doseTaken = isDoseTaken(medication.getId(), todayStr, scheduleTime);

            if (!doseTaken) {
                // Check for first reminder (5 minutes late)
                if (timeDiff >= FIRST_REMINDER_DELAY && timeDiff < FIRST_REMINDER_DELAY + CHECK_INTERVAL) {
                    handleFirstMissedDoseReminder(medication, scheduleTime);
                }
                // // Check for second reminder (10 minutes late)
                // else if (timeDiff >= SECOND_REMINDER_DELAY && timeDiff < SECOND_REMINDER_DELAY + CHECK_INTERVAL) {
                //     handleSecondMissedDoseReminder(medication, scheduleTime);
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isDoseTaken(int medicationId, String date, String time) {
        List<DoseHistory> histories = dbHelper.getTodaysDoseHistory();
        for (DoseHistory history : histories) {
            if (history.getMedicationId() == medicationId &&
                    history.getStatus().equals("taken") &&
                    history.getScheduledTime() != null) {

                SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                try {
                    Date scheduledDate = fullFormat.parse(date + " " + time);
                    if (scheduledDate != null &&
                            Math.abs(history.getScheduledTime().getTime() - scheduledDate.getTime()) < 60000) {
                        return true;
                    }
                } catch (Exception e) {
                    // Continue checking
                }
            }
        }
        return false;
    }

    private void handleFirstMissedDoseReminder(Medication medication, String scheduleTime) {
        // Create notification
        NotificationItem notification = new NotificationItem(
                "Missed Dose Reminder",
                "You missed taking " + medication.getName() + " (" + medication.getDosage() + ") at " + scheduleTime,
                "missed_dose",
                medication.getId(),
                scheduleTime
        );
        notification.setRetryCount(1);

        // Save to database
        dbHelper.addNotification(notification);

        // Show popup notification
        notificationHelper.showMissedDoseReminder(medication.getName(), medication.getDosage(), scheduleTime, 1);

        // After second reminder, mark as dismissed in history
        markDoseAsDismissed(medication, scheduleTime);
        
        // Broadcast để cập nhật badge
        sendBroadcast(new Intent("com.example.medicinereminder.NOTIFICATION_ADDED"));
    }

    // private void handleSecondMissedDoseReminder(Medication medication, String scheduleTime) {
    //     // Create notification
    //     NotificationItem notification = new NotificationItem(
    //             "Final Dose Reminder",
    //             "Final reminder: " + medication.getName() + " (" + medication.getDosage() + ") at " + scheduleTime,
    //             "missed_dose",
    //             medication.getId(),
    //             scheduleTime
    //     );
    //     notification.setRetryCount(2);

    //     // Save to database
    //     dbHelper.addNotification(notification);

    //     // Show popup notification
    //     notificationHelper.showMissedDoseReminder(medication.getName(), medication.getDosage(), scheduleTime, 2);

    //     // After second reminder, mark as dismissed in history
    //     markDoseAsDismissed(medication, scheduleTime);
        
    //     // Broadcast để cập nhật badge
    //     sendBroadcast(new Intent("com.example.medicinereminder.NOTIFICATION_ADDED"));
    // }

    private void markDoseAsDismissed(Medication medication, String scheduleTime) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            String todayStr = dateFormat.format(new Date());
            Date scheduledDateTime = fullFormat.parse(todayStr + " " + scheduleTime);

            DoseHistory missedHistory = new DoseHistory(
                    medication.getId(),
                    medication.getName(),
                    medication.getDosage(),
                    scheduledDateTime,
                    "missed" // Changed from "dismissed" to "missed"
            );

            dbHelper.addDoseHistory(missedHistory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && checkRunnable != null) {
            handler.removeCallbacks(checkRunnable);
        }
    }
}