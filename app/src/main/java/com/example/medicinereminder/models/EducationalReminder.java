package com.example.medicinereminder.models;

import java.util.Date;

public class EducationalReminder {
    private int id;
    private String title;
    private String description;
    private String type; // "study_session", "assignment", "exam", "language_practice", "other"
    private Date reminderDate;
    private String reminderTime;
    private boolean isCompleted;
    private int priority; // 1-high, 2-medium, 3-low
    private String notes;
    private boolean notificationEnabled;

    public EducationalReminder() {
        this.notificationEnabled = true;
        this.isCompleted = false;
        this.priority = 2; // Default to medium priority
    }

    public EducationalReminder(String title, String description, String type, Date reminderDate, 
                              String reminderTime) {
        this();
        this.title = title;
        this.description = description;
        this.type = type;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
    }

    // Get a human-readable type label
    public String getTypeLabel() {
        switch (type) {
            case "study_session":
                return "Study Session";
            case "assignment":
                return "Assignment Deadline";
            case "exam":
                return "Exam Date";
            case "language_practice":
                return "Language Practice";
            case "other":
                return "Other";
            default:
                return "Unknown";
        }
    }

    // Check if the reminder is due today
    public boolean isDueToday() {
        Date today = new Date();
        return !isCompleted && reminderDate != null &&
                (isSameDay(reminderDate, today));
    }

    // Check if the reminder is overdue (past date and not completed)
    public boolean isOverdue() {
        Date today = new Date();
        return !isCompleted && reminderDate != null &&
                reminderDate.before(today) && !isSameDay(reminderDate, today);
    }

    // Helper method to check if two dates are on the same day
    private boolean isSameDay(Date date1, Date date2) {
        return date1.getYear() == date2.getYear() &&
                date1.getMonth() == date2.getMonth() &&
                date1.getDate() == date2.getDate();
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(Date reminderDate) {
        this.reminderDate = reminderDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}
