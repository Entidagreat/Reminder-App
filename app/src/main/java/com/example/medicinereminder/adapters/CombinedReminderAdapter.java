package com.example.medicinereminder.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinereminder.MedicationDetailActivity;
import com.example.medicinereminder.R;
import com.example.medicinereminder.models.Medication;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public class CombinedReminderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
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
    
    private static final int VIEW_TYPE_MEDICATION = 0;

    private List<Object> items = new ArrayList<>();
    private Consumer<Medication> medicationClickListener;
    private OnDoseTakenListener onDoseTakenListener;
    
    public CombinedReminderAdapter(
            List<Medication> medications,
            Consumer<Medication> medicationClickListener,
            OnDoseTakenListener onDoseTakenListener) {
        
        this.medicationClickListener = medicationClickListener;
        this.onDoseTakenListener = onDoseTakenListener;
        
        // Add all items to combined list
        if (medications != null) {
            items.addAll(medications);
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_MEDICATION;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//
//        if (viewType == VIEW_TYPE_MEDICATION) {
//            View view = inflater.inflate(R.layout.item_medication, parent, false);
//            return new MedicationViewHolder(view);
//        }
        View view = inflater.inflate(R.layout.item_medication, parent, false);
        return new MedicationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_MEDICATION) {
            Medication medication = (Medication) items.get(position);
            MedicationViewHolder viewHolder = (MedicationViewHolder) holder;
            viewHolder.titleText.setText(medication.getName());
            viewHolder.subtitleText.setText(medication.getDosage());
            viewHolder.itemView.setOnClickListener(v -> {
                Context context = viewHolder.itemView.getContext();
                Intent intent = new Intent(context, MedicationDetailActivity.class);
                intent.putExtra("medicationId", medication.getId());
//                intent.putExtra("name", medication.getName());
//                intent.putExtra("dosage", medication.getDosage());
//                intent.putExtra("frequency", medication.getFrequency());
//                intent.putExtra("duration", medication.getDuration());
//                intent.putExtra("start_date", medication.getStartDate().getTime());
//                intent.putExtra("end_date", medication.getEndDate() != null ? medication.getEndDate().getTime() : -1);
//                intent.putExtra("is_reminder", medication.getReminderEnabled());
//                intent.putStringArrayListExtra("reminderTimes", new ArrayList<>(medication.getReminderTimes()));
//                Log.d("MedicationCombine", "ID = " + medication.getId() + ", Name = " + medication.getName()
//                        + ", Dosage = " + medication.getName() + ", Frequency = " + medication.getFrequency()
//                        + ", Duration = " + medication.getDuration() + ", startDate = " + medication.getStartDate()
//                        + ", endDate = " + medication.getEndDate() + ", is_reminder = " + medication.getReminderEnabled());
                context.startActivity(intent);
            });
            // Xóa dòng này vì dosesLayout không còn tồn tại, đã thay bằng dosesRowLayout phía dưới
            List<String> reminderTimes = medication.getReminderTimes();
            // Hiển thị 1 hàng ngang các khung giờ và 1 nút "Uống" duy nhất
            LinearLayout dosesRowLayout = ((LinearLayout) ((ViewGroup) holder.itemView).findViewById(R.id.dosesRowLayout));
            Button takeDoseButton = ((Button) ((ViewGroup) holder.itemView).findViewById(R.id.takeDoseButton));
            dosesRowLayout.removeAllViews();
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

                            // --- Đồng bộ trạng thái vào database (DoseHistory), kiểm tra trùng trước khi thêm ---
                            try {
                                android.content.Context context = dosesRowLayout.getContext();
                                com.example.medicinereminder.utils.DatabaseHelper dbHelper = new com.example.medicinereminder.utils.DatabaseHelper(context);
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                                java.util.Date today = new java.util.Date();
                                String todayStr = sdf.format(today);
                                java.text.SimpleDateFormat fullFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                                java.util.Date scheduledDateTime = fullFormat.parse(todayStr + " " + time);

                                // Kiểm tra đã có DoseHistory cho thuốc + ngày + giờ này chưa
                                java.util.List<com.example.medicinereminder.models.DoseHistory> allToday = dbHelper.getTodaysDoseHistory();
                                com.example.medicinereminder.models.DoseHistory found = null;
                                for (com.example.medicinereminder.models.DoseHistory dh : allToday) {
                                    if (dh.getMedicationId() == medication.getId() && dh.getScheduledTime() != null) {
                                        java.text.SimpleDateFormat cmpFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
                                        String cmp1 = cmpFormat.format(dh.getScheduledTime());
                                        String cmp2 = cmpFormat.format(scheduledDateTime);
                                        if (cmp1.equals(cmp2)) {
                                            found = dh;
                                            break;
                                        }
                                    }
                                }
                                if (found != null) {
                                    // Đã có, chỉ update trạng thái
                                    found.setStatus("taken");
                                    found.setTakenTime(new java.util.Date());
                                    dbHelper.updateDoseHistory(found);
                                } else {
                                    // Chưa có, thêm mới
                                    com.example.medicinereminder.models.DoseHistory newHistory = new com.example.medicinereminder.models.DoseHistory(
                                        medication.getId(),
                                        medication.getName(),
                                        medication.getDosage(),
                                        scheduledDateTime,
                                        "taken"
                                    );
                                    newHistory.setTakenTime(new java.util.Date());
                                    dbHelper.addDoseHistory(newHistory);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // --- END đồng bộ ---
                            break;
                        }
                    }
                    // Refresh lại giao diện
                    notifyItemChanged(position);
                    if (medicationClickListener != null) {
                        medicationClickListener.accept(medication);
                    }
                    // Gọi callback cập nhật tiến độ ngay
                    if (onDoseTakenListener != null) {
                        onDoseTakenListener.onDoseTaken();
                    }
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
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<Medication> medications) {
        items.clear();
        
        if (medications != null) {
            items.addAll(medications);
        }
        notifyDataSetChanged();
    }
    
    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView subtitleText;
        LinearLayout dosesRowLayout;
        Button takeDoseButton;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.medicationName);
            subtitleText = itemView.findViewById(R.id.dosageText);
            dosesRowLayout = itemView.findViewById(R.id.dosesRowLayout);
            takeDoseButton = itemView.findViewById(R.id.takeDoseButton);
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