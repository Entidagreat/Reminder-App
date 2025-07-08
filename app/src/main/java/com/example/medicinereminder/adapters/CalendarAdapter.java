package com.example.medicinereminder.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.medicinereminder.MedicationDetailActivity;
import com.example.medicinereminder.R;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private List<CalendarActivity.CalendarItem> items;
    private OnCalendarItemActionListener actionListener;

    public interface OnCalendarItemActionListener {
        void onCalendarItemAction(CalendarActivity.CalendarItem item, String action);
    }

    public CalendarAdapter(List<CalendarActivity.CalendarItem> items, OnCalendarItemActionListener actionListener) {
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
        CalendarActivity.CalendarItem item = items.get(position);

        if ("medication".equals(item.type)) {
            // Handle medication item
            holder.medicationName.setText(item.medication.getName());
            holder.dosageText.setText(item.medication.getDosage());
            holder.timeText.setText(item.scheduledTime);

            // Set up status UI based on doseHistory
            if (item.doseHistory != null) {
                // Dose has been recorded
                holder.statusText.setText(getVietnameseStatus(item.doseHistory.getStatus()));
                holder.statusText.setVisibility(View.VISIBLE);

                int statusColor = Color.parseColor(item.doseHistory.getStatusColor());
                holder.statusIndicator.setBackgroundColor(statusColor);
                holder.statusText.setTextColor(statusColor);
            } else {
                // Dose not recorded yet
                holder.statusText.setVisibility(View.GONE);
                holder.statusIndicator.setBackgroundColor(Color.GRAY);
            }
            // Đã loại bỏ nút hành động, không còn thao tác với takeButton/missButton
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateItems(List<CalendarActivity.CalendarItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView medicationName;
        private TextView dosageText;
        private TextView timeText;
        private TextView statusText;
        private View statusIndicator;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.calendarCard);
            medicationName = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.dosageText);
            timeText = itemView.findViewById(R.id.timeText);
            statusText = itemView.findViewById(R.id.statusText);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        CalendarActivity.CalendarItem clickedItem = items.get(position);
                        if ("medication".equals(clickedItem.type)) {
                            Context context = v.getContext();
                            Intent intent = new Intent(context, MedicationDetailActivity.class);
                            intent.putExtra("medicationId", clickedItem.medication.getId());
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }
    }
    private String getVietnameseStatus(String status) {
        switch (status) {
            case "taken":
                return "Đã uống";
            case "missed":
                return "Bỏ lỡ";
            case "skipped":
                return "Bỏ qua";
            default:
                return status;
        }
    }
}