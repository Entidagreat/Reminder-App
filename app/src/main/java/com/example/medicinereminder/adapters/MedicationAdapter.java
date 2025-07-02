package com.example.medicinereminder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.R;
import com.example.medicinereminder.models.Medication;
import java.util.List;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
    // Helper: lưu trạng thái đã uống cho từng liều theo ngày, thuốc, giờ
    private boolean isDoseTaken(View view, int medicationId, String doseTime) {
        String key = getDoseKey(medicationId, doseTime);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        String today = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(new java.util.Date());
        return prefs.getBoolean(key + today, false);
    }

    private void setDoseTaken(View view, int medicationId, String doseTime) {
        String key = getDoseKey(medicationId, doseTime);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        String today = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault()).format(new java.util.Date());
        prefs.edit().putBoolean(key + today, true).apply();
    }

    private String getDoseKey(int medicationId, String doseTime) {
        return "dose_taken_" + medicationId + "_" + doseTime.replace(":", "");
    }
    private List<Medication> medications;
    private OnMedicationClickListener clickListener;

    public interface OnMedicationClickListener {
        void onMedicationClick(Medication medication);
    }

    public MedicationAdapter(List<Medication> medications, OnMedicationClickListener clickListener) {
        this.medications = medications;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public MedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicationViewHolder holder, int position) {
        Medication medication = medications.get(position);
        holder.bind(medication);
    }

    @Override
    public int getItemCount() {
        return medications.size();
    }

    public void updateMedications(List<Medication> newMedications) {
        this.medications = newMedications;
        notifyDataSetChanged();
    }

    class MedicationViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView medicationName;
        private TextView dosageText;
        private LinearLayout dosesRowLayout;
        private Button takeDoseButton;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.medicationCard);
            medicationName = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.dosageText);
            dosesRowLayout = itemView.findViewById(R.id.dosesRowLayout);
            takeDoseButton = itemView.findViewById(R.id.takeDoseButton);
        }

        public void bind(Medication medication) {
            medicationName.setText(medication.getName());
            dosageText.setText(medication.getDosage());
            dosesRowLayout.removeAllViews();
            List<String> reminderTimes = medication.getReminderTimes();
            boolean hasDoseToTake = false;
            boolean allTaken = true;
            if (reminderTimes != null && !reminderTimes.isEmpty()) {
                for (int i = 0; i < reminderTimes.size(); i++) {
                    String time = reminderTimes.get(i);
                    TextView tv = new TextView(dosesRowLayout.getContext());
                    tv.setText(time);
                    tv.setPadding(16, 8, 16, 8);
                    tv.setTextSize(14f);
                    boolean taken = isDoseTaken(dosesRowLayout, medication.getId(), time);
                    if (taken) {
                        tv.setTextColor(dosesRowLayout.getResources().getColor(R.color.primary_green));
                    } else {
                        tv.setTextColor(dosesRowLayout.getResources().getColor(R.color.text_primary));
                        allTaken = false;
                        if (shouldShowTakeButton(time, reminderTimes, i)) {
                            hasDoseToTake = true;
                        }
                    }
                    dosesRowLayout.addView(tv);
                }
            }
            // Nút "Uống" chỉ bấm được khi có ít nhất 1 liều đã đến giờ mà chưa uống
            takeDoseButton.setVisibility(View.VISIBLE);
            if (!allTaken && hasDoseToTake) {
                takeDoseButton.setEnabled(true);
                takeDoseButton.setAlpha(1f);
                takeDoseButton.setText("Uống");
                takeDoseButton.setOnClickListener(v -> {
                    // Chỉ đánh dấu 1 liều đầu tiên đã đến giờ mà chưa uống
                    for (int i = 0; i < reminderTimes.size(); i++) {
                        String time = reminderTimes.get(i);
                        if (!isDoseTaken(dosesRowLayout, medication.getId(), time) && shouldShowTakeButton(time, reminderTimes, i)) {
                            setDoseTaken(dosesRowLayout, medication.getId(), time);
                            break;
                        }
                    }
                    // Refresh lại giao diện
                    bind(medication);
                    // Đã loại bỏ gọi clickListener.onMedicationClick để tránh double ghi nhận lịch sử
                });
            } else if (allTaken) {
                takeDoseButton.setEnabled(false);
                takeDoseButton.setAlpha(0.5f);
                takeDoseButton.setText("Đã uống hết");
                takeDoseButton.setOnClickListener(null);
            } else {
                takeDoseButton.setEnabled(false);
                takeDoseButton.setAlpha(0.5f);
                takeDoseButton.setText("Uống");
                takeDoseButton.setOnClickListener(null);
            }
        }

        // Hiện nút "Uống" cho mọi liều chưa uống mà thời gian hiện tại >= giờ liều đó
        private boolean shouldShowTakeButton(String time, List<String> allTimes, int index) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.util.Calendar now = java.util.Calendar.getInstance();
            String nowStr = String.format("%02d:%02d", now.get(java.util.Calendar.HOUR_OF_DAY), now.get(java.util.Calendar.MINUTE));
            try {
                java.util.Date nowDate = sdf.parse(nowStr);
                java.util.Date doseDate = sdf.parse(time);
                if (nowDate == null || doseDate == null) return false;
                // Nếu đã qua giờ liều này (hoặc đúng giờ), chưa uống thì hiện nút
                return nowDate.compareTo(doseDate) >= 0;
            } catch (Exception e) {
                return false;
            }
        }
    }
}