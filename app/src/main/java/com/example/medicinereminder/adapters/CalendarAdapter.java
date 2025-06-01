package com.example.medicinereminder.adapters;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.CalendarActivity;
import com.example.medicinereminder.R;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private List<CalendarActivity.CalendarMedicationItem> items;
    private OnMedicationActionListener actionListener;

    public interface OnMedicationActionListener {
        void onMedicationAction(CalendarActivity.CalendarMedicationItem item, String action);
    }

    public CalendarAdapter(List<CalendarActivity.CalendarMedicationItem> items, OnMedicationActionListener actionListener) {
        this.items = items;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarActivity.CalendarMedicationItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<CalendarActivity.CalendarMedicationItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView medicationName;
        private TextView dosageText;
        private TextView timeText;
        private TextView statusText;
        private Button takeButton;
        private Button missButton;
        private View statusIndicator;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.calendarCard);
            medicationName = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.dosageText);
            timeText = itemView.findViewById(R.id.timeText);
            statusText = itemView.findViewById(R.id.statusText);
            takeButton = itemView.findViewById(R.id.takeButton);
            missButton = itemView.findViewById(R.id.missButton);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(CalendarActivity.CalendarMedicationItem item) {
            medicationName.setText(item.medication.getName());
            dosageText.setText(item.medication.getDosage());
            timeText.setText(item.scheduledTime);

            if (item.doseHistory != null) {
                // Dose has been recorded
                statusText.setText(item.doseHistory.getDisplayStatus());
                statusText.setVisibility(View.VISIBLE);

                int statusColor = Color.parseColor(item.doseHistory.getStatusColor());
                statusIndicator.setBackgroundColor(statusColor);
                statusText.setTextColor(statusColor);

                // Hide action buttons if already taken
                if (item.doseHistory.isTaken()) {
                    takeButton.setVisibility(View.GONE);
                    missButton.setVisibility(View.GONE);
                } else {
                    takeButton.setVisibility(View.VISIBLE);
                    missButton.setVisibility(View.VISIBLE);
                }
            } else {
                // Dose not recorded yet
                statusText.setVisibility(View.GONE);
                statusIndicator.setBackgroundColor(Color.GRAY);
                takeButton.setVisibility(View.VISIBLE);
                missButton.setVisibility(View.VISIBLE);
            }

            takeButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onMedicationAction(item, "taken");
                }
            });

            missButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onMedicationAction(item, "missed");
                }
            });
        }
    }
}