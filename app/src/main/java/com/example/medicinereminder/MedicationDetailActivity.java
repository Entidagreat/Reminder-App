package com.example.medicinereminder;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

public class MedicationDetailActivity extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    TextView txtName, txtDosage, txtStartDate, txtEndDate, startDateText;
    ImageView backBtn;
    Button updateBtn, deleteBtn;
//    Switch reminderSwitch;
    int medicationId = -1, duration;
    String name, dosage, frequency;
    String selectedFrequency = "once";
    int selectedDuration = 7;
    long startDateMillis, endDateMillis;
    boolean isReminder;
    Date startDate, endDate;
    List<String> reminderTimes;
    private CardView onceCard, twiceCard, threeTimesCard, fourTimesCard, asNeededCard;
    private TextView onceText, twiceText, threeTimesText, fourTimesText, asNeededText;
    private CardView days7Card, days14Card, days30Card, days90Card, ongoingCard;
    private TextView days7Text, days14Text, days30Text, days90Text, ongoingText;
    private LinearLayout timeSlotLayout;
    private CheckBox timeMorning, timeNoon, timeAfternoon, timeEvening;
    private Date selectedStartDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_detail);
        databaseHelper = new DatabaseHelper(this);
        selectedStartDate = new Date(); // Default to today
        init();
        setUpListener();
        initMedicationDetailUI();
    }

    private void initMedicationDetailUI(){
        Medication medication;
        Intent intent = getIntent();
        if (intent != null) {
            medicationId = intent.getIntExtra("medicationId", -1);
            if(medicationId != -1){
                medication = databaseHelper.getMedication(medicationId);
                name = medication.getName();
                dosage = medication.getDosage();
                frequency = medication.getFrequency();
                duration = medication.getDuration();
                startDate = medication.getStartDate();
                endDate = medication.getEndDate();
                reminderTimes = medication.getReminderTimes();
                isReminder = medication.getReminderEnabled();
            }
//            name = intent.getStringExtra("name");
//            dosage = intent.getStringExtra("dosage");
//            frequency = intent.getStringExtra("frequency");
//            duration = intent.getIntExtra("duration", 0);
//            startDateMillis = intent.getLongExtra("start_date", -1);
//            startDate = (startDateMillis != -1) ? new Date(startDateMillis) : null;
//            endDateMillis = intent.getLongExtra("end_date", -1);
//            endDate = (endDateMillis != -1) ? new Date(endDateMillis) : null;
//            reminderTimes = intent.getStringArrayListExtra("reminderTimes");
//            isReminder = intent.getBooleanExtra("is_reminder", true);
            Log.d("MedicationDetail", "ID = " + medicationId + ", Name = " + name + ", Dosage = " + dosage
                    + ", Frequency = " + frequency + ", Duration = " + duration + ", startDate = "
                    + startDate + ", endDate = " + endDate + ", is_reminder = " + isReminder
                    +", startDateMillis" + startDateMillis + ", reminder" + reminderTimes);


            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            txtName.setText(name);
            txtDosage.setText(dosage);
            selectFrequency(frequency);
            selectDuration(duration);
            updateTimeSlotCheckboxState(frequency, reminderTimes);
            txtStartDate.setText(startDate != null ? sdf.format(startDate) : "");
            txtEndDate.setText(endDate != null ? sdf.format(endDate) : "");
            startDateText.setText("Chọn lại ngày bắt đầu");
//            reminderSwitch.setChecked(isReminder);
        }
    }

    private void init(){
        txtName = findViewById(R.id.txtName);
        txtDosage = findViewById(R.id.txtDosage);
        txtStartDate = findViewById(R.id.start_date);
        txtEndDate = findViewById(R.id.end_date);
        startDateText = findViewById(R.id.startDateText);

        // Frequency cards
        onceCard = findViewById(R.id.onceCard);
        twiceCard = findViewById(R.id.twiceCard);
        threeTimesCard = findViewById(R.id.threeTimesCard);
        fourTimesCard = findViewById(R.id.fourTimesCard);

        onceText = findViewById(R.id.onceText);
        twiceText = findViewById(R.id.twiceText);
        threeTimesText = findViewById(R.id.threeTimesText);
        fourTimesText = findViewById(R.id.fourTimesText);

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

        deleteBtn = findViewById(R.id.deleteMedicineButton);
        updateBtn = findViewById(R.id.updateMedicineButton);
        backBtn = findViewById(R.id.backButton);
    }

    private void setUpListener(){
        backBtn.setOnClickListener(v ->finish());
        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xoá")
                    .setMessage("Bạn có chắc muốn xoá đơn thuốc này không?")
                    .setPositiveButton("Xoá", (dialog, which) -> {
                        if (medicationId != -1) {
                            DatabaseHelper db = new DatabaseHelper(this);
                            db.deleteMedication(medicationId);
                            Toast.makeText(this, "Đã xoá thuốc", Toast.LENGTH_SHORT).show();
                            finish(); // quay về màn hình trước
                        } else {
                            Toast.makeText(this, "Không tìm thấy ID thuốc để xoá", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
        updateBtn.setOnClickListener(v -> update());
        startDateText.setOnClickListener(v -> showDatePicker());

        // Frequency selection
        onceCard.setOnClickListener(v -> selectFrequency("once"));
        twiceCard.setOnClickListener(v -> selectFrequency("twice"));
        threeTimesCard.setOnClickListener(v -> selectFrequency("three_times"));
        fourTimesCard.setOnClickListener(v -> selectFrequency("four_times"));

        // Duration selection
        days7Card.setOnClickListener(v -> selectDuration(7));
        days14Card.setOnClickListener(v -> selectDuration(14));
        days30Card.setOnClickListener(v -> selectDuration(30));
        days90Card.setOnClickListener(v -> selectDuration(90));
        ongoingCard.setOnClickListener(v -> selectDuration(-1)); // -1 for ongoing
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
//            case "as_needed":
//                asNeededCard.setCardBackgroundColor(getResources().getColor(R.color.primary_green));
//                asNeededText.setTextColor(getResources().getColor(R.color.white));
//                hideTimeSlotLayout();
//                break;
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

    private void resetFrequencyCards() {
        int defaultColor = getResources().getColor(R.color.card_background);
        int defaultTextColor = getResources().getColor(R.color.text_primary);

        onceCard.setCardBackgroundColor(defaultColor);
        twiceCard.setCardBackgroundColor(defaultColor);
        threeTimesCard.setCardBackgroundColor(defaultColor);
        fourTimesCard.setCardBackgroundColor(defaultColor);
//        asNeededCard.setCardBackgroundColor(defaultColor);

        onceText.setTextColor(defaultTextColor);
        twiceText.setTextColor(defaultTextColor);
        threeTimesText.setTextColor(defaultTextColor);
        fourTimesText.setTextColor(defaultTextColor);
//        asNeededText.setTextColor(defaultTextColor);
    }

    private void selectDuration(int duration) {
        selectedDuration = duration;
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
        updateEndDate();
    }

    private void updateEndDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (selectedStartDate != null && selectedDuration > 0) {
            long endTime = selectedStartDate.getTime() + (selectedDuration * 24L * 60L * 60L * 1000L);
            endDate = new Date(endTime);
            txtEndDate.setText(sdf.format(endDate));
        } else {
            txtEndDate.setText("");
        }
        Log.d("updateEndDate", "End date updated: " + endDate);
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

    private void updateTimeSlotCheckboxState(String frequency, List<String> reminderTimes) {
        if (frequency.equals("once") || frequency.equals("twice") || frequency.equals("three_times")) {
            timeSlotLayout.setVisibility(View.VISIBLE);
        } else {
            timeSlotLayout.setVisibility(View.GONE);
            return;
        }

        // Reset checkbox
        timeMorning.setChecked(false);
        timeNoon.setChecked(false);
        timeAfternoon.setChecked(false);
        timeEvening.setChecked(false);

        if (reminderTimes != null) {
            for (String time : reminderTimes) {
                switch (time) {
                    case "08:00": timeMorning.setChecked(true); break;
                    case "11:00": timeNoon.setChecked(true); break;
                    case "17:00": timeAfternoon.setChecked(true); break;
                    case "20:00": timeEvening.setChecked(true); break;
                }
            }
        }
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
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    txtStartDate.setText(selectedStartDate != null ? sdf.format(selectedStartDate) : "");
                    if (startDate != null && duration > 0) {
                        long endTime = startDate.getTime() + (duration * 24L * 60L * 60L * 1000L);
                        endDate = new Date(endTime);
                    }
                    txtEndDate.setText(endDate != null ? sdf.format(endDate) : "");
                    startDateText.setText(getString(R.string.starts, dateFormat.format(selectedStartDate)));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void update(){
        String updatedName = txtName.getText().toString().trim();
        String updatedDosage = txtDosage.getText().toString().trim();

        if (updatedName.isEmpty() || updatedDosage.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên và liều lượng thuốc", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> updatedReminderTimes = new ArrayList<>();
        if (timeMorning.isChecked()) updatedReminderTimes.add("08:00");
        if (timeNoon.isChecked()) updatedReminderTimes.add("11:00");
        if (timeAfternoon.isChecked()) updatedReminderTimes.add("17:00");
        if (timeEvening.isChecked()) updatedReminderTimes.add("20:00");

        Medication updatedMedication = new Medication();
        updatedMedication.setId(medicationId);
        updatedMedication.setName(updatedName);
        updatedMedication.setDosage(updatedDosage);
        updatedMedication.setFrequency(selectedFrequency);
        updatedMedication.setDuration(selectedDuration);
        updatedMedication.setStartDate(selectedStartDate);
        updatedMedication.calculateEndDate(); // sẽ tự tính endDate từ startDate + duration
        updatedMedication.setEndDate(updatedMedication.getEndDate());
//        updatedMedication.setReminderEnabled(reminderSwitch.isChecked());
        updatedMedication.setReminderTimes(updatedReminderTimes);

        int result = databaseHelper.updateMedication(updatedMedication);
        if (result > 0) {
            Toast.makeText(this, "Cập nhật thuốc thành công", Toast.LENGTH_SHORT).show();
            finish(); // hoặc chuyển về trang trước đó
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
