package com.example.gpcalculator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.gpcalculator.data.AppDatabase;
import com.example.gpcalculator.data.GradeAdapter;
import com.example.gpcalculator.data.GradeEntry;
import com.example.gpcalculator.data.Helper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity implements GradeAdapter.RecyclerItemClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private GradeAdapter mAdapter;
    private TextView mEmptyView;
    private RecyclerView recyclerView;

    AppDatabase mDb;

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
                i.putExtra(Helper.KEY_MODE, Helper.MODE_ADD_NEW);
                startActivity(i);
            }
        });

        // Initialise Recycler view
        recyclerView = findViewById(R.id.list_item);

        // Setting the layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {


            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {

            }
        }).attachToRecyclerView(recyclerView);

        // Initialising and setting adapter
        mAdapter = new GradeAdapter(this, this);
        recyclerView.setAdapter(mAdapter);

        mDb = AppDatabase.getInstance((getApplicationContext()));

        setupGrades();
    }

    private void setupGrades() {

        LiveData<List<GradeEntry>> listLiveData = mDb.gradeDao().loadAllGrades();

        listLiveData.observe(this, new Observer<List<GradeEntry>>() {
            @Override
            public void onChanged(List<GradeEntry> gradeEntries) {
                mAdapter.setGrades(gradeEntries);
                mAdapter.notifyDataSetChanged();

                computeCGPA(gradeEntries);
            }
        });
    }

    private void showEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);

        recyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideEmptyView() {
        mEmptyView.setVisibility(View.INVISIBLE);

        recyclerView.setVisibility(View.VISIBLE);
    }

    private void computeCGPA(List<GradeEntry> gradeEntries) {

        TextView cumulativeView = findViewById(R.id.cumulative_view);
        TextView gradeClassTV = findViewById(R.id.gp_class);

        if (gradeEntries == null) {
            Log.d(TAG, "Grade entries is empty");
            return;
        }

        int gradeCount = gradeEntries.size();
        if (gradeCount == 0){
            gradeClassTV.setText("");
            cumulativeView.setText("0.0");
            showEmptyView();
            return;
        }

        double cumulative = 0;
        int overallTotalUnits = 0;

        for (int i = 0; i < gradeCount; i++) {
            GradeEntry gradeEntry = gradeEntries.get(i);
            double gp = gradeEntry.getGP();
            int tu = gradeEntry.getTotalUnits();
            cumulative += gp * tu;
            overallTotalUnits += tu;
        }

        // Evaluate the CGPA
        double CGPA = (double) Math.round(cumulative / overallTotalUnits * 100) / 100;

        // What class is the CGPA in?
        String gradeClass = Helper.getGradeClass(CGPA);

        // Setting texts
        cumulativeView.setText(String.valueOf(CGPA));
        gradeClassTV.setText(gradeClass);

        hideEmptyView();
    }

/*

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

                String gradeDetails = Helper.STRING_SPLIT + level
                        + Helper.STRING_SPLIT +session;

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

                    populateUI(cursor);

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
*/

    @Override
    public void onItemClick(String level, String session, String semester) {
        // LiveData<GradeEntry> gradeEntryLiveData = mDb.gradeDao().loadTaskByProperties(level, session, semester);
    }
}
