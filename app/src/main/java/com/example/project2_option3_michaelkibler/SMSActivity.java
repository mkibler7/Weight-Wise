package com.example.project2_option3_michaelkibler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class SMSActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private String userName, password;
    private long userID;
    private double goalWeight;
    private EditText editTextPhoneNumber, editTextMessage;
    private WeighInDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        // Create database helper
        dbHelper = new WeighInDatabaseHelper(this);

        // Initialize UI
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        // Grab the Intent values from LoginActivity
        Intent intent = getIntent();
        userName = intent.getStringExtra("USERNAME");
        password = intent.getStringExtra("PASSWORD");
        goalWeight = intent.getDoubleExtra("GOAL_WEIGHT", 0);

    }

    // Register user's phone number
    /////////////////////////////////////
    public void signUpUser(View view) {

        // Grab the user input
        String phoneNumber = editTextPhoneNumber.getText().toString();

        //    String message = "Goal Weight Achieved!";

        // Check if the phoneNumber length is a valid 3 digit area code followed by a 7 digit number
        // or if the user has opted to not give their number
        if (!phoneNumber.equals("-1") && !phoneNumber.equals("5554")) {

            phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

            if ((phoneNumber.length() != 10)) {
                Toast.makeText(this, "Please enter a valid phone number, or -1 if you do" +
                                "not want to sign up for SMS updates..",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Format string into a valid phone number for db insertion
            String formattedNumber = String.format("%s=%s=%s",
                    phoneNumber.substring(0, 3),
                    phoneNumber.substring(3, 6),
                    phoneNumber.substring(6, 10)
            );
        }

        // Check if permissions are accepted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Already accepted permissions!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SMS permission is required to send messages.",
                    Toast.LENGTH_SHORT).show();
            // Request permissions
            ActivityCompat.requestPermissions(this, new
                    String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }

        // Add user info to database
        userID = dbHelper.addUser(userName, password, goalWeight, phoneNumber);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission denied",
                        Toast.LENGTH_SHORT).show();
            }
        }

        navigateToDataDisplay();

    }

    private void navigateToDataDisplay() {
        Intent intent = new Intent(SMSActivity.this, DataDisplayActivity.class);

        intent.putExtra("USER_ID", userID);

        startActivity(intent);

        finish();

    }


}