# 💊 Medicine Reminder Application

## 📋 Overview

**Medicine Reminder** is an Android application designed to help users manage their medication schedules. The app enables users to track when to take medications, log medication history, and receive timely notifications.

## 🚀 Key Features

### 🧾 Medication Management

- Add new medications with detailed information:
  - Medication name and dosage
  - Frequency (once, twice, three times, or four times daily)
  - Duration (7, 14, 30, 90 days, or ongoing)
  - Custom time slots for medication intake
  - Start date customization
- Update or delete existing medications
- View detailed information of each medication

### 🔔 Reminders and Notifications
- Receive notifications when it's time to take medications
- Missed dose reminders to ensure medication compliance
- Visual notification badges for unread notifications
- Notification center to view all medication alerts

### 📊 Medication Tracking
- Mark medications as taken directly from the home screen
- View medication history with statuses: `Taken`, `Missed`, or `Skipped`
- Clear medication history if needed

### 📅 Calendar View
- Integration with calendar to view scheduled medications by date
- Vietnamese date formatting for better localization

### 🖼️ User Interface
- Clean and intuitive UI with full Vietnamese language support
- Daily progress tracking for medication intake
- Quick action buttons for common operations

## ⚙️ Technical Details

### 🏗️ Architecture

- Java-based Android application
- `SQLite` for local data storage via `DatabaseHelper`
- `SharedPreferences` for user settings via `SharedPreferencesHelper`

### 🧩 Key Components
- **Activities**: Home, Calendar, History, Medication Detail, Add Medication
- **Adapters**: Calendar, Medication, History, CombinedReminder
- **Utils**: DateTime helpers, Notification management, Locale settings
- **Services**: Background service for checking missed doses

### 🌐 Localization
- Full Vietnamese language support
- Custom date formatting for Vietnamese locale

## 📦 Installation
1. Clone this repository
2. Open the project in Android Studio
3. Build and run on an Android device or emulator (minimum SDK 24, target SDK 34)

## 🔮 Future Improvements
- Add support for additional languages
- Implement medication inventory tracking
- Add backup and restore functionality
- Generate statistical reports on medication adherence...
