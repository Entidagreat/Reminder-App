package com.example.medicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.example.medicinereminder.utils.SharedPreferencesHelper;
import java.util.concurrent.Executor;

public class AuthActivity extends AppCompatActivity {
    private SharedPreferencesHelper prefsHelper;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private TextView titleText;
    private TextView instructionText;
    private Button authenticateButton;
    private EditText pinEditText;
    private Button verifyPinButton;
    private View pinLayout;
    private View biometricLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        prefsHelper = new SharedPreferencesHelper(this);
        initViews();
        setupBiometric();
        setupUI();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        instructionText = findViewById(R.id.instructionText);
        authenticateButton = findViewById(R.id.authenticateButton);
        pinEditText = findViewById(R.id.pinEditText);
        verifyPinButton = findViewById(R.id.verifyPinButton);
        pinLayout = findViewById(R.id.pinLayout);
        biometricLayout = findViewById(R.id.biometricLayout);
    }

    private void setupBiometric() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                showPinLayout();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                authenticateSuccess();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AuthActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authenticate")
                .setSubtitle("Use your fingerprint or face to access your medications")
                .setNegativeButtonText("Use PIN")
                .build();
    }

    private void setupUI() {
        titleText.setText(getString(R.string.welcome_back));
        instructionText.setText(getString(R.string.auth_instruction));

        // Check if biometric authentication is available
        BiometricManager biometricManager = BiometricManager.from(this);
        boolean canUseBiometric = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;

        if (canUseBiometric && prefsHelper.useBiometric()) {
            showBiometricLayout();
        } else {
            showPinLayout();
        }

        authenticateButton.setOnClickListener(v -> {
            if (canUseBiometric && prefsHelper.useBiometric()) {
                biometricPrompt.authenticate(promptInfo);
            } else {
                showPinLayout();
            }
        });

        verifyPinButton.setOnClickListener(v -> verifyPin());
    }

    private void showBiometricLayout() {
        biometricLayout.setVisibility(View.VISIBLE);
        pinLayout.setVisibility(View.GONE);

        // Auto-trigger biometric prompt if enabled
        if (prefsHelper.useBiometric()) {
            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void showPinLayout() {
        biometricLayout.setVisibility(View.GONE);
        pinLayout.setVisibility(View.VISIBLE);
        pinEditText.requestFocus();
    }

    private void verifyPin() {
        String enteredPin = pinEditText.getText().toString().trim();
        String savedPin = prefsHelper.getPinCode();

        if (TextUtils.isEmpty(enteredPin)) {
            pinEditText.setError("Please enter PIN");
            return;
        }

        if (TextUtils.isEmpty(savedPin)) {
            // First time setting PIN
            if (enteredPin.length() >= 4) {
                prefsHelper.setPinCode(enteredPin);
                prefsHelper.setAuthEnabled(true);
                authenticateSuccess();
            } else {
                pinEditText.setError("PIN must be at least 4 digits");
            }
        } else {
            // Verify existing PIN
            if (enteredPin.equals(savedPin)) {
                authenticateSuccess();
            } else {
                pinEditText.setError("Incorrect PIN");
                pinEditText.setText("");
            }
        }
    }

    private void authenticateSuccess() {
        Intent intent = new Intent(AuthActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back from auth screen
        moveTaskToBack(true);
    }
}