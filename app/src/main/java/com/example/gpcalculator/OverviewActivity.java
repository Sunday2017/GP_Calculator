package com.example.gpcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.gpcalculator.data.AppDatabase;
import com.example.gpcalculator.data.Helper;
import com.example.gpcalculator.data.GradeEntry;

public class OverviewActivity extends AppCompatActivity {

    private String[] courses;
    private String[] units;
    private String[] grades;
    private String[] properties;

    private String mMode;

    private static final String TAG = OverviewActivity.class.getSimpleName();

    AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        Intent i = getIntent();

        if (i != null) {
            Button saveUpdateEditButton = findViewById(R.id.save_update_edit_btn);
            mMode = i.getStringExtra(Helper.KEY_MODE);
            mDb = AppDatabase.getInstance(getApplicationContext());


            if (mMode.equals(Helper.MODE_ADD_NEW)) {
                saveUpdateEditButton.setText(getString(R.string.save_btn));
                courses = i.getStringArrayExtra(Helper.KEY_COURSES);
                units = i.getStringArrayExtra(Helper.KEY_UNITS);
                grades = i.getStringArrayExtra(Helper.KEY_GRADES);
                properties = i.getStringArrayExtra(Helper.KEY_PROPERTIES);

                setViewsText();

                saveUpdateEditButton.setOnClickListener(onSaveListener);
            } else if (mMode.equals(Helper.MODE_VIEW)){
                saveUpdateEditButton.setText(getString(R.string.edit_btn));
                String level = i.getStringExtra(Helper.KEY_LEVEL);
                String session = i.getStringExtra(Helper.KEY_SESSION);
                String semester = i.getStringExtra(Helper.KEY_SEMESTER);

                LiveData<GradeEntry> entryLiveData =  mDb.gradeDao()
                        .loadGradeByProperties(level, session, semester);
                entryLiveData.observe(this, new Observer<GradeEntry>() {
                    @Override
                    public void onChanged(GradeEntry gradeEntry) {
                        courses = gradeEntry.getCourses();
                        units = gradeEntry.getUnits();
                        grades = gradeEntry.getGrades();

                        properties = new String[5];
                        properties[0] = String.valueOf(gradeEntry.getGP());
                        properties[1] = gradeEntry.getLevel();
                        properties[2] = gradeEntry.getSession();
                        properties[3] = gradeEntry.getSemester();
                        properties[4] = String.valueOf(gradeEntry.getTotalUnits());

                        setViewsText();
                    }
                });

                saveUpdateEditButton.setOnClickListener(editListener);
            } else if (mMode.equals(Helper.MODE_UPDATE)) {
                this.setTitle(R.string.update_btn);
                saveUpdateEditButton.setText(getString(R.string.update_btn));

                courses = i.getStringArrayExtra(Helper.KEY_COURSES);
                units = i.getStringArrayExtra(Helper.KEY_UNITS);
                grades = i.getStringArrayExtra(Helper.KEY_GRADES);
                properties = i.getStringArrayExtra(Helper.KEY_PROPERTIES);
                saveUpdateEditButton.setOnClickListener(updateListener);
            }
        }
    }

    private void setViewsText() {
        double GP = Double.parseDouble(properties[0]);
        String level = properties[1];
        String session = properties[2];
        String semester = properties[3];
        String totalUnits = properties[4];
        String stat = Helper.getGradesStat(grades);

        TextView gpTextView = findViewById(R.id.calculated_gp);
        TextView gpClassTextView = findViewById(R.id.gp_class);
        TextView levelTextView = findViewById(R.id.user_level);
        TextView sessionTextView = findViewById(R.id.user_session);
        TextView semesterTextView = findViewById(R.id.user_semester);
        TextView totalUnitsTextView = findViewById(R.id.user_total_units);
        TextView statTextView = findViewById(R.id.user_grades_stat);

        String gpClassGrade = Helper.getGradeClass(GP);
        String format = "%s %s";

        gpTextView.setText(String.valueOf(GP));
        gpClassTextView.setText(gpClassGrade);
        levelTextView.setText(String.format(format, getString(R.string.level),  level));
        sessionTextView.setText(String.format(format, getString(R.string.session), session));
        semesterTextView.setText(
                String.format(format, getString(R.string.semester), semester));
        totalUnitsTextView.setText(
                String.format(format, getString(R.string.total_units), totalUnits));
        statTextView.setText(String.format(format, getString(R.string.stat), stat));
    }

    private final View.OnClickListener editListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(OverviewActivity.this, GPCalcActivity.class);
            intent.putExtra(Helper.KEY_MODE, mMode);
            intent.putExtra(Helper.KEY_COURSES, courses);
            intent.putExtra(Helper.KEY_UNITS, units);
            intent.putExtra(Helper.KEY_GRADES, grades);
            intent.putExtra(Helper.KEY_PROPERTIES, properties);

            startActivity(intent);
        }
    };

    private final View.OnClickListener onSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            double GP = Double.parseDouble(properties[0]);
            String level = properties[1];
            String session = properties[2];
            String semester = properties[3];
            int totalUnits = Integer.parseInt(properties[4]);

            GradeEntry gradeEntry = new GradeEntry(courses, units, grades, GP,
                    level, session, semester, totalUnits
            );

            LiveData<GradeEntry> gradeEntryLiveData = mDb.gradeDao()
                    .loadGradeByProperties(level, session, semester);

            if (gradeEntryLiveData.getValue() == null) {
                mDb.gradeDao().insertGrade(gradeEntry);

                Log.d(TAG, "Inserted successfully");
            } else {
                Log.d(TAG, "Existing aiidy");
            }
            NavUtils.navigateUpFromSameTask(OverviewActivity.this);
        }
    };

    private final View.OnClickListener updateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            double GP = Double.parseDouble(properties[0]);
            String level = properties[1];
            String session = properties[2];
            String semester = properties[3];
            int totalUnits = Integer.parseInt(properties[4]);
            final String initLevel = properties[5];
            final String initSession = properties[6];
            final String initSemester = properties[7];

            GradeEntry gradeEntry = new GradeEntry(
                    courses, units, grades,
                    Double.parseDouble(properties[0]), level, session, semester, totalUnits);


            if (level.equals(initLevel) && session.equals(initSession) && semester.equals(initSemester)) {

                mDb.gradeDao().updateGrade(gradeEntry);
                Log.d(TAG, "Updated successfully");
                NavUtils.navigateUpFromSameTask(OverviewActivity.this);
            } else {
                LiveData<GradeEntry> entryLiveData = mDb.gradeDao().loadGradeByProperties(level, session, semester);
                entryLiveData.observe(OverviewActivity.this, new Observer<GradeEntry>() {
                    @Override
                    public void onChanged(GradeEntry gradeEntry) {
                        if (gradeEntry == null) {
                            mDb.gradeDao().deleteGradeByProperties(initLevel, initSession, initSemester);

                            mDb.gradeDao();
                        } else{
                            Log.d(TAG, "Does exist");
                        }
                    }
                });
            }
        }
    };

    private void goHome() {
        Intent i = new Intent(OverviewActivity.this, MainActivity.class);
        startActivity(i);
    }

    /*private void showDialogForUpdate(final ContentValues values, final String details) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.details_already_exist);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int noOfRowsUpdated = getContentResolver().update(mUri, values, null, null);

                if (noOfRowsUpdated == 1) {
                    goHome();
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
    }*/
}
