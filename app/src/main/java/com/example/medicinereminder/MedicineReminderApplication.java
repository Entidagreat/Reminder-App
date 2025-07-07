package com.example.medicinereminder;

import android.app.Application;
import android.content.Context;
import com.example.medicinereminder.utils.LocaleHelper;

public class MedicineReminderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Thiết lập ngôn ngữ tiếng Việt
        LocaleHelper.setLocale(this, "vi");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base, "vi"));
    }
}