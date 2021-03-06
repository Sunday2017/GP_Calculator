package com.example.gpcalculator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gpcalculator.data.GPContract.GPConstants;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gpcalculator.data.GPContract.GPEntry;
import com.example.gpcalculator.data.GPContract;

import java.util.ArrayList;
import java.util.List;

public class OverviewActivity extends AppCompatActivity {

    List<Grade> mGrades;
    int mLevel, mTotalUnits;
    double GP;
    String mSession, mSemester;
    Uri mUri;
    String mInitialDetailsToEdit, mInitSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        Intent i = getIntent();

        // ArrayList<Grade> dt = i.getParcelableArrayListExtra("GRADE");

        Button saveUpdateEditButton = (Button) findViewById(R.id.save_update_edit_btn);

        if (i != null) {

            mGrades = i.getParcelableArrayListExtra("GRADE");
            GP = i.getDoubleExtra(GPConstants.KEY_CALCULATED_GP, 0);
            mLevel = i.getIntExtra(GPConstants.KEY_LEVEL, 0);
            mSemester = i.getStringExtra(GPConstants.KEY_SEMESTER);
            mSession = i.getStringExtra(GPConstants.KEY_SESSION);
            mTotalUnits = i.getIntExtra(GPConstants.KEY_TOTAL_UNITS, 0);
            String stat = i.getStringExtra(GPConstants.KEY_GRADES_STAT);

            final String details = GPConstants.STRING_SPLIT +mLevel+ GPConstants.STRING_SPLIT +mSession;

            mUri = Uri.withAppendedPath(GPEntry.CONTENT_URI, details + GPConstants.STRING_SPLIT +mSemester);

            String mode = i.getStringExtra(GPConstants.KEY_MODE);

            if (mode.equals(getResources().getString(R.string.add_new))) {

                saveUpdateEditButton.setText(R.string.save_btn);
                setTitle(R.string.add_new);

                saveUpdateEditButton.setOnClickListener(onSaveListener);

            } else if (mode.equals(getResources().getString(R.string.edit))) {

                saveUpdateEditButton.setText(R.string.update_btn);
                setTitle(R.string.edit);

                mInitialDetailsToEdit = i.getStringExtra(GPConstants.KEY_INIT_DETAILS);
                mInitSemester = i.getStringExtra(GPConstants.KEY_INIT_SEMESTER);

                saveUpdateEditButton.setOnClickListener(updateListener);

            } else if (mode.equals(GPConstants.STRING_VIEW_MODE)) {

                saveUpdateEditButton.setText(R.string.edit);
                setTitle(R.string.details);

                saveUpdateEditButton.setOnClickListener(editListener);
            }

            TextView gpTextView = (TextView) findViewById(R.id.calculated_gp);
            TextView gpClassTextView = (TextView) findViewById(R.id.gp_class);
            TextView levelTextView = (TextView) findViewById(R.id.user_level);
            TextView sessionTextView = (TextView) findViewById(R.id.user_session);
            TextView semesterTextView = (TextView) findViewById(R.id.user_semester);
            TextView totalUnitsTextView = (TextView) findViewById(R.id.user_total_units);
            TextView statTextView = (TextView) findViewById(R.id.user_grades_stat);

            String gpClassGrade = GPConstants.getGradeClass(GP);

            gpTextView.setText("" +GP);
            gpClassTextView.setText(gpClassGrade);
            levelTextView.setText(getString(R.string.level) +" "+ mLevel);
            sessionTextView.setText(getString(R.string.session) +" "+ mSession);
            semesterTextView.setText(getString(R.string.semester) +" "+ mSemester);
            totalUnitsTextView.setText(getString(R.string.total_units) +" "+ mTotalUnits);
            statTextView.setText(getString(R.string.stat) +" "+ stat);
        }

        //saveUpdateEditButton.setOnClickListener(onSaveListener);
    }

    View.OnClickListener editListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent i = new Intent(OverviewActivity.this, GPCalcActivity.class);
            i.putExtra(GPConstants.KEY_SEMESTER, mSemester);
            i.setData(mUri);

            startActivity(i);
        }
    };

    View.OnClickListener onSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            // details is a combination of Level and Session
            final String details = GPConstants.extractExtrasFromUri(mUri)[0];

            String courses = "";
            String units = "";
            String grades = "";
            int numberOfGrades = mGrades.size();

            // Extract all the grades
            for (int i = 0; i < numberOfGrades; i++) {
                Grade grade = mGrades.get(i);
                courses += GPConstants.STRING_SPLIT + grade.getCourse();
                units += GPConstants.STRING_SPLIT + grade.getUnit();
                grades += GPConstants.STRING_SPLIT + grade.getGrade();
            }

            // Initialise content values and put the appropriate pairs
            final ContentValues values = new ContentValues();
            values.put(GPEntry.COLUMN_COURSES, courses);
            values.put(GPEntry.COLUMN_UNITS, units);
            values.put(GPEntry.COLUMN_GRADES, grades);
            values.put(GPEntry.COLUMN_DETAILS, details);
            values.put(GPEntry.COLUMN_GP, GP);
            values.put(GPEntry.COLUMN_TU, Grade.getTotalUnits());
            values.put(GPEntry.COLUMN_SEMESTER, mSemester);

            // Insert the value in the db
            Uri uriCopy = getContentResolver().insert(mUri, values);

            // When uri is successfully saved
            if (uriCopy != null) {
                Toast.makeText(v.getContext(), "Saved successfully", Toast.LENGTH_SHORT).show();

                goHome();

            } else{
                // When uri's details is already in the db
                showDialogForUpdate(values, details);
            }
        }
    };

    View.OnClickListener updateListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Putting the level first, followed by session and the semester
            // Helps in easy sorting of the grades.
            final String details = GPConstants.STRING_SPLIT +mLevel+ GPConstants.STRING_SPLIT + mSession;

            // Initialise selection arguments and Uri
            //mSelectionArgs = new String[]{details};
            mUri = Uri.withAppendedPath(GPContract.GPEntry.CONTENT_URI, details + GPConstants.STRING_SPLIT +mSemester);

            String courses = "";
            String units = "";
            String grades = "";
            int numberOfGrades = mGrades.size();

            // Loop through the Grades
            for (int i = 0; i < numberOfGrades; i++) {
                Grade grade = mGrades.get(i);
                courses += GPContract.GPConstants.STRING_SPLIT + grade.getCourse();
                units += GPContract.GPConstants.STRING_SPLIT + grade.getUnit();
                grades += GPContract.GPConstants.STRING_SPLIT + grade.getGrade();
            }

            // Initialise content values and put the appropriate pairs
            final ContentValues values = new ContentValues();
            values.put(GPEntry.COLUMN_COURSES, courses);
            values.put(GPEntry.COLUMN_UNITS, units);
            values.put(GPEntry.COLUMN_GRADES, grades);
            values.put(GPEntry.COLUMN_DETAILS, details);
            values.put(GPEntry.COLUMN_GP, GP);
            values.put(GPEntry.COLUMN_TU, Grade.getTotalUnits());
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
                        new String[] {GPContract.GPEntry._ID},
                        null,
                        null,
                        null);

                // When the new defined details already exist
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
                            int rowDeleted = getContentResolver().delete(mUri, GPContract.GPEntry.COLUMN_DETAILS +"= ?", new String[] { mInitialDetailsToEdit } );

                            if (rowDeleted > 0) {
                                int noOfRowsUpdated = getContentResolver().update(mUri, values, GPContract.GPEntry.COLUMN_DETAILS +"= ?", new String[] { details } );

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

                    int rowDeleted = getContentResolver().delete(mUri, GPContract.GPEntry.COLUMN_DETAILS +"= ?", new String[] { mInitialDetailsToEdit } );

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
    };

    private void goHome() {
        Intent i = new Intent(OverviewActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void showDialogForUpdate(final ContentValues values, final String details) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.details_already_exist);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int noOfRowsUpdated = getContentResolver().update(mUri, values, GPContract.GPEntry.COLUMN_DETAILS +"= ?", new String[] {details} );

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
    }
}
