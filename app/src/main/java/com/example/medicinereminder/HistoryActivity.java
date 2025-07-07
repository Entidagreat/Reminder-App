package com.example.medicinereminder;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.adapters.HistoryAdapter;
import com.example.medicinereminder.models.DoseHistory;
import com.example.medicinereminder.utils.DatabaseHelper;
import com.example.medicinereminder.utils.LocaleHelper;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private HistoryAdapter historyAdapter;

    private RecyclerView historyRecycler;
    private TextView noHistoryText;
    private Button clearAllButton;
    private CardView allFilterCard;
    private CardView takenFilterCard;
    private CardView missedFilterCard;
    private TextView allFilterText;
    private TextView takenFilterText;
    private TextView missedFilterText;

    private String currentFilter = "all"; // "all", "taken", "missed"
    private List<DoseHistory> allHistory = new ArrayList<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, "vi"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
        loadHistory();
    }

    private void initViews() {
        historyRecycler = findViewById(R.id.historyRecycler);
        noHistoryText = findViewById(R.id.noHistoryText);
        clearAllButton = findViewById(R.id.clearAllButton);
        allFilterCard = findViewById(R.id.allFilterCard);
        takenFilterCard = findViewById(R.id.takenFilterCard);
        missedFilterCard = findViewById(R.id.missedFilterCard);
        allFilterText = findViewById(R.id.allFilterText);
        takenFilterText = findViewById(R.id.takenFilterText);
        missedFilterText = findViewById(R.id.missedFilterText);

        // Setup RecyclerView
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        historyRecycler.setAdapter(historyAdapter);

        // Set initial filter
        selectFilter("all");
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        allFilterCard.setOnClickListener(v -> selectFilter("all"));
        takenFilterCard.setOnClickListener(v -> selectFilter("taken"));
        missedFilterCard.setOnClickListener(v -> selectFilter("missed"));

        clearAllButton.setOnClickListener(v -> showClearAllDialog());
    }

    private void loadHistory() {
        allHistory = dbHelper.getAllDoseHistory();
        applyFilter();
    }

    private void selectFilter(String filter) {
        currentFilter = filter;

        // Reset all filter cards
        resetFilterCards();

        // Highlight selected filter
        switch (filter) {
            case "all":
                allFilterCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                allFilterText.setTextColor(getResources().getColor(R.color.white));
                break;
            case "taken":
                takenFilterCard.setCardBackgroundColor(getResources().getColor(R.color.status_taken));
                takenFilterText.setTextColor(getResources().getColor(R.color.white));
                break;
            case "missed":
                missedFilterCard.setCardBackgroundColor(getResources().getColor(R.color.status_missed));
                missedFilterText.setTextColor(getResources().getColor(R.color.white));
                break;
        }

        applyFilter();
    }

    private void resetFilterCards() {
        int defaultColor = getResources().getColor(R.color.card_background);
        int defaultTextColor = getResources().getColor(R.color.text_primary);

        allFilterCard.setCardBackgroundColor(defaultColor);
        takenFilterCard.setCardBackgroundColor(defaultColor);
        missedFilterCard.setCardBackgroundColor(defaultColor);

        allFilterText.setTextColor(defaultTextColor);
        takenFilterText.setTextColor(defaultTextColor);
        missedFilterText.setTextColor(defaultTextColor);
    }

    private void applyFilter() {
        List<DoseHistory> filteredHistory = new ArrayList<>();

        for (DoseHistory history : allHistory) {
            switch (currentFilter) {
                case "all":
                    filteredHistory.add(history);
                    break;
                case "taken":
                    if (history.isTaken()) {
                        filteredHistory.add(history);
                    }
                    break;
                case "missed":
                    if (history.isMissed()) {
                        filteredHistory.add(history);
                    }
                    break;
            }
        }

        if (filteredHistory.isEmpty()) {
            historyRecycler.setVisibility(View.GONE);
            noHistoryText.setVisibility(View.VISIBLE);
            clearAllButton.setVisibility(View.GONE);
        } else {
            historyRecycler.setVisibility(View.VISIBLE);
            noHistoryText.setVisibility(View.GONE);
            clearAllButton.setVisibility(View.VISIBLE);
            historyAdapter.updateHistory(filteredHistory);
        }
    }

    private void showClearAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tất cả dữ liệu")
                .setMessage("Bạn có chắc chắn xóa mọi dữ liệu về lịch sử uống thuốc? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                    dbHelper.clearAllData();
                    loadHistory();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}