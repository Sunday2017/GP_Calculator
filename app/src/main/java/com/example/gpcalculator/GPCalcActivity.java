package com.example.gpcalculator;

import com.example.gpcalculator.data.Helper;
import com.example.gpcalculator.data.GradeEntry;

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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;

public class GPCalcActivity extends AppCompatActivity{

    private LinearLayout mFormContainer;
    private Spinner mLevelSpinner, mSessionSpinner, mSemesterSpinner, mTotalUnitsSpinner;

    private String mMode;
    private String initLevel, initSession, initSemester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpcalc);

        // Initialising variables
        mFormContainer = findViewById(R.id.form_container);
        mLevelSpinner = findViewById(R.id.level_spinner);
        mSessionSpinner = findViewById(R.id.session_spinner);
        mSemesterSpinner = findViewById(R.id.semester_spinner);
        mTotalUnitsSpinner = findViewById(R.id.total_units_spinner);
        Button addRowButton = findViewById(R.id.add_row_btn);
        Button calcButton = findViewById(R.id.calc_gp);
        Button delRowButton = findViewById(R.id.delete_row_btn);

        // Setting Listeners on buttons
        addRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRowToContainer("", Helper.NONE, Helper.NONE);
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

        Intent intent = getIntent();

        if (intent != null) {

            if (savedInstanceState == null){
                mMode = intent.getStringExtra(Helper.KEY_MODE);

                if (mMode.equals(Helper.MODE_ADD_NEW)) {
                    setupDetailsSpinners(Helper.NONE, Helper.NONE, Helper.NONE, Helper.NONE);
                    addRowToContainer("MTH", "2", "A");
                }else if (mMode.equals(Helper.MODE_VIEW)) {
                    String[] courses = intent.getStringArrayExtra(Helper.KEY_COURSES);
                    String[] units =  intent.getStringArrayExtra(Helper.KEY_UNITS);
                    String[] grades = intent.getStringArrayExtra(Helper.KEY_GRADES);
                    String[] properties = intent.getStringArrayExtra(Helper.KEY_PROPERTIES);

                    initLevel = properties[1];
                    initSession = properties[2];
                    initSemester = properties[3];

                    setupDetailsSpinners(initLevel, initSession, initSemester, properties[4]);
                    populateFormContainer(courses, units, grades);
                }

            }else {
                setupDetailsSpinners(Helper.NONE, Helper.NONE, Helper.NONE, Helper.NONE);
                mMode = savedInstanceState.getString(Helper.KEY_MODE);

                String[] courses = savedInstanceState.getStringArray(Helper.KEY_COURSES);
                String[] units =  savedInstanceState.getStringArray(Helper.KEY_UNITS);
                String[] grades = savedInstanceState.getStringArray(Helper.KEY_GRADES);

                populateFormContainer(courses, units, grades);
            }

            if (mMode.equals(Helper.MODE_ADD_NEW)) {
                // Set the title and the mode
                this.setTitle(R.string.add_new);
            }else{
                // Set the title and the mode
                this.setTitle(R.string.edit);
            }
        }
    }

    private void populateFormContainer(String[] courses, String[] units, String[] grades){
        int count = courses.length;

        for (int i = 0; i < count; i++) {
            String course = courses[i];
            String unit = units[i];
            String grade = grades[i];

            addRowToContainer(course, unit, grade);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // get FormContainer child count
        int childCount = mFormContainer.getChildCount();

        String[] courses = new String[childCount];
        String[] units = new String[childCount];
        String[] grades = new String[childCount];

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

            courses[i] = course;
            units[i] = unit;
            grades[i] = grade;
        }

        outState.putString(Helper.KEY_MODE, mMode);
        outState.putStringArray(Helper.KEY_COURSES, courses);
        outState.putStringArray(Helper.KEY_UNITS, units);
        outState.putStringArray(Helper.KEY_GRADES, grades);
    }

    private void delRowFromContainer() {
        if (mFormContainer.getChildCount() > 1) {
            mFormContainer.removeViewAt(mFormContainer.getChildCount() - 1);
        }
    }

    private void extractGPInputs() {
        String level = mLevelSpinner.getSelectedItem().toString();
        String session = mSessionSpinner.getSelectedItem().toString();
        String semester = mSemesterSpinner.getSelectedItem().toString();
        String strTotalUnits = mTotalUnitsSpinner.getSelectedItem().toString();

        // Making sure that none of the above Strings is "None"
        if (level.equals(Helper.NONE)) {
            Toast.makeText(this, "Invalid Level", Toast.LENGTH_SHORT).show();
            return;
        } else if (session.equals(Helper.NONE)) {
            Toast.makeText(this, "Invalid Session", Toast.LENGTH_SHORT).show();
            return;
        } else if (semester.equals(Helper.NONE)) {
            Toast.makeText(this, "Invalid Semester", Toast.LENGTH_SHORT).show();
            return;
        } else if (strTotalUnits.equals(Helper.NONE)) {
            Toast.makeText(this, "Invalid Total Units", Toast.LENGTH_SHORT).show();
            return;
        }

        // get FormContainer child count
        int childCount = mFormContainer.getChildCount();

        String[] courses = new String[childCount];
        String[] units = new String[childCount];
        String[] grades = new String[childCount];

        // Initialise the total GradePoint obtained
        int accumulatedGradePoint = 0;

        int selectedTotalUnits = 0;

        // Loop through each row
        for (int i = 0; i < childCount; i++) {
            LinearLayout rowContainer = (LinearLayout) mFormContainer.getChildAt(i);

            EditText courseEditText = (EditText) rowContainer.getChildAt(0);
            Spinner unitSpinner = (Spinner) rowContainer.getChildAt(1);
            Spinner gradeSpinner = (Spinner) rowContainer.getChildAt(2);

            // Get the values inputted/selected
            String course = courseEditText.getText().toString().trim();
            String strUnit = unitSpinner.getSelectedItem().toString();
            String grade = gradeSpinner.getSelectedItem().toString();

            // Making sure every course is none empty, unit isn't NONE, grade isn't NONE
            if (course.equals("") || strUnit.equals(Helper.NONE) || grade.equals(Helper.NONE)) {
                Toast.makeText(this, "Some column(s) is empty/none", Toast.LENGTH_SHORT).show();
                return;
            }

            int unit = Integer.parseInt(strUnit);
            courses[i] = course;
            units[i] = strUnit;
            grades[i] = grade;
            selectedTotalUnits += unit;

            // get the gradePoint corresponding to each grade obtained;  A:5, B:4, ...
            int gradePoint = Helper.getGradePoint(grade);

            // Cummulate the total
            accumulatedGradePoint += unit * gradePoint;
        }

        int totalUnits = Integer.parseInt(strTotalUnits);

        // cumulative unit isn't equal to the selected total units?
        if (selectedTotalUnits > totalUnits) {
            Toast.makeText(this, "Total Units of course(s) is more than Total Units selected", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedTotalUnits < totalUnits) {
            Toast.makeText(this, "Total Units of course(s) is less than Total Units selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate the GP to 2 decimal places
        // set the GP
        double GP = (double) Math.round(accumulatedGradePoint / selectedTotalUnits * 100) / 100;

        String[] properties = new String[5];
        properties[0] = String.valueOf(GP);
        properties[1] = level;
        properties[2] = session;
        properties[3] = semester;
        properties[4] = strTotalUnits;

        showOverview(courses, units, grades, properties);
    }

    private void showOverview (String[] courses, String[] units,
                               String[] grades, String[] properties) {

        String mode = mMode.equals(Helper.MODE_VIEW) ? Helper.MODE_UPDATE : mMode;

        String[] propertiesToPut;

        if (mMode.equals(Helper.MODE_VIEW)) {
            propertiesToPut = new String[8];

            propertiesToPut[0] = properties[0];
            propertiesToPut[1] = properties[1];
            propertiesToPut[2] = properties[2];
            propertiesToPut[3] = properties[3];
            propertiesToPut[4] = properties[4];
            propertiesToPut[5] = initLevel;
            propertiesToPut[6] = initSession;
            propertiesToPut[7] = initSemester;
        } else {
            propertiesToPut = properties;
        }

        // GP calculated? Now, show Overview...
        Intent i = new Intent(this, OverviewActivity.class);
        i.putExtra(Helper.KEY_MODE, mode);
        i.putExtra(Helper.KEY_COURSES, courses);
        i.putExtra(Helper.KEY_UNITS, units);
        i.putExtra(Helper.KEY_GRADES, grades);
        i.putExtra(Helper.KEY_PROPERTIES, propertiesToPut);
        startActivity(i);
    }

    private void setupDetailsSpinners (String level, String session, String semester, String totalUnits){


        // Setting up the ArrayAdapter
        ArrayAdapter<CharSequence> levelSpinnerAdapter = ArrayAdapter
                .createFromResource
                        (this, R.array.level_option, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> sessionSpinnerAdapter = ArrayAdapter
                .createFromResource(
                        this, R.array.session_option, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> semesterSpinnerAdapter = ArrayAdapter
                .createFromResource(
                        this, R.array.semester_option, android.R.layout.simple_spinner_item);

        ArrayAdapter<CharSequence> totalUnitsSpinnerAdapter = ArrayAdapter
                .createFromResource(
                        this, R.array.total_units_option, android.R.layout.simple_spinner_item);

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

        int levelPosition = Helper.getPositionInSpinner(
                level, getResources().getStringArray(R.array.level_option));
        int sessionPosition = Helper.getPositionInSpinner(
                session, getResources().getStringArray(R.array.session_option));
        int semesterPosition = Helper.getPositionInSpinner(
                semester, getResources().getStringArray(R.array.semester_option));
        int totalUnitsPosition = Helper.getPositionInSpinner(
                totalUnits, getResources().getStringArray(R.array.total_units_option));

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

    private void setupRowSpinners (Spinner unitSpinner, Spinner gradeSpinner, String unitSelected, String gradeSelected){
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
        int unitsPosition = Helper.getPositionInSpinner(unitSelected, getResources().getStringArray(R.array.units_option));
        int gradesPosition = Helper.getPositionInSpinner(gradeSelected, getResources().getStringArray(R.array.grades_option));

        // Set the selection(position) appropriately
        unitSpinner.setSelection(unitsPosition);
        gradeSpinner.setSelection(gradesPosition);
    }
}
