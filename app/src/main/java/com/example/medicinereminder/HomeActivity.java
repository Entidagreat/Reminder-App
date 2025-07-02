package com.example.medicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.adapters.CombinedReminderAdapter;
import com.example.medicinereminder.adapters.MedicationAdapter;
import com.example.medicinereminder.models.DoseHistory;
import com.example.medicinereminder.models.EducationalReminder;
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.utils.DatabaseHelper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private MedicationAdapter medicationAdapter;

    private TextView progressText;
    private ProgressBar dailyProgressBar;
    private RecyclerView todaysScheduleRecycler;
    private TextView seeAllButton;
    private TextView noMedicationsText;
    private CardView addMedicationCard;
    private CardView calendarCard;
    private CardView historyCard;
    private CardView refillCard;
    private CardView educationalReminderCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh data when returning to home
    }

    private void initViews() {
        progressText = findViewById(R.id.progressText);
        dailyProgressBar = findViewById(R.id.dailyProgressBar);
        todaysScheduleRecycler = findViewById(R.id.todaysScheduleRecycler);
        seeAllButton = findViewById(R.id.seeAllButton);
        noMedicationsText = findViewById(R.id.noMedicationsText);
        addMedicationCard = findViewById(R.id.addMedicationCard);
        calendarCard = findViewById(R.id.calendarCard);
        historyCard = findViewById(R.id.historyCard);
        refillCard = findViewById(R.id.refillCard);
        educationalReminderCard = findViewById(R.id.educationalReminderCard);

        // Setup RecyclerView
        todaysScheduleRecycler.setLayoutManager(new LinearLayoutManager(this));
        medicationAdapter = new MedicationAdapter(new ArrayList<>(), this::onMedicationClick);
        todaysScheduleRecycler.setAdapter(medicationAdapter);
    }

    private void setupClickListeners() {
        addMedicationCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddReminderSelectionActivity.class);
            startActivity(intent);
        });

        calendarCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        historyCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        refillCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RefillActivity.class);
            startActivity(intent);
        });

        educationalReminderCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EducationalReminderActivity.class);
            startActivity(intent);
        });

        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
    }

    private void loadData() {
        // Get today's medications
        List<Medication> todaysMedications = dbHelper.getTodaysMedications();

        // Get today's educational reminders
        List<EducationalReminder> todaysEducationalReminders = dbHelper.getTodaysEducationalReminders();

        // For future: Add other types of reminders here

        // Show appropriate UI if there are no reminders
        if (todaysMedications.isEmpty() && todaysEducationalReminders.isEmpty()) {
            showNoReminders();
        } else {
            // Show medications and reminders
            showScheduledItems(todaysMedications, todaysEducationalReminders);
            updateDailyProgress(todaysMedications);
        }
    }

    private void showNoReminders() {
        todaysScheduleRecycler.setVisibility(View.GONE);
        noMedicationsText.setVisibility(View.VISIBLE);
        seeAllButton.setVisibility(View.GONE);
        progressText.setText("0 of 0 doses");
        dailyProgressBar.setProgress(0);
    }

    private void showScheduledItems(List<Medication> medications, List<EducationalReminder> educationalReminders) {
        todaysScheduleRecycler.setVisibility(View.VISIBLE);
        noMedicationsText.setVisibility(View.GONE);
        seeAllButton.setVisibility(View.VISIBLE);

        // Create a combined adapter that handles both medication and educational reminders
        // Truyền callback rỗng để tránh double ghi nhận lịch sử khi bấm "Uống"
        CombinedReminderAdapter adapter = new CombinedReminderAdapter(
                medications,
                educationalReminders,
                m -> {}, // callback rỗng, không làm gì
                () -> updateDailyProgress(medications), // callback cập nhật tiến độ ngay khi uống
                reminder -> onEducationalReminderClick(reminder)
        );

        todaysScheduleRecycler.setAdapter(adapter);
    }

    private void updateDailyProgress(List<Medication> medications) {
        int totalDoses = 0;
        int takenDoses = 0;

        for (Medication medication : medications) {
            int dailyDoses = medication.getDailyDoseCount();
            totalDoses += dailyDoses;

            // Check how many doses have been taken today
            List<DoseHistory> todaysHistory = getTodaysHistoryForMedication(medication.getId());
            for (DoseHistory history : todaysHistory) {
                if (history.isTaken()) {
                    takenDoses++;
                }
            }
        }

        // Update progress display
        progressText.setText(getString(R.string.doses_progress, takenDoses, totalDoses));

        int progressPercentage = totalDoses > 0 ? (takenDoses * 100) / totalDoses : 0;
        dailyProgressBar.setProgress(progressPercentage);
    }

    private List<DoseHistory> getTodaysHistoryForMedication(int medicationId) {
        List<DoseHistory> allTodaysHistory = dbHelper.getTodaysDoseHistory();
        List<DoseHistory> medicationHistory = new ArrayList<>();

        for (DoseHistory history : allTodaysHistory) {
            if (history.getMedicationId() == medicationId) {
                medicationHistory.add(history);
            }
        }

        return medicationHistory;
    }

    private void onMedicationClick(Medication medication) {
        // Create dose history entry for taken medication
        DoseHistory doseHistory = new DoseHistory(
                medication.getId(),
                medication.getName(),
                medication.getDosage(),
                new Date(),
                "taken"
        );

        dbHelper.addDoseHistory(doseHistory);

        // Update medication supply if refill tracking is enabled
        if (medication.isRefillTrackingEnabled()) {
            medication.setCurrentSupply(medication.getCurrentSupply() - 1);
            dbHelper.updateMedication(medication);
        }

        // Refresh the display
        loadData();
    }

    // Add method to handle educational reminder click
    private void onEducationalReminderClick(EducationalReminder reminder) {
        Intent intent = new Intent(HomeActivity.this, EducationalReminderActivity.class);
        intent.putExtra("reminder_id", reminder.getId());
        startActivity(intent);
    }

    private String getGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            return "Good Morning";
        } else if (hour < 17) {
            return "Good Afternoon";
        } else {
            return "Good Evening";
        }
    }
}