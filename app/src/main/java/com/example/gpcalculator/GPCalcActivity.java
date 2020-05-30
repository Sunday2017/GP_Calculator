package com.example.gpcalculator;

import com.example.gpcalculator.data.GPContract.GPConstants;
import com.example.gpcalculator.data.GPContract.GPEntry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class GPCalcActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private LinearLayout mFormContainer;
    private Spinner mLevelSpinner, mSessionSpinner, mSemesterSpinner, mTotalUnitsSpinner;

    private ArrayList<Grade> mGrades;
    private Uri mUri;
    private String mMode, mInitialDetailsToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpcalc);

        // Initialising variables
        mFormContainer = (LinearLayout) findViewById(R.id.form_container);
        mLevelSpinner = (Spinner) findViewById(R.id.level_spinner);
        mSessionSpinner = (Spinner) findViewById(R.id.session_spinner);
        mSemesterSpinner = (Spinner) findViewById(R.id.semester_spinner);
        mTotalUnitsSpinner = (Spinner) findViewById(R.id.total_units_spinner);
        Button addRowButton = (Button) findViewById(R.id.add_row_btn);
        Button calcButton = (Button) findViewById(R.id.calc_gp);
        Button delRowButton = (Button) findViewById(R.id.delete_row_btn);

        // Setting Listeners on buttons
        addRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRowToContainer("", GPConstants.NONE, GPConstants.NONE);
            }
        });

        delRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delRowFromContainer();
            }
        });
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extractGPInputs();
            }
        });

        Intent i = getIntent();

        mUri = i.getData();

        if (mUri == null) {

            // Set the title
            this.setTitle(R.string.add_new);

            // Set the mode
            mMode = getResources().getString(R.string.add_new);

            // Call method to setUpSpinners by populating 'em with the required data
            setupDetailsSpinners(GPConstants.NONE, GPConstants.NONE, GPConstants.NONE, GPConstants.NONE);

            // Add to first row {courses, units, grades} to the container}
            addRowToContainer("", GPConstants.NONE, GPConstants.NONE);
        } else {

            // Set the title
            this.setTitle(R.string.edit);

            // Set the mode
            mMode = getResources().getString(R.string.edit);

            getSupportLoaderManager().initLoader(1, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Need to persist
        // mMode, mUri and User's input choices

        outState.putString(GPConstants.KEY_MODE, mMode);
        outState.putString(GPConstants.KEY_INIT_DETAILS, mInitialDetailsToEdit);
    }

    private void delRowFromContainer() {
        if (mFormContainer.getChildCount() > 1) {
            // The last Child in  in the form container
            // Making sure that there's at least one left
            mFormContainer.removeViewAt(mFormContainer.getChildCount() - 1);
        }
    }

    private void extractGPInputs() {
        String level = mLevelSpinner.getSelectedItem().toString();
        String session = mSessionSpinner.getSelectedItem().toString();
        String semester = mSemesterSpinner.getSelectedItem().toString();
        String totalUnits = mTotalUnitsSpinner.getSelectedItem().toString();

        // Making sure that none of the above Strings is "None"
        if (level.equals(GPConstants.NONE)) {
            Toast.makeText(this, "Invalid Level", Toast.LENGTH_SHORT).show();
            return;
        } else if (session.equals(GPConstants.NONE)) {
            Toast.makeText(this, "Invalid Session", Toast.LENGTH_SHORT).show();
            return;
        } else if (semester.equals(GPConstants.NONE)) {
            Toast.makeText(this, "Invalid Semester", Toast.LENGTH_SHORT).show();
            return;
        } else if (totalUnits.equals(GPConstants.NONE)) {
            Toast.makeText(this, "Invalid Total Units", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialising the List<Grades>
        // Making sure the list of grades is empty before adding grades
        mGrades = new ArrayList<>();

        // get FormContainer child count
        int childCount = mFormContainer.getChildCount();

        int cumulativeUnit = 0;
        String allGradesString = "";

        // Initialise the total GradePoint obtained
        double totalGradePoint = 0;

        // Loop through each row
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowContainer = (LinearLayout) mFormContainer.getChildAt(i);

            EditText courseEditText = (EditText) rowContainer.getChildAt(0);
            Spinner unitSpinner = (Spinner) rowContainer.getChildAt(1);
            Spinner gradeSpinner = (Spinner) rowContainer.getChildAt(2);

            // Get the values inputted/selected
            String course = courseEditText.getText().toString().trim();
            String unit = unitSpinner.getSelectedItem().toString();
            String grade = gradeSpinner.getSelectedItem().toString();

            // Making sure every course has a name (nonn empty), unit isn't NONE, grade isn't NONE
            if (course.equals("") || unit.equals(GPConstants.NONE) || grade.equals(GPConstants.NONE)) {
                Toast.makeText(this, "Some column(s) is empty/none", Toast.LENGTH_SHORT).show();
                return;
            }

            // Restricting the input ###
            if (course.indexOf(GPConstants.STRING_SPLIT) > -1) {
                Toast.makeText(this, "Entry ### is restricted", Toast.LENGTH_SHORT).show();
                return;
            }

            int actualUnit = Integer.parseInt(unit);
            cumulativeUnit += actualUnit;
            allGradesString += GPConstants.STRING_SPLIT + grade;

            // get the gradePoint corresponding to each grade obtained;  A:5, B:4, ...
            int gradePoint = GPConstants.getGradePoint(grade);
            // Cumulate the total
            totalGradePoint += actualUnit * gradePoint;

            // Add extracted grade details
            mGrades.add(new Grade(course, actualUnit, grade));
        }

        int totalUnitsInt = Integer.parseInt(totalUnits);
        String stat = GPConstants.getGradesStat(allGradesString);

        // Set the static variables of the class: level, session, semester, totalUnits, stat
        Grade.setLevel(Integer.parseInt(level));
        Grade.setSession(session);
        Grade.setSemester(semester);
        Grade.setTotalUnits(totalUnitsInt);
        Grade.setStat(stat);

        // cumulative unit isn't equal to the selected total units?
        if (cumulativeUnit > totalUnitsInt) {
            Toast.makeText(this, "Total Units of course(s) is more than Total Units selected", Toast.LENGTH_SHORT).show();
            return;
        } else if (cumulativeUnit < totalUnitsInt) {
            Toast.makeText(this, "Total Units of course(s) is less than Total Units selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate the GP to 2 decimal places
        // set the GP
        double GP = (double) Math.round(totalGradePoint / totalUnitsInt * 100) / 100;
        Grade.setGP(GP);

        // Start OverviewActivity
        showOverview();
    }

    private void extractRawInputs() {
        String level = mLevelSpinner.getSelectedItem().toString();
        String session = mSessionSpinner.getSelectedItem().toString();
        String semester = mSemesterSpinner.getSelectedItem().toString();
        String totalUnits = mTotalUnitsSpinner.getSelectedItem().toString();

        // Initialising the List<Grades>
        // Making sure the list of grades is empty before adding grades
        mGrades = new ArrayList<>();

        // get FormContainer child count
        int childCount = mFormContainer.getChildCount();

        // Loop through each row
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowContainer = (LinearLayout) mFormContainer.getChildAt(i);

            EditText courseEditText = (EditText) rowContainer.getChildAt(0);
            Spinner unitSpinner = (Spinner) rowContainer.getChildAt(1);
            Spinner gradeSpinner = (Spinner) rowContainer.getChildAt(2);

            // Get the values inputted/selected
            String course = courseEditText.getText().toString();
            String unit = unitSpinner.getSelectedItem().toString();
            String grade = gradeSpinner.getSelectedItem().toString();

            int actualUnit = unit.equals(GPConstants.NONE) ? 0 : Integer.parseInt(unit);

            // Add extracted grade details
            mGrades.add(new Grade(course, actualUnit, grade));
        }

        int totalUnitsInt = totalUnits.equals(GPConstants.NONE) ? 0 : Integer.parseInt(totalUnits);
        int levelInt = level.equals(GPConstants.NONE) ? 0 : Integer.parseInt(level);

        // Set the static variables of the class: level, session, semester, totalUnits, stat
        Grade.setLevel(levelInt);
        Grade.setSession(session);
        Grade.setSemester(semester);
        Grade.setTotalUnits(totalUnitsInt);
    }

    private void showOverview () {

        // GP calculated? Now, show Overview...
        Intent i = new Intent(this, OverviewActivity.class);
        i.putExtra(GPConstants.KEY_CALCULATED_GP, Grade.getGP());
        i.putExtra(GPConstants.KEY_LEVEL, Grade.getLevel());
        i.putExtra(GPConstants.KEY_SESSION, Grade.getSession());
        i.putExtra(GPConstants.KEY_SEMESTER, Grade.getSemester());
        i.putExtra(GPConstants.KEY_TOTAL_UNITS, Grade.getTotalUnits());
        i.putExtra(GPConstants.KEY_MODE, mMode);
        i.putExtra(GPConstants.KEY_INIT_DETAILS, mInitialDetailsToEdit);
        i.putExtra(GPConstants.KEY_GRADES_STAT, Grade.getStat());
        i.putExtra("GRADE", mGrades);

        startActivity(i);
    }

    private void setupDetailsSpinners (String level, String session, String semester, String totalUnits){
        // Setting up the ArrayAdapter
        ArrayAdapter levelSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.level_option, android.R.layout.simple_spinner_item);
        ArrayAdapter sessionSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.session_option, android.R.layout.simple_spinner_item);
        ArrayAdapter semesterSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.semester_option, android.R.layout.simple_spinner_item);
        ArrayAdapter totalUnitsSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.total_units_option, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        levelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sessionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        semesterSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        totalUnitsSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mLevelSpinner.setAdapter(levelSpinnerAdapter);
        mSessionSpinner.setAdapter(sessionSpinnerAdapter);
        mSemesterSpinner.setAdapter(semesterSpinnerAdapter);
        mTotalUnitsSpinner.setAdapter(totalUnitsSpinnerAdapter);

        int levelPosition = GPConstants.getPositionInSpinner(level, getResources().getStringArray(R.array.level_option));
        int sessionPosition = GPConstants.getPositionInSpinner(session, getResources().getStringArray(R.array.session_option));
        int semesterPosition = GPConstants.getPositionInSpinner(semester, getResources().getStringArray(R.array.semester_option));
        int totalUnitsPosition = GPConstants.getPositionInSpinner(totalUnits, getResources().getStringArray(R.array.total_units_option));

        mLevelSpinner.setSelection(levelPosition);
        mSessionSpinner.setSelection(sessionPosition);
        mSemesterSpinner.setSelection(semesterPosition);
        mTotalUnitsSpinner.setSelection(totalUnitsPosition);
    }

    private void addRowToContainer (String course, String unit, String grade){
        // Adding a LinearLayout containing an EditText and two Spinners to the container
        LinearLayout parent = new LinearLayout(this);
        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.HORIZONTAL);

        EditText courseEditText = new EditText(this);
        courseEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Spinner unitSpinner = new Spinner(this);
        courseEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        courseEditText.setText(course);

        Spinner gradeSpinner = new Spinner(this);
        courseEditText.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        parent.addView(courseEditText);
        parent.addView(unitSpinner);
        parent.addView(gradeSpinner);

        // Setup Spinners with the appropriate selections
        setupRowSpinners(unitSpinner, gradeSpinner, unit, grade);

        // Add view to the form container...
        mFormContainer.addView(parent);
    }

    private void setupRowSpinners (Spinner unitSpinner, Spinner gradeSpinner, String
    unitSelected, String gradeSelected){
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter unitSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.units_option, android.R.layout.simple_spinner_item);
        ArrayAdapter gradeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.grades_option, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        unitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        gradeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        unitSpinner.setAdapter(unitSpinnerAdapter);
        gradeSpinner.setAdapter(gradeSpinnerAdapter);

        // This gets the position of the unitSelected, gradeSelected values in each spinner array
        int unitsPosition = GPConstants.getPositionInSpinner(unitSelected, getResources().getStringArray(R.array.units_option));
        int gradesPosition = GPConstants.getPositionInSpinner(gradeSelected, getResources().getStringArray(R.array.grades_option));

        // Set the selection(position) appropriately
        unitSpinner.setSelection(unitsPosition);
        gradeSpinner.setSelection(gradesPosition);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader ( int id, @Nullable Bundle args){

        return new CursorLoader(getApplicationContext(), mUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished (@NonNull Loader <Cursor> loader, Cursor cursor){

        if (cursor != null && mFormContainer.getChildCount() == 0) {
            // Move to first
            cursor.moveToFirst();

            // Since all grades for a particular {level, session, semester} are stored in a single row
            // Get the courses, units, grades
            String courses = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_COURSES));
            String units = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_UNITS));
            String grades = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_GRADES));
            String semester = cursor.getString(cursor.getColumnIndex(GPEntry.COLUMN_SEMESTER));

            mInitialDetailsToEdit = GPConstants.extractExtrasFromUri(mUri)[0];

            // For these arrays, the index 0 is ""
            // Arrays (each with equal length) for courses, units and grades
            String[] coursesArray = courses.split(GPConstants.STRING_SPLIT);
            String[] unitsArray = units.split(GPConstants.STRING_SPLIT);
            String[] gradesArray = grades.split(GPConstants.STRING_SPLIT);

            // details array: level=[1], session=[2], semester=[3]
            String[] detailsArray = mInitialDetailsToEdit.split(GPConstants.STRING_SPLIT);

            int totalUnits = 0;

            // Calculate totalUnits and add rows...
            for (int i = 1; i < unitsArray.length; i++) {
                // This loops starts from 1 because [0] = ""
                totalUnits += Integer.parseInt(unitsArray[i]);

                addRowToContainer(coursesArray[i], unitsArray[i], gradesArray[i]);
            }

            // Setup spinners for Level, Session, Semester and Total Units
            setupDetailsSpinners(detailsArray[1], // Level
                    detailsArray[2], // session
                    semester, // semester
                    "" + totalUnits); // Total Units
        }
    }

    @Override
    public void onLoaderReset (@NonNull Loader < Cursor > loader) {

    }
}
