package com.example.gpcalculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.LiveData;

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

            if (mMode.equals(Helper.MODE_ADD_NEW)) {
                saveUpdateEditButton.setText(getString(R.string.save_btn));
                courses = i.getStringArrayExtra(Helper.KEY_COURSES);
                units = i.getStringArrayExtra(Helper.KEY_UNITS);
                grades = i.getStringArrayExtra(Helper.KEY_GRADES);
                properties = i.getStringArrayExtra(Helper.KEY_PROPERTIES);

                setViewsText();

                saveUpdateEditButton.setOnClickListener(onSaveListener);
            }
            mDb = AppDatabase.getInstance(getApplicationContext());
        }
    }

    private void setViewsText() {
        String level = properties[0];
        String session = properties[1];
        String semester = properties[2];
        String totalUnits = properties[3];
        double GP = Double.parseDouble(properties[4]);
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

        }
    };

    private final View.OnClickListener onSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String level = properties[0];
            String session = properties[1];
            String semester = properties[2];
            int totalUnits = Integer.parseInt(properties[3]);
            double GP = Double.parseDouble(properties[4]);

            GradeEntry gradeEntry = new GradeEntry(courses, units, grades,
                    level, session, semester, totalUnits, GP
            );

            LiveData<GradeEntry> gradeEntryLiveData = mDb.gradeDao()
                    .loadTaskByProperties(level, session, semester);

            if (gradeEntryLiveData.getValue() == null) {
                mDb.gradeDao().insertGrade(gradeEntry);

                Log.d(TAG, "Inserted successfully");
            } else {
                Log.d(TAG, "Existing aiidy");
            }
            NavUtils.navigateUpFromSameTask(OverviewActivity.this);
        }
    };

    /*private final View.OnClickListener updateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Putting the level first, followed by session and the semester
            // Helps in easy sorting of the grades.
            final String details = Helper.STRING_SPLIT +mLevel+ Helper.STRING_SPLIT + mSession;

            // Initialise selection arguments and Uri
            //mSelectionArgs = new String[]{details};
            mUri = Uri.withAppendedPath(Helper.GPEntry.CONTENT_URI, details + Helper.STRING_SPLIT +mSemester);

            StringBuilder courses = new StringBuilder();
            StringBuilder units = new StringBuilder();
            StringBuilder grades = new StringBuilder();
            int numberOfGrades = mGradeEntry.size();

            // Loop through the Grades
            for (int i = 0; i < numberOfGrades; i++) {
                GradeEntry gradeEntry = mGradeEntry.get(i);
                courses.append(Helper.STRING_SPLIT).append(gradeEntry.getCourse());
                units.append(Helper.STRING_SPLIT).append(gradeEntry.getUnit());
                grades.append(Helper.STRING_SPLIT).append(gradeEntry.getGrade());
            }

            // Initialise content values and put the appropriate pairs
            final ContentValues values = new ContentValues();
            values.put(GPEntry.COLUMN_COURSES, courses.toString());
            values.put(GPEntry.COLUMN_UNITS, units.toString());
            values.put(GPEntry.COLUMN_GRADES, grades.toString());
            values.put(GPEntry.COLUMN_DETAILS, details);
            values.put(GPEntry.COLUMN_GP, GP);
            values.put(GPEntry.COLUMN_TU, GradeEntry.getTotalUnits());
            values.put(GPEntry.COLUMN_SEMESTER, mSemester);

            if (details.equals(mInitialDetailsToEdit)) {
                // When the initial detail is same as current details
                // Update the corresponding rows with the values
                int noOfRowsUpdated = getContentResolver()
                        .update(mUri, values, GPEntry.COLUMN_DETAILS +"= ?", new String[] {details} );

                if (noOfRowsUpdated > 0) {
                    goHome();
                }

            } else{

                // When the current detail differs
                // Check to see if a row with this detail already exist
                // Check if the current details already exist in the db
                Cursor c = getContentResolver().query(mUri,
                        new String[] {Helper.GPEntry._ID},
                        null,
                        null,
                        null);

                // When the new defined details already exist
                assert c != null;
                if (c.getCount() == 1) {
                    // Create an AlertDialog.Builder and set the message, and click listeners
                    // for the positive and negative buttons on the dialog.
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage(R.string.details_already_exist);

                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Delete the initial details row
                            // Update the current details row
                            int rowDeleted = getContentResolver().delete(mUri, Helper.GPEntry.COLUMN_DETAILS +"= ?", new String[] { mInitialDetailsToEdit } );

                            if (rowDeleted > 0) {
                                int noOfRowsUpdated = getContentResolver().update(mUri, values, Helper.GPEntry.COLUMN_DETAILS +"= ?", new String[] { details } );

                                if (noOfRowsUpdated > 0) {
                                    Toast.makeText(OverviewActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                                    goHome();
                                }
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
                else{
                    // Current details doesn't exist yet
                    // Initial details needs to be updated to a new details

                    int rowDeleted = getContentResolver().delete(mUri, Helper.GPEntry.COLUMN_DETAILS +"= ?", new String[] { mInitialDetailsToEdit } );

                    if (rowDeleted > 0) {

                        mUri = getContentResolver().insert(mUri, values);

                        if (mUri != null) {
                            goHome();
                        } else{
                            Log.i("###", "Update? Inserting not successful!");
                        }
                    } else{
                        Log.i("###", "Update? Inserting not successful!");
                    }
                }

                c.close();
            }
        }
    };*/

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
