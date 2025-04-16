package com.example.project2_option3_michaelkibler;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project2_option3_michaelkibler.R;

import java.util.List;

public class WeighInAdapter extends ArrayAdapter<WeighIn> {
    private final List<WeighIn> weighInList;
    private final Context context;
    private final WeighInDatabaseHelper dbHelper;

    public WeighInAdapter(Context context, List<WeighIn> weighInList) {
        super(context, R.layout.row_item, weighInList);
        this.context = context;
        this.weighInList = weighInList;
        dbHelper = new WeighInDatabaseHelper(context);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.row_item, parent, false);
        }

        // Get the current WeighIn object
        WeighIn currentWeighIn = weighInList.get(position);

        // Bind data to TextViews
        TextView textDate = convertView.findViewById(R.id.textDate);
        TextView textWeighIn = convertView.findViewById(R.id.textWeighIn);
        TextView textDifference = convertView.findViewById(R.id.textDifference);
        ImageButton buttonDelete = convertView.findViewById(R.id.buttonDelete);

        String weight = currentWeighIn.getWeight() + "lb";
        String difference = currentWeighIn.getDifference() + "lb";

        textDate.setText(currentWeighIn.getDate());
        textWeighIn.setText(weight);
        textDifference.setText(difference);

        // Handle Delete Button Click
        buttonDelete.setOnClickListener(view -> {
            weighInList.remove(position); // Remove item from the list

            Long userID = currentWeighIn.getUserID();
            Long weighInID = currentWeighIn.getWeighInID();

            dbHelper.removeWeighIn(userID, weighInID);
            notifyDataSetChanged();      // Notify adapter to update the view
        });

        return convertView;
    }
}