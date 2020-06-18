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

            // Called when a user swipes lqeft or right on a ViewHolder
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

    @Override
    public void onItemClick(String level, String session, String semester) {

        Intent intent = new Intent(MainActivity.this, OverviewActivity.class);
        intent.putExtra(Helper.KEY_MODE, Helper.MODE_VIEW);
        intent.putExtra(Helper.KEY_LEVEL, level);
        intent.putExtra(Helper.KEY_SESSION, session);
        intent.putExtra(Helper.KEY_SEMESTER, semester);
        startActivity(intent);
    }

}
