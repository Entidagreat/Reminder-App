package com.example.medicinereminder.models;

import java.util.Date;

public class NotificationItem {
    private long id;
    private String title;
    private String message;
    private String type; // "missed_dose", "refill_reminder", "general"
    private Date createdAt;
    private boolean isRead;
    private int medicationId;
    private String scheduledTime;
    private int retryCount;

    public NotificationItem() {}

    public NotificationItem(String title, String message, String type, int medicationId, String scheduledTime) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.medicationId = medicationId;
        this.scheduledTime = scheduledTime;
        this.createdAt = new Date();
        this.isRead = false;
        this.retryCount = 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getMedicationId() { return medicationId; }
    public void setMedicationId(int medicationId) { this.medicationId = medicationId; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
}