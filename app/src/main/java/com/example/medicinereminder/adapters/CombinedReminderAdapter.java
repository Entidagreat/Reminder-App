package com.example.medicinereminder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinereminder.R;
import com.example.medicinereminder.models.EducationalReminder;
import com.example.medicinereminder.models.Medication;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CombinedReminderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_MEDICATION = 0;
    private static final int VIEW_TYPE_EDUCATIONAL = 1;
    
    private List<Object> items = new ArrayList<>();
    private Consumer<Medication> medicationClickListener;
    private Consumer<EducationalReminder> educationalClickListener;
    
    public CombinedReminderAdapter(
            List<Medication> medications, 
            List<EducationalReminder> educationalReminders,
            Consumer<Medication> medicationClickListener,
            Consumer<EducationalReminder> educationalClickListener) {
        
        this.medicationClickListener = medicationClickListener;
        this.educationalClickListener = educationalClickListener;
        
        // Add all items to combined list
        if (medications != null) {
            items.addAll(medications);
        }
        
        if (educationalReminders != null) {
            items.addAll(educationalReminders);
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Medication) {
            return VIEW_TYPE_MEDICATION;
        } else {
            return VIEW_TYPE_EDUCATIONAL;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == VIEW_TYPE_MEDICATION) {
            View view = inflater.inflate(R.layout.item_medication, parent, false);
            return new MedicationViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_educational_reminder, parent, false);
            return new EducationalViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_MEDICATION) {
            Medication medication = (Medication) items.get(position);
            MedicationViewHolder viewHolder = (MedicationViewHolder) holder;
            
            viewHolder.titleText.setText(medication.getName());
            viewHolder.subtitleText.setText(medication.getDosage());
            
            // Set reminder time if available
            List<String> reminderTimes = medication.getReminderTimes();
            if (reminderTimes != null && !reminderTimes.isEmpty()) {
                viewHolder.timeText.setText("Next: " + reminderTimes.get(0));
            } else {
                viewHolder.timeText.setText("No time set");
            }
            
            // Set up click listener
            viewHolder.takeButton.setOnClickListener(v -> {
                medicationClickListener.accept(medication);
            });
        } else {
            EducationalReminder reminder = (EducationalReminder) items.get(position);
            EducationalViewHolder viewHolder = (EducationalViewHolder) holder;
            
            viewHolder.titleText.setText(reminder.getTitle());
            viewHolder.typeText.setText(reminder.getTypeLabel());
            
            // Format date and time
            if (reminder.getReminderDate() != null) {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                viewHolder.dateText.setText(dateFormat.format(reminder.getReminderDate()));
            }
            
            viewHolder.timeText.setText(reminder.getReminderTime());
            
            // Set up click listener
            viewHolder.itemView.setOnClickListener(v -> {
                educationalClickListener.accept(reminder);
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void updateItems(List<Medication> medications, List<EducationalReminder> educationalReminders) {
        items.clear();
        
        if (medications != null) {
            items.addAll(medications);
        }
        
        if (educationalReminders != null) {
            items.addAll(educationalReminders);
        }
        
        notifyDataSetChanged();
    }
    
    static class MedicationViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView subtitleText;
        TextView timeText;
        ImageView icon;
        android.widget.Button takeButton;
        
        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.medicationName);
            subtitleText = itemView.findViewById(R.id.dosageText); // Fixed - was incorrectly using medicationDosage
            timeText = itemView.findViewById(R.id.timeText);
//            icon = itemView.findViewById(R.id.medicationIcon);
            takeButton = itemView.findViewById(R.id.takeButton);
        }
    }
    
    static class EducationalViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView typeText;
        TextView dateText;
        TextView timeText;
        
        public EducationalViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.reminderTitle);
            typeText = itemView.findViewById(R.id.reminderType);
            dateText = itemView.findViewById(R.id.reminderDate);
            timeText = itemView.findViewById(R.id.reminderTime);
        }
    }
}