package com.example.medicinereminder.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Medication {
    private int id;
    private String name;
    private String dosage;
    private String frequency; // "once", "twice", "three_times", "four_times", "as_needed"
    private int duration; // in days
    private Date startDate;
    private Date endDate;
    private boolean reminderEnabled;
    private boolean refillTrackingEnabled;
    private int currentSupply;
    private int refillThreshold; // percentage
    private Date lastRefillDate;
    private List<String> reminderTimes;
    private boolean isActive;

    public Medication() {
        this.reminderTimes = new ArrayList<>();
        this.isActive = true;
        this.reminderEnabled = true;
        this.refillTrackingEnabled = false;
        this.currentSupply = 0;
        this.refillThreshold = 20;
    }

    public Medication(String name, String dosage, String frequency, int duration, Date startDate) {
        this();
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
        this.startDate = startDate;
        calculateEndDate();
    }

    public void calculateEndDate() {
        if (startDate != null && duration > 0) {
            long endTime = startDate.getTime() + (duration * 24L * 60L * 60L * 1000L);
            this.endDate = new Date(endTime);
        }
    }

    public int getDailyDoseCount() {
        switch (frequency) {
            case "once":
                return 1;
            case "twice":
                return 2;
            case "three_times":
                return 3;
            case "four_times":
                return 4;
            case "as_needed":
                return 0;
            default:
                return 1;
        }
    }

    public List<String> getDefaultReminderTimes() {
        List<String> times = new ArrayList<>();
        switch (frequency) {
            case "once":
                times.add("08:00");
                break;
            case "twice":
                times.add("08:00");
                times.add("20:00");
                break;
            case "three_times":
                times.add("08:00");
                times.add("14:00");
                times.add("20:00");
                break;
            case "four_times":
                times.add("08:00");
                times.add("12:00");
                times.add("16:00");
                times.add("20:00");
                break;
        }
        return times;
    }

    public boolean isValidToday() {
        Date today = new Date();
        return isActive && startDate != null &&
                (startDate.before(today) || isSameDay(startDate, today)) &&
                (endDate == null || endDate.after(today) || isSameDay(endDate, today));
    }

    private boolean isSameDay(Date date1, Date date2) {
        return date1.getYear() == date2.getYear() &&
                date1.getMonth() == date2.getMonth() &&
                date1.getDate() == date2.getDate();
    }

    public boolean needsRefill() {
        if (!refillTrackingEnabled) return false;
        int dailyDoses = getDailyDoseCount();
        if (dailyDoses == 0) return false;

        double supplyPercentage = (currentSupply * 100.0) / (dailyDoses * 30); // 30 days supply
        return supplyPercentage <= refillThreshold;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) {
        this.frequency = frequency;
        if (reminderTimes.isEmpty()) {
            this.reminderTimes = getDefaultReminderTimes();
        }
    }

    public int getDuration() { return duration; }
    public void setDuration(int duration) {
        this.duration = duration;
        calculateEndDate();
    }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        calculateEndDate();
    }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }
    public boolean getReminderEnabled() {return reminderEnabled;}

    public boolean isRefillTrackingEnabled() { return refillTrackingEnabled; }
    public void setRefillTrackingEnabled(boolean refillTrackingEnabled) { this.refillTrackingEnabled = refillTrackingEnabled; }

    public int getCurrentSupply() { return currentSupply; }
    public void setCurrentSupply(int currentSupply) { this.currentSupply = currentSupply; }

    public int getRefillThreshold() { return refillThreshold; }
    public void setRefillThreshold(int refillThreshold) { this.refillThreshold = refillThreshold; }

    public Date getLastRefillDate() { return lastRefillDate; }
    public void setLastRefillDate(Date lastRefillDate) { this.lastRefillDate = lastRefillDate; }

    public List<String> getReminderTimes() { return reminderTimes; }
    public void setReminderTimes(List<String> reminderTimes) { this.reminderTimes = reminderTimes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}