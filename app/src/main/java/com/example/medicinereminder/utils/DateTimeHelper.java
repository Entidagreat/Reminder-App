package com.example.medicinereminder.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeHelper {

    private static final Locale VIETNAMESE_LOCALE = new Locale("vi", "VN");

    public static String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", VIETNAMESE_LOCALE);
        return format.format(date);
    }

    public static String formatDateTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", VIETNAMESE_LOCALE);
        return format.format(date);
    }

    public static String formatDayName(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE", VIETNAMESE_LOCALE);
        return format.format(date);
    }

    public static String formatFullDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("EEEE, dd/MM/yyyy", VIETNAMESE_LOCALE);
        return format.format(date);
    }

    public static String formatTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", VIETNAMESE_LOCALE);
        return format.format(date);
    }

    public static String getGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Chào buổi sáng";
        } else if (hour < 17) {
            return "Chào buổi chiều";
        } else {
            return "Chào buổi tối";
        }
    }

    public static String getCurrentDate() {
        return formatDate(new Date());
    }

    public static String getCurrentDateTime() {
        return formatDateTime(new Date());
    }
}