package com.example.gpcalculator.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.gpcalculator.data.GPContract.GPEntry;


public class GPDbHelper extends SQLiteOpenHelper {
    // database properties
    private static final String DATABASE_NAME = "gp.db";
    private static final int DATABASE_VERSION = 2;

    // Create table sql code
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + GPEntry.TABLE_NAME +"("
                    + GPEntry._ID +" INTEGER PRIMARY KEY, "
                    + GPEntry.COLUMN_COURSES +" TEXT, "
                    + GPEntry.COLUMN_UNITS +" TEXT, "
                    + GPEntry.COLUMN_GRADES +" TEXT, "
                    + GPEntry.COLUMN_GP + " REAL, "
                    + GPEntry.COLUMN_TU + " INT, "
                    + GPEntry.COLUMN_SEMESTER + " TEXT, "
                    + GPEntry.COLUMN_DETAILS +" TEXT" +")";

    // Delete table sql code
    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + GPEntry.TABLE_NAME;

    // Helper constructor
    public GPDbHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This is create a new table if none exists yet
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    // Typically called when the DATABASE_VERSION is changed (usually incrementally)
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }
}
