package com.example.gpcalculator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.gpcalculator.data.GPContract;
import com.example.gpcalculator.data.GPContract.GPConstants;
import com.example.gpcalculator.data.GPContract.GPEntry;
import com.example.gpcalculator.data.RecyclerViewCursorAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements RecyclerViewCursorAdapter.RecyclerItemClickListener {

    // Setting up global variables
    FloatingActionButton mAddSemesterTextView;
    RecyclerViewCursorAdapter mAdapter;
    TextView mEmptyViewForList;
    RecyclerView recyclerView;

    double CGPA;

    private String KEY_SAVE_CGPA = "User CGPA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialising variables
        mAddSemesterTextView = (FloatingActionButton) findViewById(R.id.add_new_semester);
        mEmptyViewForList = (TextView) findViewById(R.id.empty_view);

        // Set click listener for the addSemester button
        mAddSemesterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start new Activity explicitly
                Intent i = new Intent(MainActivity.this, GPCalcActivity.class);
                startActivity(i);
            }
        });

        // Initialise Recycler view
        recyclerView = (RecyclerView) findViewById(R.id.list_item);

        // Setting the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setHasFixedSize(true);

        // Initialising and setting adapter
        mAdapter = new RecyclerViewCursorAdapter(this, null, this);
        recyclerView.setAdapter(mAdapter);

        queryDatabase();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble(KEY_SAVE_CGPA, CGPA);
    }

    public void queryDatabase() {
        Cursor c = getContentResolver().query( GPEntry.CONTENT_URI, null, null, null, null);

        visualiseDetails(c);
    }


   // @Override
    public void visualiseDetails(Cursor data) {

        TextView cumulativeView = (TextView) findViewById(R.id.cumulative_view);
        TextView gradeClassTV = (TextView) findViewById(R.id.gp_class);

        if (data != null){

            if (data.getCount() == 0) {
                gradeClassTV.setText("");
                cumulativeView.setText("0.0");
                mAdapter.swapCursor(data);
                return;
            }

            /* Computing the Cumulative Grade point Average */
            double cumulative = 0;
            int overallTotalUnits = 0;
            while (data.moveToNext()) {

                double gp = data.getDouble(data.getColumnIndex(GPEntry.COLUMN_GP));
                int tu = data.getInt(data.getColumnIndex(GPEntry.COLUMN_TU));
                cumulative += gp * tu;
                overallTotalUnits += tu;
            }

            CGPA = (double) Math.round(cumulative / overallTotalUnits * 100) / 100;

            String gradeClass = GPConstants.getGradeClass(CGPA);

            cumulativeView.setText("" +CGPA);
            gradeClassTV.setText(gradeClass);
        }

        mAdapter.changeCursor(data);
    }


    /* @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }*/

    @Override
    public void onItemClick(Cursor cursor, int position) {

        cursor.moveToPosition(position);

        String details = cursor.getString(cursor.getColumnIndex(GPContract.GPEntry.COLUMN_DETAILS));
        double gp = cursor.getDouble(cursor.getColumnIndex(GPEntry.COLUMN_GP));
        int totalUnits = cursor.getInt(cursor.getColumnIndex(GPEntry.COLUMN_TU));
        String grades = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_GRADES));

        String gradesStat = GPConstants.getGradesStat(grades);
        String[] detArray = details.split(GPConstants.STRING_SPLIT);

        Intent i = new Intent(MainActivity.this, OverviewActivity.class);
        i.putExtra(GPConstants.KEY_CALCULATED_GP, gp);
        i.putExtra(GPConstants.KEY_LEVEL, Integer.parseInt(detArray[1]));
        i.putExtra(GPConstants.KEY_SESSION, detArray[2]);
        i.putExtra(GPConstants.KEY_SEMESTER, detArray[3]);
        i.putExtra(GPConstants.KEY_TOTAL_UNITS, totalUnits);
        i.putExtra(GPConstants.KEY_MODE, GPConstants.STRING_VIEW_MODE);
        i.putExtra(GPConstants.KEY_GRADES_STAT, gradesStat);

        startActivity(i);
    }
}
