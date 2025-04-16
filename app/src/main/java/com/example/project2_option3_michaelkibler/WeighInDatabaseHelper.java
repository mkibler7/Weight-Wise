package com.example.project2_option3_michaelkibler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeighInDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "weighIns.db";
    private static final int VERSION = 3;

    public WeighInDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // Create a table for user profiles
    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "userID";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD = "password";
        private static final String COL_GOAL = "goalWeight";
        private static final String COL_PHONE = "phoneNumber";
    }

    ////////////////////////////////////////////////////
    // Methods for UserTable ///////////////////////////
    ////////////////////////////////////////////////////

    // Method for new users to register to the database
    ///////////////////////////////////////////////////
    public long addUser(String userName, String password, double goalWeight, String phoneNumber) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, userName);
        values.put(UserTable.COL_PASSWORD, password);
        values.put(UserTable.COL_GOAL, goalWeight);
        values.put(UserTable.COL_PHONE, phoneNumber);


        return db.insert(UserTable.TABLE, null, values);
    }

    // Method to check userID/password exist in the db
    ////////////////////////////////////////////////////
    public long validateLogIn(String userName, String password) {

        // Get a readable state of our db
        SQLiteDatabase db = getReadableDatabase();

        // Create a query command to select user row based off userName/password values
        String query = "SELECT * FROM " + UserTable.TABLE + " WHERE " +
            UserTable.COL_USERNAME + " = ? AND " + UserTable.COL_PASSWORD + " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {userName, password});

        // Check if the cursor is pointing to a record from the db
        if (cursor.moveToFirst()) {

            // Case 1 : We find a matching record
            long userID = cursor.getLong(0);

            Log.d("validateLogIn", "Login successful for user: " + userName);

            cursor.close();
            return userID;
        }

        // Case 2 : We don't find a matching username
        Log.d("validateLogIn", "Invalid login attempt for user: " + userName);
        cursor.close();
        return 0;
    }

    // Method to update a user's goal weight
    ////////////////////////////////////////////////////
    public void updateGoal(long userID, double goalWeight) {

        if (goalWeight <= 0) {
            Log.d("updateGoal", "Invalid goal weight: " + goalWeight);
            return;
        }

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserTable.COL_GOAL, goalWeight);

        int rowsUpdated = db.update(UserTable.TABLE, values, UserTable.COL_ID + " = ?",
                new String[] { Long.toString(userID)});

        Log.d("updateGoal", "Rows updated: " + rowsUpdated);
    }

    public double getGoal(long userID) {

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + UserTable.TABLE + " WHERE " + UserTable.COL_ID +
                " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(userID)});

        // There should only be one userID, so continue with the first record
        if (cursor.moveToFirst()) {

            // Return the data from the 4th column
            Log.d("getGoal","Found record in db, extracted the user's goalWeight");

            double currentWeight = cursor.getDouble(3);
            cursor.close();

            return currentWeight;
        }

        // No record for the given userID
        Log.d("getGoal", "No db record for the give userID");
        cursor.close();
        return 0;
    }

    public String getPhoneNum(long userID) {

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT " + UserTable.COL_PHONE + " FROM " + UserTable.TABLE +
                " WHERE " + UserTable.COL_ID +
                " = ?";

        Cursor cursor = db.rawQuery(query, new String[] {Long.toString(userID)});

        // There should only be one userID, so continue with the first record
        if (cursor.moveToFirst()) {

            // Return the data from the 4th column
            Log.d("getPhoneNum","Found record in db, extracted the user's phoneNumber");

            String phoneNumber = cursor.getString(0);
            cursor.close();

            return phoneNumber;
        }

        // No record for the given userID
        Log.d("getGoal", "No db record for the give userID");
        cursor.close();
        return "-1";
    }


    // Create a table for weighIns
    ////////////////////////////////////////////////////
    private static final class WeighInTable {
        private static final String TABLE = "weighIns";

        private static final String COL_ID = "weighInID"; // Unique ID for each weigh-in
        private static final String COL_USER_ID = "userID"; // Foreign key referencing UserTable
        private static final String COL_WEIGHT = "weight";
        private static final String COL_DATE = "date";
        private static final String COL_GOAL_WEIGHT = "goalWeight";

    }

    // Add weigh in data for the user
    ////////////////////////////////////////////////////
    public void addWeighIn(long userID, double weight, double currentGoalWeight) {

        SQLiteDatabase db = getWritableDatabase();

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                new Date());

        ContentValues values = new ContentValues();
        values.put(WeighInTable.COL_USER_ID, userID);
        values.put(WeighInTable.COL_WEIGHT, weight);
        values.put(WeighInTable.COL_DATE, currentDate);
        values.put(WeighInTable.COL_GOAL_WEIGHT, currentGoalWeight);

        long result = db.insert(WeighInTable.TABLE, null, values);

        if (result == -1) {
            Log.e("addWeighIn", "Error inserting weigh-in data into database");
        } else {
            Log.d("addWeighIn", "weighIn data added with ID " + result);
        }
    }

    // Check to see if new weighInData matches user's goal weight
    ////////////////////////////////////////////////////
    public boolean isGoalWeightAchieved (long userID, double weighInWeight) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + UserTable.COL_GOAL + " FROM " + UserTable.TABLE +
                " WHERE " + UserTable.COL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userID)});

        if (cursor.moveToFirst()) {
            double goalWeight = cursor.getDouble(0);
            cursor.close();
            return Math.abs(weighInWeight - goalWeight) < 0.001; // Check if the weight matches
        }
        cursor.close();
        return false;
    }

    // Add weigh in data for the user
    ////////////////////////////////////////////////////
    public void removeWeighIn(long userID, long weighInID) {

        SQLiteDatabase db = getWritableDatabase();

        // Define the WHERE clause to target the specific weigh-in record
        String whereClause = WeighInTable.COL_USER_ID + " = ? AND " + WeighInTable.COL_ID + " = ?";

        // Define the arguments for the WHERE clause
        String[] whereArgs = {String.valueOf(userID), String.valueOf(weighInID)};

        // Execute the delete query
        int rowsDeleted = db.delete(WeighInTable.TABLE, whereClause, whereArgs);

        // Log the result for debugging purposes
        if (rowsDeleted > 0) {
            Log.d("removeWeighIn", "Weigh-in record deleted. WeighInID: " + weighInID);
        } else {
            Log.d("removeWeighIn", "No record found to delete for WeighInID: " + weighInID);
        }
    }

    // Retrieve all weigh-in data records for a specific user
    ////////////////////////////////////////////////////
    public Cursor getWeighIns(long userID) {

        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + WeighInTable.TABLE + " WHERE " +
                WeighInTable.COL_USER_ID + " = ? " +
                "ORDER BY " + WeighInTable.COL_ID + " DESC";

        Log.d("getWeighIns", "Grabbed weigh-in data records for user: " + userID);
        return db.rawQuery(query, new String[] {String.valueOf(userID)});
    }

    public void deleteAllWeighInData() {
        SQLiteDatabase db = getWritableDatabase();

        // Delete all records of data from WeighInTable
        db.execSQL("DELETE FROM " + WeighInTable.TABLE);

        // Reset the auto-increment counter
        db.execSQL("DELETE FROM sqlite_sequence WHERE name = '" + WeighInTable.TABLE + "'");

        Log.d("Database", "All weigh-in data deleted.");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        UserTable.COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                        UserTable.COL_PASSWORD + " TEXT NOT NULL, " +
                        UserTable.COL_GOAL + " FLOAT, " +
                        UserTable.COL_PHONE + " TEXT NOT NULL)"
                );
        db.execSQL("CREATE TABLE " + WeighInTable.TABLE + " (" +
                WeighInTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeighInTable.COL_USER_ID + " INTEGER, " +
                WeighInTable.COL_WEIGHT + " FLOAT, " +
                WeighInTable.COL_DATE + " TEXT, " +
                WeighInTable.COL_GOAL_WEIGHT + " TEXT, " +
                "FOREIGN KEY(" + WeighInTable.COL_USER_ID + ") REFERENCES " +
                UserTable.TABLE + "(" + UserTable.COL_ID + "))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            // Add the goalWeight column if upgrading
            db.execSQL("ALTER TABLE " + UserTable.TABLE + " ADD COLUMN phoneNumber REAL");
        }
        db.execSQL("DROP TABLE IF EXISTS " + WeighInTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UserTable.TABLE);
        onCreate(db);
    }
}
