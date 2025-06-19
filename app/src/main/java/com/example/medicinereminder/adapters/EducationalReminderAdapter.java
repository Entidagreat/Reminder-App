package com.example.medicinereminder.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicinereminder.R;
import com.example.medicinereminder.models.EducationalReminder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EducationalReminderAdapter extends RecyclerView.Adapter<EducationalReminderAdapter.ViewHolder> {
    
    private List<EducationalReminder> reminderList;
    private Context context;
    private OnReminderClickListener reminderClickListener;
    private OnCompletionToggleListener completionToggleListener;
    
    public interface OnReminderClickListener {
        void onReminderClick(EducationalReminder reminder);
    }
    
    public interface OnCompletionToggleListener {
        void onCompletionToggled(EducationalReminder reminder, boolean isCompleted);
    }
    
    public EducationalReminderAdapter(List<EducationalReminder> reminderList, 
                                     OnReminderClickListener reminderClickListener,
                                     OnCompletionToggleListener completionToggleListener) {
        this.reminderList = reminderList;
        this.reminderClickListener = reminderClickListener;
        this.completionToggleListener = completionToggleListener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_educational_reminder, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EducationalReminder reminder = reminderList.get(position);
        
        holder.titleText.setText(reminder.getTitle());
        holder.typeText.setText(reminder.getTypeLabel());
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(reminder.getReminderDate());
        holder.dateText.setText(dateStr);
        holder.timeText.setText(reminder.getReminderTime());
        
        // Set background color based on priority
        int priorityColor;
        switch (reminder.getPriority()) {
            case 1: // High
                priorityColor = R.color.priority_high;
                break;
            case 3: // Low
                priorityColor = R.color.priority_low;
                break;
            default: // Medium (2) or any other value
                priorityColor = R.color.priority_medium;
        }
        holder.priorityIndicator.setBackgroundColor(ContextCompat.getColor(context, priorityColor));
        
        // Set icon based on type
        int iconResource;
        switch (reminder.getType()) {
            case "study_session":
                iconResource = R.drawable.ic_study;
                break;
            case "assignment":
                iconResource = R.drawable.ic_assignment;
                break;
            case "exam":
                iconResource = R.drawable.ic_exam;
                break;
            case "language_practice":
                iconResource = R.drawable.ic_language;
                break;
            default:
                iconResource = R.drawable.ic_education;
        }
        holder.typeIcon.setImageResource(iconResource);
        
        // Set completion status
        holder.completionCheckbox.setChecked(reminder.isCompleted());
        if (reminder.isCompleted()) {
            holder.cardView.setAlpha(0.6f);
        } else {
            holder.cardView.setAlpha(1.0f);
        }
        
        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (reminderClickListener != null) {
                reminderClickListener.onReminderClick(reminder);
            }
        });
        
        holder.completionCheckbox.setOnClickListener(v -> {
            boolean isChecked = holder.completionCheckbox.isChecked();
            if (completionToggleListener != null) {
                completionToggleListener.onCompletionToggled(reminder, isChecked);
            }
            reminder.setCompleted(isChecked);
            holder.cardView.setAlpha(isChecked ? 0.6f : 1.0f);
        });
    }
    
    @Override
    public int getItemCount() {
        return reminderList.size();
    }
    
    public void updateReminders(List<EducationalReminder> newReminderList) {
        this.reminderList = newReminderList;
        notifyDataSetChanged();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleText;
        TextView typeText;
        TextView dateText;
        TextView timeText;
        ImageView typeIcon;
        View priorityIndicator;
        CheckBox completionCheckbox;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.reminderCardView);
            titleText = itemView.findViewById(R.id.reminderTitle);
            typeText = itemView.findViewById(R.id.reminderType);
            dateText = itemView.findViewById(R.id.reminderDate);
            timeText = itemView.findViewById(R.id.reminderTime);
            typeIcon = itemView.findViewById(R.id.reminderTypeIcon);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            completionCheckbox = itemView.findViewById(R.id.completionCheckbox);
        }
    }
}
