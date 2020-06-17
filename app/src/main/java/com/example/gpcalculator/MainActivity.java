package com.example.gpcalculator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gpcalculator.data.GPContract;
import com.example.gpcalculator.data.GPContract.GPConstants;
import com.example.gpcalculator.data.GPContract.GPEntry;
import com.example.gpcalculator.data.RecyclerViewCursorAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements RecyclerViewCursorAdapter.RecyclerItemClickListener {

    private RecyclerViewCursorAdapter mAdapter;
    private TextView mEmptyView;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialising variables
        FloatingActionButton mAddSemesterTextView = findViewById(R.id.add_new_semester);
        mEmptyView = findViewById(R.id.empty_view);

        // Click listener for the floating action button
        mAddSemesterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start new Activity explicitly
                Intent i = new Intent(MainActivity.this, GPCalcActivity.class);
                startActivity(i);
            }
        });

        // Initialise Recycler view
        recyclerView = findViewById(R.id.list_item);

        // Setting the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);

        // Initialising and setting adapter
        mAdapter = new RecyclerViewCursorAdapter(null, this);
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

        queryDatabase();
    }

    private void queryDatabase() {

        // Query DB for the whole table
        Cursor c = getContentResolver().query(GPEntry.CONTENT_URI, null, null, null, null);

        visualiseCursorDetails(c);
    }

    private void showEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);

        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideEmptyView() {
        mEmptyView.setVisibility(View.INVISIBLE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void visualiseCursorDetails(Cursor data) {

        TextView cumulativeView = findViewById(R.id.cumulative_view);
        TextView gradeClassTV = findViewById(R.id.gp_class);

        /*
        * COMPUTING CGPA
        */
        if (data != null){

            // When there is no data
            if (data.getCount() == 0) {
                gradeClassTV.setText("");
                cumulativeView.setText("0.0");

                mAdapter.swapCursor(data);
                showEmptyView();
                return;
            }

            double cumulative = 0;
            int overallTotalUnits = 0;

            while (data.moveToNext()) {

                double gp = data.getDouble(data.getColumnIndex(GPEntry.COLUMN_GP));
                int tu = data.getInt(data.getColumnIndex(GPEntry.COLUMN_TU));
                cumulative += gp * tu;
                overallTotalUnits += tu;
            }

            // Evaluate the CGPA
            double CGPA = (double) Math.round(cumulative / overallTotalUnits * 100) / 100;

            // What class is the CGPA in?
            String gradeClass = GPConstants.getGradeClass(CGPA);

            // Setting texts
            cumulativeView.setText(String.valueOf(CGPA));
            gradeClassTV.setText(gradeClass);

            hideEmptyView();

            // Change cursor of adapter
            mAdapter.changeCursor(data);
        }else {
            showEmptyView();
        }
    }

    @Override
    public void onItemClick(Cursor cursor, int position) {

        cursor.moveToPosition(position);

        String details = cursor.getString(cursor.getColumnIndex(GPContract.GPEntry.COLUMN_DETAILS));
        double gp = cursor.getDouble(cursor.getColumnIndex(GPEntry.COLUMN_GP));
        int totalUnits = cursor.getInt(cursor.getColumnIndex(GPEntry.COLUMN_TU));
        String grades = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_GRADES));
        String semester = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_SEMESTER));

        String gradesStat = GPConstants.getGradesStat(grades);

        // Array of {"", level, session}
        String[] detArray = details.split(GPConstants.STRING_SPLIT);

        Intent i = new Intent(MainActivity.this, OverviewActivity.class);
        i.putExtra(GPConstants.KEY_CALCULATED_GP, gp);
        i.putExtra(GPConstants.KEY_LEVEL, Integer.parseInt(detArray[1]));
        i.putExtra(GPConstants.KEY_SESSION, detArray[2]);
        i.putExtra(GPConstants.KEY_SEMESTER, semester);
        i.putExtra(GPConstants.KEY_TOTAL_UNITS, totalUnits);
        i.putExtra(GPConstants.KEY_MODE, GPConstants.STRING_VIEW_MODE);
        i.putExtra(GPConstants.KEY_GRADES_STAT, gradesStat);

        startActivity(i);
    }

    @Override
    public void onItemDeleteClick(View v, final int position) {
        deleteMessageDialog(v);
    }


    private void deleteMessageDialog(View v) {
        final Context context = v.getContext();

        final TextView detailsTV = v.findViewById(R.id.item_details);

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.confirm_to_delete);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // This is an array of {level, semester, session}
                String[] detArray = detailsTV.getText().toString().split(" ");

                String level = detArray[0];
                String semester = detArray[1];
                String session = detArray[2];

                String gradeDetails = GPConstants.STRING_SPLIT + level
                        + GPConstants.STRING_SPLIT +session;

                String whereClause = GPEntry.COLUMN_DETAILS +"= ? AND "
                        + GPEntry.COLUMN_SEMESTER + "= ?";

                String[] whereArgs = new String[] {gradeDetails, semester};

                int num = context.getContentResolver()
                        .delete(GPEntry.CONTENT_URI, whereClause, whereArgs);

                if (num > 0) {

                    Cursor cursor = getContentResolver()
                            .query(GPEntry.CONTENT_URI,
                                    null,
                                    null,
                                    null,
                                    null);

                    visualiseCursorDetails(cursor);

                    Toast.makeText(context, R.string.successful_delete_msg, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
