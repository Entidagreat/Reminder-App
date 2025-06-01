package com.example.medicinereminder.models;

import java.util.Date;

public class DoseHistory {
    private int id;
    private int medicationId;
    private String medicationName;
    private String dosage;
    private Date scheduledTime;
    private Date takenTime;
    private String status; // "taken", "missed", "skipped"
    private String notes;

    public DoseHistory() {}

    public DoseHistory(int medicationId, String medicationName, String dosage, Date scheduledTime, String status) {
        this.medicationId = medicationId;
        this.medicationName = medicationName;
        this.dosage = dosage;
        this.scheduledTime = scheduledTime;
        this.status = status;
        if ("taken".equals(status)) {
            this.takenTime = new Date();
        }
    }

    public boolean isTaken() {
        return "taken".equals(status);
    }

    public boolean isMissed() {
        return "missed".equals(status);
    }

    public boolean isSkipped() {
        return "skipped".equals(status);
    }

    public void markAsTaken() {
        this.status = "taken";
        this.takenTime = new Date();
    }

    public void markAsMissed() {
        this.status = "missed";
        this.takenTime = null;
    }

    public void markAsSkipped() {
        this.status = "skipped";
        this.takenTime = null;
    }

    public String getStatusColor() {
        switch (status) {
            case "taken":
                return "#4CAF50"; // Green
            case "missed":
                return "#F44336"; // Red
            case "skipped":
                return "#FF9800"; // Orange
            default:
                return "#9E9E9E"; // Gray
        }
    }

    public String getDisplayStatus() {
        switch (status) {
            case "taken":
                return "Taken";
            case "missed":
                return "Missed";
            case "skipped":
                return "Skipped";
            default:
                return "Pending";
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMedicationId() { return medicationId; }
    public void setMedicationId(int medicationId) { this.medicationId = medicationId; }

    public String getMedicationName() { return medicationName; }
    public void setMedicationName(String medicationName) { this.medicationName = medicationName; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public Date getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Date scheduledTime) { this.scheduledTime = scheduledTime; }

    public Date getTakenTime() { return takenTime; }
    public void setTakenTime(Date takenTime) { this.takenTime = takenTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}