package com.example.project2_option3_michaelkibler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class  DataDisplayActivity extends AppCompatActivity {

    private Button buttonAddData, buttonEditGoal;
    private Cursor cursor;
    private TextView goalWeightText;
    private ListView listViewData;
    private List<WeighIn> weighInList;
    private WeighInAdapter adapter;
    private long currentUserID;
    private double goalWeight;
    private WeighInDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        // Retrieve the userID passed from LoginActivity
        Intent intent = getIntent();
        currentUserID = intent.getLongExtra("USER_ID", 0);

        // Initialize database helper
        dbHelper = new WeighInDatabaseHelper(this);

        // Initialize UI elements
        buttonAddData = findViewById(R.id.buttonAddData);
        buttonEditGoal = findViewById(R.id.buttonEditGoal);
        goalWeightText = findViewById(R.id.goalWeightText);
        goalWeightText.setText(String.format(Locale.US, "%.1f",getGoalWeight()));

        listViewData = findViewById(R.id.listViewData);

        weighInList = new ArrayList<>();

        // Set up adapter
        adapter = new WeighInAdapter(this, weighInList);
        listViewData.setAdapter(adapter);

        // Grab cursor pointing to weighIn data with respective userID
        cursor = dbHelper.getWeighIns(currentUserID);
        displayWeighIns(cursor);
    }

    public void displayWeighIns(Cursor cursor) {

        // Clear ArrayList
        weighInList.clear();

        // Check if there are any db records for user
        if (cursor != null) {

            // CASE 1 : There is at least one record, and we extract each record's data
            //          to our list view
            if (cursor.moveToFirst()) {
                do {
                    Long weighInID = cursor.getLong(0);
                    Long userID = cursor.getLong(1);
                    double weight = cursor.getDouble(2);
                    String date = cursor.getString(3);
                    double goalWeight = cursor.getDouble(4);

                    weighInList.add( new WeighIn(weighInID, userID, date, weight,
                            weight - goalWeight));

                } while (cursor.moveToNext());
            }
            cursor.close();
        } else {
            // CASE 2 : No records found in db for current user
            Log.d("onCreate", "No records to record data for user. Add data to display.");
        }

        adapter.notifyDataSetChanged();
    }

    public void addWeighIn(View view) {
        DialogWindow.showInputDialog(this,
                "Add a Weigh-In", "Weight (Lbs.)", "Add Weigh-In",
                input -> {

                    String date = new SimpleDateFormat("yyyy-MM-dd",
                            Locale.getDefault()).format(new Date());
                    double weight = Double.parseDouble(input);
                    double goalWeight = getGoalWeight();
                    double difference = Double.parseDouble(String.format(
                            Locale.US, "%.1f",weight - goalWeight));

                   dbHelper.addWeighIn(currentUserID, weight,
                            goalWeight);

                    // Debug logs
                    Log.d("addWeighIn", "Input weight: " + weight);
                    Log.d("addWeighIn", "Goal weight from DB: " + goalWeight);
                    Log.d("addWeighIn", "Weight difference: " + difference);

                    boolean isGoalAchieved = dbHelper.isGoalWeightAchieved(currentUserID, weight);
                    Log.d("addWeighIn", "isGoalWeightAchieved: " + isGoalAchieved);

                   // Check if the new weight matches the user's goalWeight
                   if (dbHelper.isGoalWeightAchieved(currentUserID, weight)) {
                       sendGoalAchievedSMS();
                   }

                    // Grab cursor pointing to weighIn data with respective userID
                    cursor = dbHelper.getWeighIns(currentUserID);
                    displayWeighIns(cursor);

                    // Log for testing
                    Log.d("addWeighIn", "User added weighIn data to db");
                    Toast.makeText(this, "Weigh-In Record Added " + goalWeight + "lbs.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Send and SMS message to the user
    ///////////////////////////////////
    private void sendGoalAchievedSMS () {
        String phoneNumber = dbHelper.getPhoneNum(currentUserID);
        String message = "Congratulations! You've achieved your goal weight. Choose a new goal " +
                "weight to continue on your fitness journey";

        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null,
                        message, null, null);
                Log.d("sendGoalAchievedSMS", "SMS notification sent.");
            } catch (Exception e) {
                Log.d("sendGoalAchievedSMS", "Error sending SMS." + e.getMessage());
            }
        } else {
            Log.d("sendGoalAchievedSMS", "User has SMS permissions denied.");
        }

    }

    public void changeGoalWeight(View view) {
        DialogWindow.showInputDialog(this,
                "Set Goal Weight", "Goal Weight (Lbs.)", "Update Goal",
                input -> {
                    double newGoalWeight = Double.parseDouble(input);

                    // Update your goal weight in the database or preferences
                    setGoalWeight(newGoalWeight);
                    Log.d("changeGoalWeight", "User updated goalWeight in db user record");
                    Toast.makeText(this, "Goal weight updated to " + newGoalWeight +
                            "lbs.", Toast.LENGTH_SHORT).show();
                });
    }

    public void setGoalWeight(double goalWeight) {

        dbHelper.updateGoal(currentUserID, goalWeight);
        goalWeightText.setText(String.format(Locale.US, "%.1f",getGoalWeight()));
    }

    public double getGoalWeight() {
        return dbHelper.getGoal(currentUserID);
    }

    public void logOut(View view) {
        currentUserID = 0;
        navigateToLoginDisplay();
    }

    public void navigateToLoginDisplay() {
        Intent intent = new Intent(DataDisplayActivity.this, LoginActivity.class);

        startActivity(intent);

        finish();
    }

}

