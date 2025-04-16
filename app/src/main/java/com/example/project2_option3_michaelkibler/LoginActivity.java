package com.example.project2_option3_michaelkibler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project2_option3_michaelkibler.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editUserName;
    private EditText editPassword;
    private String username;
    private String password;
    private WeighInDatabaseHelper dbHelper;
    private long userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toast.makeText(this, "LoginActivity loaded", Toast.LENGTH_SHORT).show();

        // Initialize UI elements
        editUserName = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        // Initialize database helper
        dbHelper = new WeighInDatabaseHelper(this);
    }

    public void login(View view) {

        if (validateInputs()) {

            userID = dbHelper.validateLogIn(username, password);

            if (userID != 0) {

                // Case 1 : username and password inputs match data in db, and user is logged in


                navigateToDataDisplay();

                // Log for testing
                Log.d("login", "Username and password are a match. User is logged in.");
                Toast.makeText(this, "Login successful!",
                        Toast.LENGTH_SHORT).show();


            } else {

                // Case 2 : username and password inputs do not match any db records
                //          and log-in is rejected
                Log.d("login", "No user with given account credentials.");
                Toast.makeText(this, "No account with given credentials. Please " +
                        "try again or make a new account.", Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void register(View view) {

        // Validate user inputs
        if (validateInputs()) {

            // Grab userID(0 if no user data in DB)
            userID = dbHelper.validateLogIn(username, password);

            if (userID == 0) {

                // Case 1 : username and password inputs do not match any db records
                //          and user is registered
                // Populate dialog window to request starting goalWeight from user
                DialogWindow.showInputDialog(this, "Enter Starting Goal Weight",
                        "Input Goal Weight (Lbs.)", "Set Starting Goal",
                        input -> {

                    // Take user input from dialog window and make a new SQLite query adding
                    // user info to db
                    double goalWeight = Double.parseDouble(input);
                    //userID = dbHelper.addUser(username, password, goalWeight);

                    navigateToSMSDisplay(username, password, goalWeight);
            });

                // Log for testing
                Log.d("register", "New User Account created.");
                Toast.makeText(this, "New User Account created.",
                        Toast.LENGTH_SHORT).show();


            } else {

                // Case 2 : username and password inputs match data in db,
                // Log for testing and do nothing
                Log.d("register", "There is a user with these credentials.");
                Toast.makeText(this, "There is a user with these credentials already.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean validateInputs() {

        // Grab the username/password from respective EditText fields
        username = editUserName.getText().toString().trim();
        password = editPassword.getText().toString().trim();

        boolean validInputs = !(username.isEmpty() || password.isEmpty());

        // Logging for testing
        if (validInputs) {

            // Case 1 : both username and password are filled
            Log.d("validateInputs", "User input fields filled.");

        } else {

            // Case 2 : either, or both, username and password fields are empty
            Log.d("validateInputs", "Username and or password field(s) are empty.");

        }

        return validInputs;
    }

    public void navigateToDataDisplay() {

        // Create the Intent to navigate to the DataDisplayActivity
        Intent intent = new Intent(LoginActivity.this,
                DataDisplayActivity.class);

        // Pass userID to new Activity
        intent.putExtra("USER_ID", userID);

        // Start the new activity
        startActivity(intent);

        // Close the current activity
        finish();
    }

    public void navigateToSMSDisplay(String userName, String password,
                                     double goalWeight) {
        // Create the Intent to navigate to the DataDisplayActivity
        Intent intent = new Intent(LoginActivity.this,
                SMSActivity.class);

        intent.putExtra("USERNAME", username);
        intent.putExtra("PASSWORD", password);
        intent.putExtra("GOAL_WEIGHT", goalWeight);

        // Start the new activity
        startActivity(intent);

        // Close the current activity
        finish();
    }

}