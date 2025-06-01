package com.example.medicinereminder.adapters;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.R;
import com.example.medicinereminder.RefillActivity;
import com.example.medicinereminder.models.Medication;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RefillAdapter extends RecyclerView.Adapter<RefillAdapter.RefillViewHolder> {
    private List<RefillActivity.RefillMedicationItem> items;
    private OnRefillClickListener clickListener;

    public interface OnRefillClickListener {
        void onRefillClick(Medication medication);
    }

    public RefillAdapter(List<Medication> medications, OnRefillClickListener clickListener) {
        this.items = new ArrayList<>();
        this.clickListener = clickListener;
        updateMedications(medications);
    }

    @NonNull
    @Override
    public RefillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_refill, parent, false);
        return new RefillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RefillViewHolder holder, int position) {
        RefillActivity.RefillMedicationItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void updateMedications(List<Medication> medications) {
        this.items.clear();
        for (Medication medication : medications) {
            this.items.add(new RefillActivity.RefillMedicationItem(medication));
        }
        notifyDataSetChanged();
    }

    class RefillViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView medicationName;
        private TextView dosageText;
        private TextView supplyText;
        private TextView statusText;
        private TextView lastRefillText;
        private TextView refillThresholdText;
        private ProgressBar supplyProgressBar;
        private Button recordRefillButton;
        private View statusIndicator;

        public RefillViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.refillCard);
            medicationName = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.dosageText);
            supplyText = itemView.findViewById(R.id.supplyText);
            statusText = itemView.findViewById(R.id.statusText);
            lastRefillText = itemView.findViewById(R.id.lastRefillText);
            refillThresholdText = itemView.findViewById(R.id.refillThresholdText);
            supplyProgressBar = itemView.findViewById(R.id.supplyProgressBar);
            recordRefillButton = itemView.findViewById(R.id.recordRefillButton);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }

        public void bind(RefillActivity.RefillMedicationItem item) {
            medicationName.setText(item.medication.getName());
            dosageText.setText(item.medication.getDosage());
            supplyText.setText("Supply: " + item.medication.getCurrentSupply());
            lastRefillText.setText(item.lastRefillText);
            refillThresholdText.setText("Refill at: " + item.medication.getRefillThreshold() + "%");

            // Set supply progress
            supplyProgressBar.setProgress(item.supplyPercentage);

            // Set status based on supply level
            statusText.setText(getStatusDisplayText(item.supplyStatus));

            int statusColor = getStatusColor(item.supplyStatus);
            statusIndicator.setBackgroundColor(statusColor);
            statusText.setTextColor(statusColor);

            // Set progress bar color
            supplyProgressBar.getProgressDrawable().setColorFilter(statusColor, android.graphics.PorterDuff.Mode.SRC_IN);

            recordRefillButton.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onRefillClick(item.medication);
                }
            });
        }

        private String getStatusDisplayText(String status) {
            switch (status) {
                case "good":
                    return "Good";
                case "medium":
                    return "Medium";
                case "low":
                    return "Low";
                default:
                    return "Unknown";
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "good":
                    return Color.parseColor("#4CAF50"); // Green
                case "medium":
                    return Color.parseColor("#FF9800"); // Orange
                case "low":
                    return Color.parseColor("#F44336"); // Red
                default:
                    return Color.parseColor("#9E9E9E"); // Gray
            }
        }
    }
}