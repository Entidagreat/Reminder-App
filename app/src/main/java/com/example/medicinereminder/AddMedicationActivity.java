package com.example.medicinereminder;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CheckBox;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.medicinereminder.models.Medication;
import com.example.medicinereminder.utils.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddMedicationActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    private EditText medicationNameEdit;
    private EditText dosageEdit;
    private TextView startDateText;
    private Switch reminderSwitch;
    private Switch refillSwitch;
    private EditText currentSupplyEdit;
    private SeekBar refillThresholdSeeker;
    private TextView refillThresholdText;
    private LinearLayout refillLayout;
    private Button addMedicationButton;

    // Frequency selection
    private CardView onceCard, twiceCard, threeTimesCard, fourTimesCard, asNeededCard;
    private TextView onceText, twiceText, threeTimesText, fourTimesText, asNeededText;

    // Duration selection
    private CardView days7Card, days14Card, days30Card, days90Card, ongoingCard;
    private TextView days7Text, days14Text, days30Text, days90Text, ongoingText;

    // Time slot selection
    private LinearLayout timeSlotLayout;
    private CheckBox timeMorning, timeNoon, timeAfternoon, timeEvening;

    private String selectedFrequency = "once";
    private int selectedDuration = 7;
    private Date selectedStartDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        dbHelper = new DatabaseHelper(this);
        selectedStartDate = new Date(); // Default to today

        initViews();
        setupClickListeners();
        setupSwitches();
        updateUI();
    }

    private void initViews() {
        medicationNameEdit = findViewById(R.id.medicationNameEdit);
        dosageEdit = findViewById(R.id.dosageEdit);
        startDateText = findViewById(R.id.startDateText);
        reminderSwitch = findViewById(R.id.reminderSwitch);
        refillSwitch = findViewById(R.id.refillSwitch);
        currentSupplyEdit = findViewById(R.id.currentSupplyEdit);
        refillThresholdSeeker = findViewById(R.id.refillThresholdSeeker);
        refillThresholdText = findViewById(R.id.refillThresholdText);
        refillLayout = findViewById(R.id.refillLayout);
        addMedicationButton = findViewById(R.id.addMedicationButton);

        // Frequency cards
        onceCard = findViewById(R.id.onceCard);
        twiceCard = findViewById(R.id.twiceCard);
        threeTimesCard = findViewById(R.id.threeTimesCard);
        fourTimesCard = findViewById(R.id.fourTimesCard);
        asNeededCard = findViewById(R.id.asNeededCard);

        onceText = findViewById(R.id.onceText);
        twiceText = findViewById(R.id.twiceText);
        threeTimesText = findViewById(R.id.threeTimesText);
        fourTimesText = findViewById(R.id.fourTimesText);
        asNeededText = findViewById(R.id.asNeededText);

        // Duration cards
        days7Card = findViewById(R.id.days7Card);
        days14Card = findViewById(R.id.days14Card);
        days30Card = findViewById(R.id.days30Card);
        days90Card = findViewById(R.id.days90Card);
        ongoingCard = findViewById(R.id.ongoingCard);

        days7Text = findViewById(R.id.days7Text);
        days14Text = findViewById(R.id.days14Text);
        days30Text = findViewById(R.id.days30Text);
        days90Text = findViewById(R.id.days90Text);
        ongoingText = findViewById(R.id.ongoingText);
        // Time slot selection
        timeSlotLayout = findViewById(R.id.timeSlotLayout);
        timeMorning = findViewById(R.id.timeMorning);
        timeNoon = findViewById(R.id.timeNoon);
        timeAfternoon = findViewById(R.id.timeAfternoon);
        timeEvening = findViewById(R.id.timeEvening);
        hideTimeSlotLayout();
    }

    private void setupClickListeners() {
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        startDateText.setOnClickListener(v -> showDatePicker());

        // Frequency selection
        onceCard.setOnClickListener(v -> selectFrequency("once"));
        twiceCard.setOnClickListener(v -> selectFrequency("twice"));
        threeTimesCard.setOnClickListener(v -> selectFrequency("three_times"));
        fourTimesCard.setOnClickListener(v -> selectFrequency("four_times"));
        asNeededCard.setOnClickListener(v -> selectFrequency("as_needed"));

        // Duration selection
        days7Card.setOnClickListener(v -> selectDuration(7));
        days14Card.setOnClickListener(v -> selectDuration(14));
        days30Card.setOnClickListener(v -> selectDuration(30));
        days90Card.setOnClickListener(v -> selectDuration(90));
        ongoingCard.setOnClickListener(v -> selectDuration(-1)); // -1 for ongoing

        addMedicationButton.setOnClickListener(v -> addMedication());

        refillThresholdSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refillThresholdText.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupSwitches() {
        reminderSwitch.setChecked(true);
        refillSwitch.setChecked(false);

        refillSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refillLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void updateUI() {
        startDateText.setText(getString(R.string.starts, dateFormat.format(selectedStartDate)));
        selectFrequency(selectedFrequency);
        selectDuration(selectedDuration);
        refillThresholdText.setText(refillThresholdSeeker.getProgress() + "%");
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedStartDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedStartDate = selectedCalendar.getTime();
                    startDateText.setText(getString(R.string.starts, dateFormat.format(selectedStartDate)));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void selectFrequency(String frequency) {
        selectedFrequency = frequency;
        resetFrequencyCards();
        hideTimeSlotLayout();
        switch (frequency) {
            case "once":
                onceCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                onceText.setTextColor(getResources().getColor(R.color.white));
                showTimeSlotLayout(1);
                break;
            case "twice":
                twiceCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                twiceText.setTextColor(getResources().getColor(R.color.white));
                showTimeSlotLayout(2);
                break;
            case "three_times":
                threeTimesCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                threeTimesText.setTextColor(getResources().getColor(R.color.white));
                // Mặc định chọn sáng, trưa, chiều
                showTimeSlotLayout(3);
                break;
            case "four_times":
                fourTimesCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                fourTimesText.setTextColor(getResources().getColor(R.color.white));
                hideTimeSlotLayout();
                break;
            case "as_needed":
                asNeededCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                asNeededText.setTextColor(getResources().getColor(R.color.white));
                hideTimeSlotLayout();
                break;
        }
    }

    private void showTimeSlotLayout(int count) {
        timeSlotLayout.setVisibility(View.VISIBLE);
        // Reset all
        timeMorning.setChecked(false);
        timeNoon.setChecked(false);
        timeAfternoon.setChecked(false);
        timeEvening.setChecked(false);
        timeMorning.setEnabled(true);
        timeNoon.setEnabled(true);
        timeAfternoon.setEnabled(true);
        timeEvening.setEnabled(true);
        if (count == 3) {
            // Mặc định chọn sáng, trưa, chiều
            timeMorning.setChecked(true);
            timeNoon.setChecked(true);
            timeAfternoon.setChecked(true);
            timeMorning.setEnabled(false);
            timeNoon.setEnabled(false);
            timeAfternoon.setEnabled(false);
            timeEvening.setEnabled(false);
        }
    }

    private void hideTimeSlotLayout() {
        timeSlotLayout.setVisibility(View.GONE);
    }

    private void selectDuration(int duration) {
        selectedDuration = duration;

        // Reset all cards
        resetDurationCards();

        // Highlight selected card
        switch (duration) {
            case 7:
                days7Card.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                days7Text.setTextColor(getResources().getColor(R.color.white));
                break;
            case 14:
                days14Card.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                days14Text.setTextColor(getResources().getColor(R.color.white));
                break;
            case 30:
                days30Card.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                days30Text.setTextColor(getResources().getColor(R.color.white));
                break;
            case 90:
                days90Card.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                days90Text.setTextColor(getResources().getColor(R.color.white));
                break;
            case -1:
                ongoingCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
                ongoingText.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    private void resetFrequencyCards() {
        int defaultColor = getResources().getColor(R.color.card_background);
        int defaultTextColor = getResources().getColor(R.color.text_primary);

        onceCard.setCardBackgroundColor(defaultColor);
        twiceCard.setCardBackgroundColor(defaultColor);
        threeTimesCard.setCardBackgroundColor(defaultColor);
        fourTimesCard.setCardBackgroundColor(defaultColor);
        asNeededCard.setCardBackgroundColor(defaultColor);

        onceText.setTextColor(defaultTextColor);
        twiceText.setTextColor(defaultTextColor);
        threeTimesText.setTextColor(defaultTextColor);
        fourTimesText.setTextColor(defaultTextColor);
        asNeededText.setTextColor(defaultTextColor);
    }

    private void resetDurationCards() {
        int defaultColor = getResources().getColor(R.color.card_background);
        int defaultTextColor = getResources().getColor(R.color.text_primary);

        days7Card.setCardBackgroundColor(defaultColor);
        days14Card.setCardBackgroundColor(defaultColor);
        days30Card.setCardBackgroundColor(defaultColor);
        days90Card.setCardBackgroundColor(defaultColor);
        ongoingCard.setCardBackgroundColor(defaultColor);

        days7Text.setTextColor(defaultTextColor);
        days14Text.setTextColor(defaultTextColor);
        days30Text.setTextColor(defaultTextColor);
        days90Text.setTextColor(defaultTextColor);
        ongoingText.setTextColor(defaultTextColor);
    }

    private void addMedication() {
        String name = medicationNameEdit.getText().toString().trim();
        String dosage = dosageEdit.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            medicationNameEdit.setError("Please enter medication name");
            return;
        }

        if (TextUtils.isEmpty(dosage)) {
            dosageEdit.setError("Please enter dosage");
            return;
        }

        // Create medication object
        Medication medication = new Medication(name, dosage, selectedFrequency, selectedDuration, selectedStartDate);
        medication.setReminderEnabled(reminderSwitch.isChecked());
        medication.setRefillTrackingEnabled(refillSwitch.isChecked());

        if (refillSwitch.isChecked()) {
            String currentSupplyStr = currentSupplyEdit.getText().toString().trim();
            if (!TextUtils.isEmpty(currentSupplyStr)) {
                medication.setCurrentSupply(Integer.parseInt(currentSupplyStr));
            }
            medication.setRefillThreshold(refillThresholdSeeker.getProgress());
        }

        // Set reminder times theo lựa chọn
        if (reminderSwitch.isChecked()) {
            List<String> times = new ArrayList<>();
            if (selectedFrequency.equals("once")) {
                if (timeMorning.isChecked()) times.add("08:00");
                if (timeNoon.isChecked()) times.add("11:00");
                if (timeAfternoon.isChecked()) times.add("17:00");
                if (timeEvening.isChecked()) times.add("20:00");
                if (times.size() != 1) {
                    Toast.makeText(this, "Chọn đúng 1 khung giờ", Toast.LENGTH_SHORT).show();
                    addMedicationButton.setEnabled(true);
                    addMedicationButton.setText(getString(R.string.add_medication_button));
                    return;
                }
            } else if (selectedFrequency.equals("twice")) {
                if (timeMorning.isChecked()) times.add("08:00");
                if (timeNoon.isChecked()) times.add("11:00");
                if (timeAfternoon.isChecked()) times.add("17:00");
                if (timeEvening.isChecked()) times.add("20:00");
                if (times.size() != 2) {
                    Toast.makeText(this, "Chọn đúng 2 khung giờ", Toast.LENGTH_SHORT).show();
                    addMedicationButton.setEnabled(true);
                    addMedicationButton.setText(getString(R.string.add_medication_button));
                    return;
                }
            } else if (selectedFrequency.equals("three_times")) {
                times.add("08:00");
                times.add("11:00");
                times.add("17:00");
            } else if (selectedFrequency.equals("four_times")) {
                times.add("08:00");
                times.add("11:00");
                times.add("17:00");
                times.add("20:00");
            }
            medication.setReminderTimes(times);
        }

        // Save to database
        addMedicationButton.setText(getString(R.string.adding));
        addMedicationButton.setEnabled(false);

        long result = dbHelper.addMedication(medication);

        if (result != -1) {
            Toast.makeText(this, "Medication added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add medication", Toast.LENGTH_SHORT).show();
            addMedicationButton.setText(getString(R.string.add_medication_button));
            addMedicationButton.setEnabled(true);
        }
    }
}