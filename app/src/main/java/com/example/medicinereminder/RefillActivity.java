package com.example.medicinereminder;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.adapters.RefillAdapter;
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.utils.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RefillActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RefillAdapter refillAdapter;

    private RecyclerView refillRecycler;
    private TextView noMedicationsText;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refill);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadRefillMedications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRefillMedications(); // Refresh when returning to activity
    }

    private void initViews() {
        refillRecycler = findViewById(R.id.refillRecycler);
        noMedicationsText = findViewById(R.id.noMedicationsText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // Setup RecyclerView
        refillRecycler.setLayoutManager(new LinearLayoutManager(this));
        refillAdapter = new RefillAdapter(new ArrayList<>(), this::onRefillClick);
        refillRecycler.setAdapter(refillAdapter);
    }

    private void loadRefillMedications() {
        List<Medication> allMedications = dbHelper.getAllMedications();
        List<Medication> refillMedications = new ArrayList<>();

        for (Medication medication : allMedications) {
            if (medication.isRefillTrackingEnabled()) {
                refillMedications.add(medication);
            }
        }

        if (refillMedications.isEmpty()) {
            refillRecycler.setVisibility(View.GONE);
            noMedicationsText.setVisibility(View.VISIBLE);
            noMedicationsText.setText("No medications with refill tracking enabled");
        } else {
            refillRecycler.setVisibility(View.VISIBLE);
            noMedicationsText.setVisibility(View.GONE);
            refillAdapter.updateMedications(refillMedications);
        }
    }

    private void onRefillClick(Medication medication) {
        showRefillDialog(medication);
    }

    private void showRefillDialog(Medication medication) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_refill, null);

        TextView medicationNameText = dialogView.findViewById(R.id.medicationNameText);
        TextView currentSupplyText = dialogView.findViewById(R.id.currentSupplyText);
        EditText newSupplyEdit = dialogView.findViewById(R.id.newSupplyEdit);

        medicationNameText.setText(medication.getName());
        currentSupplyText.setText("Current supply: " + medication.getCurrentSupply());

        builder.setView(dialogView)
                .setTitle("Record Refill")
                .setPositiveButton("Record", (dialog, which) -> {
                    String newSupplyStr = newSupplyEdit.getText().toString().trim();
                    if (!TextUtils.isEmpty(newSupplyStr)) {
                        try {
                            int newSupply = Integer.parseInt(newSupplyStr);
                            recordRefill(medication, newSupply);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void recordRefill(Medication medication, int newSupply) {
        medication.setCurrentSupply(newSupply);
        medication.setLastRefillDate(new Date());

        int result = dbHelper.updateMedication(medication);

        if (result > 0) {
            Toast.makeText(this, "Refill recorded successfully", Toast.LENGTH_SHORT).show();
            loadRefillMedications(); // Refresh the list
        } else {
            Toast.makeText(this, "Failed to record refill", Toast.LENGTH_SHORT).show();
        }
    }

    public static class RefillMedicationItem {
        public Medication medication;
        public String supplyStatus; // "good", "medium", "low"
        public int supplyPercentage;
        public String lastRefillText;

        public RefillMedicationItem(Medication medication) {
            this.medication = medication;
            calculateSupplyStatus();
            formatLastRefillText();
        }

        private void calculateSupplyStatus() {
            int dailyDoses = medication.getDailyDoseCount();
            if (dailyDoses == 0) {
                supplyStatus = "good";
                supplyPercentage = 100;
                return;
            }

            // Calculate based on 30-day supply
            int totalNeeded = dailyDoses * 30;
            supplyPercentage = totalNeeded > 0 ? (medication.getCurrentSupply() * 100) / totalNeeded : 0;

            if (supplyPercentage >= 70) {
                supplyStatus = "good";
            } else if (supplyPercentage >= 30) {
                supplyStatus = "medium";
            } else {
                supplyStatus = "low";
            }
        }

        private void formatLastRefillText() {
            if (medication.getLastRefillDate() != null) {
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                lastRefillText = "Last refill: " + format.format(medication.getLastRefillDate());
            } else {
                lastRefillText = "No refill recorded";
            }
        }
    }
}
