package com.example.medicinereminder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinereminder.adapters.EducationalReminderAdapter;
import com.example.medicinereminder.models.EducationalReminder;
import com.example.medicinereminder.utils.DatabaseHelper;
import com.example.medicinereminder.utils.NotificationHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EducationalReminderActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private NotificationHelper notificationHelper;
    
    private ImageButton backButton;
    private ImageButton addButton;
    private ChipGroup filterChipGroup;
    private RecyclerView todayRemindersRecycler;
    private TextView noTodayRemindersText;
    private RecyclerView upcomingRemindersRecycler;
    private TextView noUpcomingRemindersText;
    
    private EducationalReminderAdapter todayAdapter;
    private EducationalReminderAdapter upcomingAdapter;
    
    private Dialog reminderDialog;
    private String currentFilter = "all"; // Default filter
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational_reminder);
        
        dbHelper = new DatabaseHelper(this);
        notificationHelper = new NotificationHelper(this);
        
        initViews();
        setupListeners();
        loadData();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.backButton);
        addButton = findViewById(R.id.addButton);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        todayRemindersRecycler = findViewById(R.id.todayRemindersRecycler);
        noTodayRemindersText = findViewById(R.id.noTodayRemindersText);
        upcomingRemindersRecycler = findViewById(R.id.upcomingRemindersRecycler);
        noUpcomingRemindersText = findViewById(R.id.noUpcomingRemindersText);
        
        // Set up RecyclerViews
        todayRemindersRecycler.setLayoutManager(new LinearLayoutManager(this));
        upcomingRemindersRecycler.setLayoutManager(new LinearLayoutManager(this));
        
        todayAdapter = new EducationalReminderAdapter(new ArrayList<>(), this::onReminderClick, this::onCompletionToggled);
        upcomingAdapter = new EducationalReminderAdapter(new ArrayList<>(), this::onReminderClick, this::onCompletionToggled);
        
        todayRemindersRecycler.setAdapter(todayAdapter);
        upcomingRemindersRecycler.setAdapter(upcomingAdapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        addButton.setOnClickListener(v -> showAddReminderDialog(null));
        
        // Set up filter chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = findViewById(checkedId);
            if (selectedChip != null) {
                if (checkedId == R.id.chipAll) {
                    currentFilter = "all";
                } else if (checkedId == R.id.chipStudy) {
                    currentFilter = "study_session";
                } else if (checkedId == R.id.chipAssignment) {
                    currentFilter = "assignment";
                } else if (checkedId == R.id.chipExam) {
                    currentFilter = "exam";
                } else if (checkedId == R.id.chipLanguage) {
                    currentFilter = "language_practice";
                }
                loadData();
            }
        });
    }
    
    private void loadData() {
        List<EducationalReminder> todayReminders;
        List<EducationalReminder> upcomingReminders;
        
        if ("all".equals(currentFilter)) {
            todayReminders = dbHelper.getTodaysEducationalReminders();
            upcomingReminders = dbHelper.getUpcomingEducationalReminders();
            
            // Remove today's reminders from upcoming list to avoid duplicates
            List<EducationalReminder> filteredUpcoming = new ArrayList<>();
            for (EducationalReminder reminder : upcomingReminders) {
                if (!reminder.isDueToday()) {
                    filteredUpcoming.add(reminder);
                }
            }
            upcomingReminders = filteredUpcoming;
        } else {
            List<EducationalReminder> filteredReminders = dbHelper.getEducationalRemindersByType(currentFilter);
            todayReminders = new ArrayList<>();
            upcomingReminders = new ArrayList<>();
            
            for (EducationalReminder reminder : filteredReminders) {
                if (reminder.isDueToday()) {
                    todayReminders.add(reminder);
                } else if (!reminder.isCompleted() && reminder.getReminderDate() != null && 
                           reminder.getReminderDate().after(new Date())) {
                    upcomingReminders.add(reminder);
                }
            }
        }
        
        // Update Today's reminders section
        if (todayReminders.isEmpty()) {
            todayRemindersRecycler.setVisibility(View.GONE);
            noTodayRemindersText.setVisibility(View.VISIBLE);
        } else {
            todayRemindersRecycler.setVisibility(View.VISIBLE);
            noTodayRemindersText.setVisibility(View.GONE);
            todayAdapter.updateReminders(todayReminders);
        }
        
        // Update Upcoming reminders section
        if (upcomingReminders.isEmpty()) {
            upcomingRemindersRecycler.setVisibility(View.GONE);
            noUpcomingRemindersText.setVisibility(View.VISIBLE);
        } else {
            upcomingRemindersRecycler.setVisibility(View.VISIBLE);
            noUpcomingRemindersText.setVisibility(View.GONE);
            upcomingAdapter.updateReminders(upcomingReminders);
        }
    }
    
    private void onReminderClick(EducationalReminder reminder) {
        showAddReminderDialog(reminder);
    }
    
    private void onCompletionToggled(EducationalReminder reminder, boolean isCompleted) {
        dbHelper.markEducationalReminderAsCompleted(reminder.getId(), isCompleted);
        loadData();
    }
    
    private void showAddReminderDialog(EducationalReminder existingReminder) {
        reminderDialog = new Dialog(this, android.R.style.Theme_NoTitleBar_Fullscreen);
        reminderDialog.setContentView(R.layout.dialog_educational_reminder);
        
        // Initialize dialog views
        TextView dialogTitle = reminderDialog.findViewById(R.id.dialogTitle);
        EditText titleEditText = reminderDialog.findViewById(R.id.titleEditText);
        EditText descriptionEditText = reminderDialog.findViewById(R.id.descriptionEditText);
        Spinner typeSpinner = reminderDialog.findViewById(R.id.typeSpinner);
        Button dateButton = reminderDialog.findViewById(R.id.dateButton);
        Button timeButton = reminderDialog.findViewById(R.id.timeButton);
        RadioGroup priorityRadioGroup = reminderDialog.findViewById(R.id.priorityRadioGroup);
        EditText notesEditText = reminderDialog.findViewById(R.id.notesEditText);
        
        // Use SwitchMaterial instead of Switch to match the layout XML
        com.google.android.material.switchmaterial.SwitchMaterial notificationSwitch = 
            reminderDialog.findViewById(R.id.notificationSwitch);
        
        Button deleteButton = reminderDialog.findViewById(R.id.deleteButton);
        ImageButton closeButton = reminderDialog.findViewById(R.id.closeButton);
        Button saveButton = reminderDialog.findViewById(R.id.saveButton);
        
        // Set up spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.reminder_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        
        // Initialize calendar for date and time
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        if (existingReminder != null) {
            // Edit mode
            dialogTitle.setText(R.string.edit_educational_reminder);
            titleEditText.setText(existingReminder.getTitle());
            descriptionEditText.setText(existingReminder.getDescription());
            
            // Set spinner selection
            switch (existingReminder.getType()) {
                case "study_session":
                    typeSpinner.setSelection(0);
                    break;
                case "assignment":
                    typeSpinner.setSelection(1);
                    break;
                case "exam":
                    typeSpinner.setSelection(2);
                    break;
                case "language_practice":
                    typeSpinner.setSelection(3);
                    break;
                default:
                    typeSpinner.setSelection(4);
            }
              // Set date and time
            if (existingReminder.getReminderDate() != null) {
                calendar.setTime(existingReminder.getReminderDate());
                String formattedDate = dateFormat.format(calendar.getTime());
                dateButton.setText(formattedDate);
                dateButton.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
            
            if (existingReminder.getReminderTime() != null && !existingReminder.getReminderTime().isEmpty()) {
                timeButton.setText(existingReminder.getReminderTime());
                timeButton.setTextColor(getResources().getColor(R.color.text_primary, null));
            }
            
            // Set priority
            switch (existingReminder.getPriority()) {
                case 1:
                    ((RadioButton)priorityRadioGroup.findViewById(R.id.radioHigh)).setChecked(true);
                    break;
                case 3:
                    ((RadioButton)priorityRadioGroup.findViewById(R.id.radioLow)).setChecked(true);
                    break;
                default:
                    ((RadioButton)priorityRadioGroup.findViewById(R.id.radioMedium)).setChecked(true);
            }
            
            notesEditText.setText(existingReminder.getNotes());
            notificationSwitch.setChecked(existingReminder.isNotificationEnabled());
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            // Add mode
            dialogTitle.setText(R.string.add_educational_reminder);
              // Set today's date by default
            String formattedDate = dateFormat.format(calendar.getTime());
            dateButton.setText(formattedDate);
            dateButton.setTextColor(getResources().getColor(R.color.text_primary, null));
            
            // Set current time by default
            String formattedTime = timeFormat.format(calendar.getTime());
            timeButton.setText(formattedTime);
            timeButton.setTextColor(getResources().getColor(R.color.text_primary, null));
            
            deleteButton.setVisibility(View.GONE);
        }
        
        // Date button click listener
        dateButton.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String formattedDate = dateFormat.format(calendar.getTime());
                        dateButton.setText(formattedDate);
                        dateButton.setTextColor(getResources().getColor(R.color.text_primary, null));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
        
        // Time button click listener
        timeButton.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view, hourOfDay, minute) -> {                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);
                        String formattedTime = timeFormat.format(calendar.getTime());
                        timeButton.setText(formattedTime);
                        timeButton.setTextColor(getResources().getColor(R.color.text_primary, null));
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true);
            timePickerDialog.show();
        });
        
        // Delete button click listener
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirm_delete)
                    .setMessage(R.string.delete_reminder_message)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        dbHelper.deleteEducationalReminder(existingReminder.getId());
                        Toast.makeText(this, R.string.reminder_deleted_success, Toast.LENGTH_SHORT).show();
                        reminderDialog.dismiss();
                        loadData();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        
        // Close button click listener
        closeButton.setOnClickListener(v -> reminderDialog.dismiss());
        
        // Save button click listener
        saveButton.setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String type;
            switch (typeSpinner.getSelectedItemPosition()) {
                case 0:
                    type = "study_session";
                    break;
                case 1:
                    type = "assignment";
                    break;
                case 2:
                    type = "exam";
                    break;
                case 3:
                    type = "language_practice";
                    break;
                default:
                    type = "other";
            }
            
            // Validation
            if (title.isEmpty()) {
                titleEditText.setError(getString(R.string.error_empty_title));
                return;
            }
            
            // Create or update reminder
            EducationalReminder reminder;
            if (existingReminder != null) {
                reminder = existingReminder;
            } else {
                reminder = new EducationalReminder();
            }
            
            reminder.setTitle(title);
            reminder.setDescription(description);
            reminder.setType(type);
            reminder.setReminderDate(calendar.getTime());
            reminder.setReminderTime(timeButton.getText().toString());
            
            // Set priority based on selected radio button
            int priority = 2; // Default to medium
            int selectedRadioButtonId = priorityRadioGroup.getCheckedRadioButtonId();
            if (selectedRadioButtonId == R.id.radioHigh) {
                priority = 1;
            } else if (selectedRadioButtonId == R.id.radioLow) {
                priority = 3;
            }
            reminder.setPriority(priority);
            
            reminder.setNotes(notesEditText.getText().toString().trim());
            reminder.setNotificationEnabled(notificationSwitch.isChecked());
            
            // Save to database
            if (existingReminder != null) {
                dbHelper.updateEducationalReminder(reminder);
                Toast.makeText(this, R.string.reminder_updated_success, Toast.LENGTH_SHORT).show();
            } else {
                long id = dbHelper.addEducationalReminder(reminder);
                reminder.setId((int) id);
                Toast.makeText(this, R.string.reminder_added_success, Toast.LENGTH_SHORT).show();
            }
            
            // Schedule notification if enabled
            if (reminder.isNotificationEnabled()) {
                scheduleReminderNotification(reminder);
            }
            
            reminderDialog.dismiss();
            loadData();
        });
        
        reminderDialog.show();
    }
    
    private void scheduleReminderNotification(EducationalReminder reminder) {
        // Convert reminder time to notification time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(reminder.getReminderDate());
        
        String[] timeParts = reminder.getReminderTime().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        
        // Schedule notification
        notificationHelper.scheduleEducationalReminderNotification(
                reminder.getId(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getTypeLabel(),
                calendar.getTimeInMillis()
        );
    }
}
