package com.example.medicinereminder.adapters;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.R;
import com.example.medicinereminder.models.DoseHistory;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<DoseHistory> historyList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public HistoryAdapter(List<DoseHistory> historyList) {
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        DoseHistory history = historyList.get(position);
        
        holder.medicationName.setText(history.getMedicationName());
        holder.dosageText.setText(history.getDosage());
        
        // Định dạng ngày tháng bằng tiếng Việt
        SimpleDateFormat vietnameseFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        String formattedDate = vietnameseFormat.format(history.getScheduledTime());
        holder.dateText.setText(formattedDate);
        
        // Hiển thị trạng thái bằng tiếng Việt
        String statusText = "";
        int statusColor = 0;
        
        switch (history.getStatus()) {
            case "taken":
                statusText = "Đã uống";
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_taken);
                break;
            case "missed":
                statusText = "Bỏ lỡ";
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_missed);
                break;
            case "skipped":
                statusText = "Bỏ qua";
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.status_skipped);
                break;
            default:
                statusText = "Chưa xác định";
                statusColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary);
                break;
        }
        
        holder.statusText.setText(statusText);
        holder.statusText.setTextColor(statusColor);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public void updateHistory(List<DoseHistory> newHistory) {
        this.historyList = newHistory;
        notifyDataSetChanged();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView medicationName;
        private TextView dosageText;
        private TextView dateText;
        private TextView timeText;
        private TextView statusText;
        private View statusIndicator;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.historyCard);
            medicationName = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.dosageText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
            statusText = itemView.findViewById(R.id.statusText);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(DoseHistory history) {
            medicationName.setText(history.getMedicationName());
            dosageText.setText(history.getDosage());
            dateText.setText(dateFormat.format(history.getScheduledTime()));
            timeText.setText(timeFormat.format(history.getScheduledTime()));
            statusText.setText(history.getDisplayStatus());

            // Set status indicator color
            int statusColor = Color.parseColor(history.getStatusColor());
            statusIndicator.setBackgroundColor(statusColor);
            statusText.setTextColor(statusColor);

            // Add taken time if available
            if (history.getTakenTime() != null && history.isTaken()) {
                String takenTimeStr = timeFormat.format(history.getTakenTime());
                timeText.setText(timeFormat.format(history.getScheduledTime()) + " → " + takenTimeStr);
            }
        }
    }
}