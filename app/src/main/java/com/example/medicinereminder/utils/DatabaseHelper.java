package com.example.medicinereminder.utils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.models.DoseHistory;
import com.example.medicinereminder.models.EducationalReminder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MedicineReminder.db";
    private static final int DATABASE_VERSION = 2;

    // Tables
    private static final String TABLE_MEDICATIONS = "medications";
    private static final String TABLE_DOSE_HISTORY = "dose_history";
    private static final String TABLE_EDUCATIONAL_REMINDERS = "educational_reminders"; // New table

    // Medications table columns
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DOSAGE = "dosage";
    private static final String KEY_FREQUENCY = "frequency";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_REMINDER_ENABLED = "reminder_enabled";
    private static final String KEY_REFILL_TRACKING = "refill_tracking";
    private static final String KEY_CURRENT_SUPPLY = "current_supply";
    private static final String KEY_REFILL_THRESHOLD = "refill_threshold";
    private static final String KEY_LAST_REFILL_DATE = "last_refill_date";
    private static final String KEY_REMINDER_TIMES = "reminder_times";
    private static final String KEY_IS_ACTIVE = "is_active";

    // Dose history table columns
    private static final String KEY_MEDICATION_ID = "medication_id";
    private static final String KEY_MEDICATION_NAME = "medication_name";
    private static final String KEY_SCHEDULED_TIME = "scheduled_time";
    private static final String KEY_TAKEN_TIME = "taken_time";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NOTES = "notes";

    // Educational reminders table columns
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_TYPE = "type";
    private static final String KEY_REMINDER_DATE = "reminder_date";
    private static final String KEY_REMINDER_TIME = "reminder_time";
    private static final String KEY_IS_COMPLETED = "is_completed";
    private static final String KEY_PRIORITY = "priority";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private Gson gson = new Gson();

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MEDICATIONS_TABLE = "CREATE TABLE " + TABLE_MEDICATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT NOT NULL,"
                + KEY_DOSAGE + " TEXT,"
                + KEY_FREQUENCY + " TEXT,"
                + KEY_DURATION + " INTEGER,"
                + KEY_START_DATE + " TEXT,"
                + KEY_END_DATE + " TEXT,"
                + KEY_REMINDER_ENABLED + " INTEGER DEFAULT 1,"
                + KEY_REFILL_TRACKING + " INTEGER DEFAULT 0,"
                + KEY_CURRENT_SUPPLY + " INTEGER DEFAULT 0,"
                + KEY_REFILL_THRESHOLD + " INTEGER DEFAULT 20,"
                + KEY_LAST_REFILL_DATE + " TEXT,"
                + KEY_REMINDER_TIMES + " TEXT,"
                + KEY_IS_ACTIVE + " INTEGER DEFAULT 1" + ")";

        String CREATE_DOSE_HISTORY_TABLE = "CREATE TABLE " + TABLE_DOSE_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEDICATION_ID + " INTEGER,"
                + KEY_MEDICATION_NAME + " TEXT,"
                + KEY_DOSAGE + " TEXT,"
                + KEY_SCHEDULED_TIME + " TEXT,"
                + KEY_TAKEN_TIME + " TEXT,"
                + KEY_STATUS + " TEXT,"
                + KEY_NOTES + " TEXT,"
                + "FOREIGN KEY(" + KEY_MEDICATION_ID + ") REFERENCES " + TABLE_MEDICATIONS + "(" + KEY_ID + ")"
                + ")";
                
        String CREATE_EDUCATIONAL_REMINDERS_TABLE = "CREATE TABLE " + TABLE_EDUCATIONAL_REMINDERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT NOT NULL,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_REMINDER_DATE + " TEXT,"
                + KEY_REMINDER_TIME + " TEXT,"
                + KEY_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_PRIORITY + " INTEGER DEFAULT 2,"
                + KEY_NOTES + " TEXT,"
                + KEY_NOTIFICATION_ENABLED + " INTEGER DEFAULT 1"
                + ")";

        db.execSQL(CREATE_MEDICATIONS_TABLE);
        db.execSQL(CREATE_DOSE_HISTORY_TABLE);
        db.execSQL(CREATE_EDUCATIONAL_REMINDERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Instead of dropping all tables, only add the missing ones
        if (oldVersion < 2) {
            // Create educational reminders table if upgrading from version 1
            String CREATE_EDUCATIONAL_REMINDERS_TABLE = "CREATE TABLE " + TABLE_EDUCATIONAL_REMINDERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT NOT NULL,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_REMINDER_DATE + " TEXT,"
                + KEY_REMINDER_TIME + " TEXT,"
                + KEY_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_PRIORITY + " INTEGER DEFAULT 2,"
                + KEY_NOTES + " TEXT,"
                + KEY_NOTIFICATION_ENABLED + " INTEGER DEFAULT 1"
                + ")";
            db.execSQL(CREATE_EDUCATIONAL_REMINDERS_TABLE);
        }
    }

    // Medication CRUD operations
    public long addMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, medication.getName());
        values.put(KEY_DOSAGE, medication.getDosage());
        values.put(KEY_FREQUENCY, medication.getFrequency());
        values.put(KEY_DURATION, medication.getDuration());
        values.put(KEY_START_DATE, dateFormat.format(medication.getStartDate()));
        if (medication.getEndDate() != null) {
            values.put(KEY_END_DATE, dateFormat.format(medication.getEndDate()));
        }
        values.put(KEY_REMINDER_ENABLED, medication.isReminderEnabled() ? 1 : 0);
        values.put(KEY_REFILL_TRACKING, medication.isRefillTrackingEnabled() ? 1 : 0);
        values.put(KEY_CURRENT_SUPPLY, medication.getCurrentSupply());
        values.put(KEY_REFILL_THRESHOLD, medication.getRefillThreshold());
        if (medication.getLastRefillDate() != null) {
            values.put(KEY_LAST_REFILL_DATE, dateFormat.format(medication.getLastRefillDate()));
        }
        values.put(KEY_REMINDER_TIMES, gson.toJson(medication.getReminderTimes()));
        values.put(KEY_IS_ACTIVE, medication.isActive() ? 1 : 0);

        long id = db.insert(TABLE_MEDICATIONS, null, values);
        db.close();
        return id;
    }

    public List<Medication> getAllMedications() {
        List<Medication> medications = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MEDICATIONS + " WHERE " + KEY_IS_ACTIVE + " = 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                medications.add(getMedicationFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return medications;
    }

    public List<Medication> getTodaysMedications() {
        List<Medication> allMedications = getAllMedications();
        List<Medication> todaysMedications = new ArrayList<>();

        for (Medication medication : allMedications) {
            if (medication.isValidToday()) {
                todaysMedications.add(medication);
            }
        }

        return todaysMedications;
    }

    public Medication getMedication(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEDICATIONS, null, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Medication medication = null;
        if (cursor != null && cursor.moveToFirst()) {
            medication = getMedicationFromCursor(cursor);
            cursor.close();
        }

        db.close();
        return medication;
    }

    public int updateMedication(Medication medication) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, medication.getName());
        values.put(KEY_DOSAGE, medication.getDosage());
        values.put(KEY_FREQUENCY, medication.getFrequency());
        values.put(KEY_DURATION, medication.getDuration());
        values.put(KEY_CURRENT_SUPPLY, medication.getCurrentSupply());
        values.put(KEY_REFILL_THRESHOLD, medication.getRefillThreshold());
        if (medication.getLastRefillDate() != null) {
            values.put(KEY_LAST_REFILL_DATE, dateFormat.format(medication.getLastRefillDate()));
        }
        values.put(KEY_REMINDER_TIMES, gson.toJson(medication.getReminderTimes()));

        int result = db.update(TABLE_MEDICATIONS, values, KEY_ID + "=?",
                new String[]{String.valueOf(medication.getId())});
        db.close();
        return result;
    }

    public void deleteMedication(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_ACTIVE, 0);
        db.update(TABLE_MEDICATIONS, values, KEY_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Dose History operations
    public long addDoseHistory(DoseHistory doseHistory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_MEDICATION_ID, doseHistory.getMedicationId());
        values.put(KEY_MEDICATION_NAME, doseHistory.getMedicationName());
        values.put(KEY_DOSAGE, doseHistory.getDosage());
        values.put(KEY_SCHEDULED_TIME, dateFormat.format(doseHistory.getScheduledTime()));
        if (doseHistory.getTakenTime() != null) {
            values.put(KEY_TAKEN_TIME, dateFormat.format(doseHistory.getTakenTime()));
        }
        values.put(KEY_STATUS, doseHistory.getStatus());
        values.put(KEY_NOTES, doseHistory.getNotes());

        long id = db.insert(TABLE_DOSE_HISTORY, null, values);
        db.close();
        return id;
    }

    public List<DoseHistory> getAllDoseHistory() {
        List<DoseHistory> history = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_DOSE_HISTORY + " ORDER BY " + KEY_SCHEDULED_TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                history.add(getDoseHistoryFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return history;
    }

    public List<DoseHistory> getTodaysDoseHistory() {
        List<DoseHistory> history = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dayFormat.format(new Date());

        String selectQuery = "SELECT * FROM " + TABLE_DOSE_HISTORY +
                " WHERE " + KEY_SCHEDULED_TIME + " LIKE '" + today + "%'" +
                " ORDER BY " + KEY_SCHEDULED_TIME + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                history.add(getDoseHistoryFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return history;
    }

    public void clearAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOSE_HISTORY, null, null);
        db.delete(TABLE_MEDICATIONS, null, null);
        db.delete(TABLE_EDUCATIONAL_REMINDERS, null, null); // Clear educational reminders table
        db.close();
    }

    // Educational Reminder CRUD operations
    
    // Add a new educational reminder
    public long addEducationalReminder(EducationalReminder reminder) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_TITLE, reminder.getTitle());
        values.put(KEY_DESCRIPTION, reminder.getDescription());
        values.put(KEY_TYPE, reminder.getType());
        values.put(KEY_REMINDER_DATE, dateFormat.format(reminder.getReminderDate()));
        values.put(KEY_REMINDER_TIME, reminder.getReminderTime());
        values.put(KEY_IS_COMPLETED, reminder.isCompleted() ? 1 : 0);
        values.put(KEY_PRIORITY, reminder.getPriority());
        values.put(KEY_NOTES, reminder.getNotes());
        values.put(KEY_NOTIFICATION_ENABLED, reminder.isNotificationEnabled() ? 1 : 0);
        
        long id = db.insert(TABLE_EDUCATIONAL_REMINDERS, null, values);
        db.close();
        
        return id;
    }
    
    // Get a single educational reminder by ID
    public EducationalReminder getEducationalReminder(int id) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_EDUCATIONAL_REMINDERS,
                null,
                KEY_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        
        EducationalReminder reminder = null;
        
        if (cursor != null && cursor.moveToFirst()) {
            reminder = cursorToEducationalReminder(cursor);
            cursor.close();
        }
        
        db.close();
        return reminder;
    }
    
    // Helper method to convert cursor to EducationalReminder object
    private EducationalReminder cursorToEducationalReminder(Cursor cursor) {
        EducationalReminder reminder = new EducationalReminder();
        
        reminder.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        reminder.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE)));
        reminder.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)));
        reminder.setType(cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)));
        
        try {
            String reminderDateStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_DATE));
            if (reminderDateStr != null && !reminderDateStr.isEmpty()) {
                reminder.setReminderDate(dateFormat.parse(reminderDateStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        reminder.setReminderTime(cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_TIME)));
        reminder.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_COMPLETED)) == 1);
        reminder.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_PRIORITY)));
        reminder.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTES)));
        reminder.setNotificationEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTIFICATION_ENABLED)) == 1);
        
        return reminder;
    }
    
    // Get all educational reminders
    public List<EducationalReminder> getAllEducationalReminders() {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        List<EducationalReminder> reminderList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_EDUCATIONAL_REMINDERS + " ORDER BY " + KEY_REMINDER_DATE + " ASC";
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                EducationalReminder reminder = cursorToEducationalReminder(cursor);
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return reminderList;
    }
    
    // Get today's educational reminders
    public List<EducationalReminder> getTodaysEducationalReminders() {
        List<EducationalReminder> allReminders = getAllEducationalReminders();
        List<EducationalReminder> todaysReminders = new ArrayList<>();
        
        for (EducationalReminder reminder : allReminders) {
            if (reminder.isDueToday() && !reminder.isCompleted()) {
                todaysReminders.add(reminder);
            }
        }
        
        return todaysReminders;
    }
    
    // Get all upcoming educational reminders (including today's)
    public List<EducationalReminder> getUpcomingEducationalReminders() {
        List<EducationalReminder> allReminders = getAllEducationalReminders();
        List<EducationalReminder> upcomingReminders = new ArrayList<>();
        
        Date today = new Date();
        for (EducationalReminder reminder : allReminders) {
            if (!reminder.isCompleted() && reminder.getReminderDate() != null && 
                (reminder.getReminderDate().after(today) || reminder.isDueToday())) {
                upcomingReminders.add(reminder);
            }
        }
        
        return upcomingReminders;
    }
    
    // Update an educational reminder
    public int updateEducationalReminder(EducationalReminder reminder) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_TITLE, reminder.getTitle());
        values.put(KEY_DESCRIPTION, reminder.getDescription());
        values.put(KEY_TYPE, reminder.getType());
        values.put(KEY_REMINDER_DATE, dateFormat.format(reminder.getReminderDate()));
        values.put(KEY_REMINDER_TIME, reminder.getReminderTime());
        values.put(KEY_IS_COMPLETED, reminder.isCompleted() ? 1 : 0);
        values.put(KEY_PRIORITY, reminder.getPriority());
        values.put(KEY_NOTES, reminder.getNotes());
        values.put(KEY_NOTIFICATION_ENABLED, reminder.isNotificationEnabled() ? 1 : 0);
        
        // Update the reminder where id = ?
        int result = db.update(TABLE_EDUCATIONAL_REMINDERS, values, KEY_ID + " = ?", 
            new String[] { String.valueOf(reminder.getId()) });
        
        db.close();
        return result;
    }
    
    // Mark an educational reminder as completed
    public int markEducationalReminderAsCompleted(int reminderId, boolean completed) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_IS_COMPLETED, completed ? 1 : 0);
        
        int rowsAffected = db.update(
                TABLE_EDUCATIONAL_REMINDERS,
                values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(reminderId)}
        );
        
        db.close();
        return rowsAffected;
    }
    
    // Delete an educational reminder
    public void deleteEducationalReminder(int reminderId) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(
                TABLE_EDUCATIONAL_REMINDERS,
                KEY_ID + " = ?",
                new String[]{String.valueOf(reminderId)}
        );
        db.close();
    }
    
    // Get educational reminders by type
    public List<EducationalReminder> getEducationalRemindersByType(String type) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        List<EducationalReminder> reminderList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
                TABLE_EDUCATIONAL_REMINDERS,
                null,
                KEY_TYPE + "=?",
                new String[]{type},
                null, null, 
                KEY_REMINDER_DATE + " ASC"
        );
        
        if (cursor.moveToFirst()) {
            do {
                EducationalReminder reminder = cursorToEducationalReminder(cursor);
                reminderList.add(reminder);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return reminderList;
    }

    // Check if table exists and create it if not
    private void ensureEducationalRemindersTableExists() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Check if the table exists
        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[]{TABLE_EDUCATIONAL_REMINDERS});
                
        boolean tableExists = cursor != null && cursor.getCount() > 0;
        
        if (cursor != null) {
            cursor.close();
        }
        
        // Create table if it doesn't exist
        if (!tableExists) {
            String CREATE_EDUCATIONAL_REMINDERS_TABLE = "CREATE TABLE " + TABLE_EDUCATIONAL_REMINDERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TITLE + " TEXT NOT NULL,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_REMINDER_DATE + " TEXT,"
                + KEY_REMINDER_TIME + " TEXT,"
                + KEY_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_PRIORITY + " INTEGER DEFAULT 2,"
                + KEY_NOTES + " TEXT,"
                + KEY_NOTIFICATION_ENABLED + " INTEGER DEFAULT 1"
                + ")";
            db.execSQL(CREATE_EDUCATIONAL_REMINDERS_TABLE);
        }
    }

    // Helper method to convert cursor to Medication object
    private Medication getMedicationFromCursor(Cursor cursor) {
        Medication medication = new Medication();
        medication.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        medication.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        medication.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DOSAGE)));
        medication.setFrequency(cursor.getString(cursor.getColumnIndexOrThrow(KEY_FREQUENCY)));
        medication.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION)));
        medication.setReminderEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REMINDER_ENABLED)) == 1);
        medication.setRefillTrackingEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REFILL_TRACKING)) == 1);
        medication.setCurrentSupply(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CURRENT_SUPPLY)));
        medication.setRefillThreshold(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_REFILL_THRESHOLD)));
        medication.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_IS_ACTIVE)) == 1);

        try {
            String startDateStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_START_DATE));
            if (startDateStr != null && !startDateStr.isEmpty()) {
                medication.setStartDate(dateFormat.parse(startDateStr));
                medication.calculateEndDate(); // Recalculate end date based on duration
            }

            String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_END_DATE));
            if (endDateStr != null && !endDateStr.isEmpty()) {
                medication.setEndDate(dateFormat.parse(endDateStr));
            }

            String lastRefillDateStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_REFILL_DATE));
            if (lastRefillDateStr != null && !lastRefillDateStr.isEmpty()) {
                medication.setLastRefillDate(dateFormat.parse(lastRefillDateStr));
            }

            String reminderTimesJson = cursor.getString(cursor.getColumnIndexOrThrow(KEY_REMINDER_TIMES));
            if (reminderTimesJson != null && !reminderTimesJson.isEmpty()) {
                List<String> reminderTimes = gson.fromJson(reminderTimesJson, new TypeToken<List<String>>(){}.getType());
                medication.setReminderTimes(reminderTimes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return medication;
    }

    // Helper method to convert cursor to DoseHistory object
    private DoseHistory getDoseHistoryFromCursor(Cursor cursor) {
        DoseHistory doseHistory = new DoseHistory();
        doseHistory.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)));
        doseHistory.setMedicationId(cursor.getInt(cursor.getColumnIndexOrThrow(KEY_MEDICATION_ID)));
        doseHistory.setMedicationName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_MEDICATION_NAME)));
        doseHistory.setDosage(cursor.getString(cursor.getColumnIndexOrThrow(KEY_DOSAGE)));
        doseHistory.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS)));
        doseHistory.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTES)));

        try {
            String scheduledTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_SCHEDULED_TIME));
            if (scheduledTimeStr != null && !scheduledTimeStr.isEmpty()) {
                doseHistory.setScheduledTime(dateFormat.parse(scheduledTimeStr));
            }

            String takenTimeStr = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TAKEN_TIME));
            if (takenTimeStr != null && !takenTimeStr.isEmpty()) {
                doseHistory.setTakenTime(dateFormat.parse(takenTimeStr));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doseHistory;
    }

    public int updateDoseHistory(DoseHistory doseHistory) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(KEY_STATUS, doseHistory.getStatus());
        
        if (doseHistory.getTakenTime() != null) {
            values.put(KEY_TAKEN_TIME, dateFormat.format(doseHistory.getTakenTime()));
        }
        
        // Update the dose history where id = ?
        int result = db.update(TABLE_DOSE_HISTORY, values, KEY_ID + " = ?", 
            new String[] { String.valueOf(doseHistory.getId()) });
            
        db.close();
        return result;
    }

    // Add this method to the DatabaseHelper class
    public List<EducationalReminder> getEducationalRemindersForDate(Date date) {
        // Ensure table exists
        ensureEducationalRemindersTableExists();
        
        List<EducationalReminder> allReminders = getAllEducationalReminders();
        List<EducationalReminder> remindersForDate = new ArrayList<>();
        
        // Format date for comparison
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateString = dayFormat.format(date);
        
        for (EducationalReminder reminder : allReminders) {
            if (reminder.getReminderDate() != null) {
                String reminderDateString = dayFormat.format(reminder.getReminderDate());
                if (dateString.equals(reminderDateString)) {
                    remindersForDate.add(reminder);
                }
            }
        }
        
        return remindersForDate;
    }
}