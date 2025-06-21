package com.example.medicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AddReminderSelectionActivity extends AppCompatActivity {

    private CardView medicineReminderCard;
    private CardView educationalReminderCard;
    private CardView socialReminderCard;
    private CardView healthReminderCard;
    private CardView sleepReminderCard;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder_selection);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        medicineReminderCard = findViewById(R.id.medicineReminderCard);
        educationalReminderCard = findViewById(R.id.educationalReminderCard);
        socialReminderCard = findViewById(R.id.socialReminderCard);
        healthReminderCard = findViewById(R.id.healthReminderCard);
        sleepReminderCard = findViewById(R.id.sleepReminderCard);
        backButton = findViewById(R.id.backButton);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        medicineReminderCard.setOnClickListener(v -> {
            Intent intent = new Intent(AddReminderSelectionActivity.this, AddMedicationActivity.class);
            startActivity(intent);
        });
        
        educationalReminderCard.setOnClickListener(v -> {
            Intent intent = new Intent(AddReminderSelectionActivity.this, EducationalReminderActivity.class);
            startActivity(intent);
        });
        
        socialReminderCard.setOnClickListener(v -> {
            // For future implementation
            showFeatureNotAvailable("Social Reminders");
        });
        
        healthReminderCard.setOnClickListener(v -> {
            // For future implementation
            showFeatureNotAvailable("Health Reminders");
        });
        
        sleepReminderCard.setOnClickListener(v -> {
            // For future implementation
            showFeatureNotAvailable("Sleep Reminders");
        });
    }
    
    private void showFeatureNotAvailable(String featureName) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(featureName + " Coming Soon")
               .setMessage("This feature is currently under development and will be available in a future update.")
               .setPositiveButton("OK", null)
               .show();
    }
}