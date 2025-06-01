package com.example.medicinereminder.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREF_NAME = "MedicineReminderPrefs";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_AUTH_ENABLED = "auth_enabled";
    private static final String KEY_PIN_CODE = "pin_code";
    private static final String KEY_USE_BIOMETRIC = "use_biometric";
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_LAST_BACKUP_DATE = "last_backup_date";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean isFirstLaunch) {
        editor.putBoolean(KEY_FIRST_LAUNCH, isFirstLaunch);
        editor.apply();
    }

    public boolean isAuthEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTH_ENABLED, false);
    }

    public void setAuthEnabled(boolean enabled) {
        editor.putBoolean(KEY_AUTH_ENABLED, enabled);
        editor.apply();
    }

    public String getPinCode() {
        return sharedPreferences.getString(KEY_PIN_CODE, "");
    }

    public void setPinCode(String pinCode) {
        editor.putString(KEY_PIN_CODE, pinCode);
        editor.apply();
    }

    public boolean useBiometric() {
        return sharedPreferences.getBoolean(KEY_USE_BIOMETRIC, true);
    }

    public void setUseBiometric(boolean useBiometric) {
        editor.putBoolean(KEY_USE_BIOMETRIC, useBiometric);
        editor.apply();
    }

    public boolean isNotificationEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATION_ENABLED, enabled);
        editor.apply();
    }

    public long getLastBackupDate() {
        return sharedPreferences.getLong(KEY_LAST_BACKUP_DATE, 0);
    }

    public void setLastBackupDate(long timestamp) {
        editor.putLong(KEY_LAST_BACKUP_DATE, timestamp);
        editor.apply();
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }
}