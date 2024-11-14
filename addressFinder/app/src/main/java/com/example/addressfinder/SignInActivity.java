package com.example.addressfinder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.textfield.TextInputLayout;

public class SignInActivity extends AppCompatActivity {
    private EditText editTextEmailOrPhone;
    private EditText editTextPassword;
    private TextInputLayout inputLayoutEmailOrPhone;
    private FirebaseAuth mAuth;  // FirebaseAuth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Check if user is already signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startMainActivity();
            finish();
            return;
        }

        // Initialize UI components
        editTextEmailOrPhone = findViewById(R.id.editTextEmailOrPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        inputLayoutEmailOrPhone = findViewById(R.id.inputLayoutEmailOrPhone);
        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        TextView textViewSignUp = findViewById(R.id.textViewSignUp);

        // Add text watcher to dynamically update input type
        editTextEmailOrPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update hint based on input
                updateInputHint(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set up click listeners
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        });
    }

    private void updateInputHint(String input) {
        if (input.contains("@")) {
            inputLayoutEmailOrPhone.setHint("Email");
        } else {
            inputLayoutEmailOrPhone.setHint("Phone Number");
        }
    }

    private void signIn() {
        String input = editTextEmailOrPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Basic validation
        if (input.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (input.contains("@")) {
            // Sign in with email and password
            signInWithEmail(input, password);
        } else {
            // Phone-based sign-in isn't directly supported for email/password authentication in Firebase
            // You would need to implement phone authentication if necessary, which involves OTP
            Toast.makeText(this, "Phone number login is not implemented", Toast.LENGTH_SHORT).show();
        }
    }

    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        startMainActivity();
                        finish();
                    } else {
                        // If sign in fails, display a message to the user
                        Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
