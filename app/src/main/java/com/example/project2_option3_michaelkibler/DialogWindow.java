package com.example.project2_option3_michaelkibler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class DialogWindow {

    public static void showInputDialog(Context context, String title, String hint, String buttonText,
                                 InputDialogCallback callback) {
        // Inflate the same dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_input, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(dialogView);

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        // Set dialog views
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);   // Dialog Title
        dialogTitle.setText(title);

        EditText editInput = dialogView.findViewById(R.id.editInput);       // Dialog EditText
        editInput.setHint(hint);

        Button buttonSave = dialogView.findViewById(R.id.buttonHandler);    // Dialog Button
        buttonSave.setText(buttonText);

        // Handle save button click
        buttonSave.setOnClickListener(v -> {
            String inputValue = editInput.getText().toString().trim();
            if (inputValue.isEmpty()) {

                Toast.makeText(context, "Please enter a value.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass the input value to the callback
            callback.onInputSubmitted(inputValue);

            dialog.dismiss();
        });
    }
    // Define an interface for the callback
    public interface InputDialogCallback {
        void onInputSubmitted(String input);
    }
}
