package com.example.medicinereminder;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.medicinereminder.utils.SharedPreferencesHelper;

public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        prefsHelper = new SharedPreferencesHelper(this);

        TextView appName = findViewById(R.id.appName);
        TextView tagline = findViewById(R.id.tagline);

        // Set app name and tagline
        appName.setText(getString(R.string.app_name));
        tagline.setText(getString(R.string.app_tagline));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToNextScreen();
            }
        }, SPLASH_DELAY);
    }

    private void navigateToNextScreen() {
        Intent intent;

        if (prefsHelper.isFirstLaunch()) {
            // First time launching the app
            prefsHelper.setFirstLaunch(false);
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else if (prefsHelper.isAuthEnabled()) {
            // Authentication is enabled
            intent = new Intent(SplashActivity.this, AuthActivity.class);
        } else {
            // Go directly to home
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        }

        startActivity(intent);
        finish();
    }
}