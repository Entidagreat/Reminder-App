package com.example.medicinereminder;


import android.content.Context;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.adapters.CalendarAdapter;
import com.example.medicinereminder.models.DoseHistory;
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.utils.DatabaseHelper;
import com.example.medicinereminder.utils.LocaleHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private CalendarAdapter calendarAdapter;

    private CalendarView calendarView;
    private RecyclerView medicationsRecycler;
    private TextView selectedDateText;
    private TextView noMedicationsText;

    private Date selectedDate;
    private SimpleDateFormat vietnameseDateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
    private SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "vi"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        dbHelper = new DatabaseHelper(this);
        selectedDate = new Date(); // Default to today

        initViews();
        setupCalendar();
        loadMedicationsForDate(selectedDate);

        // Thiết lập định dạng ngày tháng tiếng Việt
        setupCalendarWithVietnameseLocale();
    }

    private void setupCalendarWithVietnameseLocale() {
        CalendarView calendarView = findViewById(R.id.calendarView);
        
        TextView selectedDateText = findViewById(R.id.selectedDateText);
        
        // Thiết lập ngày hiện tại bằng tiếng Việt
        Calendar currentDate = Calendar.getInstance();
        SimpleDateFormat vietnameseFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        String formattedCurrentDate = vietnameseFormat.format(currentDate.getTime());
        selectedDateText.setText(formattedCurrentDate);

        // Thiết lập listener để hiển thị ngày bằng tiếng Việt
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            
            // Định dạng ngày tháng bằng tiếng Việt
            String formattedDate = vietnameseFormat.format(selectedDate.getTime());
            selectedDateText.setText(formattedDate);
            this.selectedDate = selectedDate.getTime();
            loadMedicationsForDate(selectedDate.getTime());
        });
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        medicationsRecycler = findViewById(R.id.medicationsRecycler);
        selectedDateText = findViewById(R.id.selectedDateText);
        noMedicationsText = findViewById(R.id.noMedicationsText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Setup RecyclerView
        medicationsRecycler.setLayoutManager(new LinearLayoutManager(this));
        calendarAdapter = new CalendarAdapter(new ArrayList<>(), this::onCalendarItemAction);
        medicationsRecycler.setAdapter(calendarAdapter);
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = calendar.getTime();

            // Sử dụng định dạng tiếng Việt cho tất cả các ngày
            String formattedDate = vietnameseDateFormat.format(selectedDate);
            selectedDateText.setText(formattedDate);

            loadMedicationsForDate(selectedDate);
        });
    }

    private void loadMedicationsForDate(Date date) {
        selectedDateText.setText(vietnameseDateFormat.format(date));

        // Get medications
        List<Medication> allMedications = dbHelper.getAllMedications();
        List<CalendarItem> itemsForDate = new ArrayList<>();

        // Process medications
        for (Medication medication : allMedications) {
            if (isMedicationValidForDate(medication, date)) {
                // Get dose history for this medication on this date
                List<DoseHistory> dayHistory = getDoseHistoryForDate(medication.getId(), date);

                int dailyDoses = medication.getDailyDoseCount();
                if (dailyDoses > 0) {
                    for (int i = 0; i < dailyDoses; i++) {
                        CalendarItem item = new CalendarItem();
                        item.type = "medication";
                        item.medication = medication;
                        item.doseNumber = i + 1;
                        item.scheduledTime = getScheduledTimeForDose(medication, i);

                        // Check if this dose was taken
                        item.doseHistory = findDoseHistoryForTime(dayHistory, item.scheduledTime);

                        itemsForDate.add(item);
                    }
                } else {
                    // As needed medication
                    CalendarItem item = new CalendarItem();
//                    item.type = "medication";
                    item.medication = medication;
                    item.doseNumber = 0;
                    item.scheduledTime = "As needed";
                    item.doseHistory = dayHistory.isEmpty() ? null : dayHistory.get(0);
                    itemsForDate.add(item);
                }
            }
        }
        if (itemsForDate.isEmpty()) {
            medicationsRecycler.setVisibility(android.view.View.GONE);
            noMedicationsText.setVisibility(android.view.View.VISIBLE);
        } else {
            medicationsRecycler.setVisibility(android.view.View.VISIBLE);
            noMedicationsText.setVisibility(android.view.View.GONE);
            calendarAdapter.updateItems(itemsForDate);
        }
    }

    private boolean isMedicationValidForDate(Medication medication, Date date) {
        if (!medication.isActive()) return false;

        Date startDate = medication.getStartDate();
        Date endDate = medication.getEndDate();

        if (startDate != null && date.before(startDate)) return false;
        if (endDate != null && date.after(endDate)) return false;

        return true;
    }

    private List<DoseHistory> getDoseHistoryForDate(int medicationId, Date date) {
        List<DoseHistory> allHistory = dbHelper.getAllDoseHistory();
        List<DoseHistory> dateHistory = new ArrayList<>();

        String dateString = dayFormat.format(date);

        for (DoseHistory history : allHistory) {
            if (history.getMedicationId() == medicationId) {
                String historyDateString = dayFormat.format(history.getScheduledTime());
                if (dateString.equals(historyDateString)) {
                    dateHistory.add(history);
                }
            }
        }

        return dateHistory;
    }

    private String getScheduledTimeForDose(Medication medication, int doseIndex) {
        List<String> reminderTimes = medication.getReminderTimes();
        if (reminderTimes != null && doseIndex < reminderTimes.size()) {
            return reminderTimes.get(doseIndex);
        }

        // Default times if no reminder times set
        switch (doseIndex) {
            case 0: return "08:00";
            case 1: return "14:00";
            case 2: return "20:00";
            case 3: return "22:00";
            default: return "08:00";
        }
    }

    private DoseHistory findDoseHistoryForTime(List<DoseHistory> history, String scheduledTime) {
        for (DoseHistory dose : history) {
            // Simple matching - in a real app you'd want more sophisticated time matching
            if (dose.getScheduledTime() != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String doseTime = timeFormat.format(dose.getScheduledTime());
                if (scheduledTime.equals(doseTime)) {
                    return dose;
                }
            }
        }
        return null;
    }

    private void onCalendarItemAction(CalendarItem item, String action) {
        if ("medication".equals(item.type)) {
            handleMedicationAction(item, action);
        }

        // Refresh the view
        loadMedicationsForDate(selectedDate);
    }

    private void handleMedicationAction(CalendarItem item, String action) {
        if (item.medication == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);

        // Set time based on scheduled time
        if (!"As needed".equals(item.scheduledTime)) {
            String[] timeParts = item.scheduledTime.split(":");
            if (timeParts.length == 2) {
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            }
        }

        Date scheduledDateTime = calendar.getTime();

        if (item.doseHistory == null) {
            // Create new dose history
            DoseHistory newHistory = new DoseHistory(
                    item.medication.getId(),
                    item.medication.getName(),
                    item.medication.getDosage(),
                    scheduledDateTime,
                    action
            );

            if ("taken".equals(action)) {
                newHistory.setTakenTime(new Date());
            }

            dbHelper.addDoseHistory(newHistory);
        } else {
            // Update existing dose history
            item.doseHistory.setStatus(action);
            if ("taken".equals(action)) {
                item.doseHistory.setTakenTime(new Date());
            }
            dbHelper.updateDoseHistory(item.doseHistory);
        }

        // Update medication supply if needed
        if ("taken".equals(action) && item.medication.isRefillTrackingEnabled()) {
            item.medication.setCurrentSupply(item.medication.getCurrentSupply() - 1);
            dbHelper.updateMedication(item.medication);
        }
    }

    public static class CalendarMedicationItem {
        public Medication medication;
        public int doseNumber;
        public String scheduledTime;
        public DoseHistory doseHistory;
    }

    // Create a new CalendarItem class that can represent any type of reminder
    public static class CalendarItem {
        public String type;
        public Medication medication;
        public int doseNumber;
        public String scheduledTime;
        public DoseHistory doseHistory;
    }
}