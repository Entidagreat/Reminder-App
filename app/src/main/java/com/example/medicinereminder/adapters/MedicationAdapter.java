package com.example.medicinereminder.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.medicinereminder.R;
import com.example.medicinereminder.models.Medication;
import java.util.List;

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.MedicationViewHolder> {
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
        private TextView timeText;
        private Button takeButton;

        public MedicationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.medicationCard);
            medicationName = itemView.findViewById(R.id.medicationName);
            dosageText = itemView.findViewById(R.id.dosageText);
            timeText = itemView.findViewById(R.id.timeText);
            takeButton = itemView.findViewById(R.id.takeButton);
        }

        public void bind(Medication medication) {
            medicationName.setText(medication.getName());
            dosageText.setText(medication.getDosage());

            // Set next dose time
            List<String> reminderTimes = medication.getReminderTimes();
            if (reminderTimes != null && !reminderTimes.isEmpty()) {
                timeText.setText("Next: " + reminderTimes.get(0));
            } else {
                timeText.setText("No time set");
            }

            takeButton.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMedicationClick(medication);
                }
            });
        }
    }
}