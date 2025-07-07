package com.example.medicinereminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
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
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.services.MissedDoseService;
import com.example.medicinereminder.utils.DatabaseHelper;
import com.example.medicinereminder.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private MedicationAdapter medicationAdapter;

    // Timer for auto refresh
    private Handler autoRefreshHandler;
    private Runnable autoRefreshRunnable;
    private static final int AUTO_REFRESH_INTERVAL = 5000; // 5 seconds

    private TextView progressText;
    private ProgressBar dailyProgressBar;
    private RecyclerView todaysScheduleRecycler;
    private TextView seeAllButton;
    private TextView noMedicationsText;
    private CardView addMedicationCard;
    private CardView calendarCard;
    private CardView historyCard;

    private View notificationDot;
    private ImageView notificationIcon;

    private BroadcastReceiver notificationReceiver;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "vi"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        setupAutoRefresh(); // Setup timer for auto refresh
        setupNotificationReceiver();
        loadData();

        notificationDot = findViewById(R.id.notificationDot);

        // Start missed dose service
        Intent serviceIntent = new Intent(this, MissedDoseService.class);
        startService(serviceIntent);
    }

    private void setupNotificationReceiver() {
        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("com.example.medicinereminder.NOTIFICATION_ADDED".equals(intent.getAction())) {
                    // Cập nhật ngay lập tức trên UI thread
                    runOnUiThread(() -> updateNotificationBadge());
                }
            }
        };
        
        IntentFilter filter = new IntentFilter("com.example.medicinereminder.NOTIFICATION_ADDED");
        // Kiểm tra API level và thêm flag phù hợp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ cần flag RECEIVER_NOT_EXPORTED vì đây là broadcast nội bộ
            registerReceiver(notificationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
        else {
            registerReceiver(notificationReceiver, filter);
        }
    }

    private void openNotificationCenter() {
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        startActivity(intent);
    }

    private void updateNotificationBadge() {
        int unreadCount = dbHelper.getUnreadNotificationCount();
        if (unreadCount > 0) {
            notificationDot.setVisibility(View.VISIBLE); // Hiện chấm đỏ
        } else {
            notificationDot.setVisibility(View.GONE); // Ẩn chấm đỏ
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // Refresh data when returning to home
        // Start auto refresh when activity is resumed
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL);
        updateNotificationBadge();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Stop auto refresh when activity is paused
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    @Override
    protected void onDestroy() {
        // Make sure to remove all callbacks to prevent memory leaks
        if (autoRefreshHandler != null) {
            autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
        }
        super.onDestroy();
        
        if (notificationReceiver != null) {
            unregisterReceiver(notificationReceiver);
        }
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
        
        // Setup RecyclerView
        todaysScheduleRecycler.setLayoutManager(new LinearLayoutManager(this));
        medicationAdapter = new MedicationAdapter(new ArrayList<>(), this::onMedicationClick);
        todaysScheduleRecycler.setAdapter(medicationAdapter);

        notificationIcon = findViewById(R.id.notificationIcon);
    }

    private void setupClickListeners() {
        addMedicationCard.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddMedicationActivity.class);
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

        seeAllButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        notificationIcon.setOnClickListener(v -> openNotificationCenter());
    }

    // Setup auto refresh timer
    private void setupAutoRefresh() {
        autoRefreshHandler = new Handler();
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                // Check if activity is still active before refreshing
                if (!isFinishing() && !isDestroyed()) {
                    loadData(); // Refresh data
                }
                // Schedule the next refresh
                autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL);
            }
        };
    }

    private void loadData() {
        // Get today's medications
        List<Medication> todaysMedications = dbHelper.getTodaysMedications();

        // Show appropriate UI if there are no reminders
        if (todaysMedications.isEmpty()) {
            showNoReminders();
        } else {
            // Show medications and reminders
            showScheduledItems(todaysMedications);
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

    private void showScheduledItems(List<Medication> medications) {
        todaysScheduleRecycler.setVisibility(View.VISIBLE);
        noMedicationsText.setVisibility(View.GONE);
        seeAllButton.setVisibility(View.VISIBLE);

        // Create a combined adapter that handles both medication and educational reminders
        // Truyền callback rỗng để tránh double ghi nhận lịch sử khi bấm "Uống"
        CombinedReminderAdapter adapter = new CombinedReminderAdapter(
                medications,
                m -> {}, // callback rỗng, không làm gì
                () -> updateDailyProgress(medications)// callback cập nhật tiến độ ngay khi uống
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
}