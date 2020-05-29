package com.example.gpcalculator.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.gpcalculator.data.GPContract.GPEntry;
import com.example.gpcalculator.data.GPContract.GPConstants;

public class GPProvider extends ContentProvider {

    GPDbHelper mDbHelper;

    // Code matcher for respective possible content uri
    private static final int GP_ALL = 100;
    private static final int GP_ONE = 101;

    // URI matcher initialisation
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // matches content://com.example.gpcalculator/gp
        sUriMatcher.addURI(GPContract.CONTENT_AUTHORITY, GPContract.PATH_GP, GP_ALL);

        // matches content://com.example.gpcalculator/gp/ String / String...
        // This is done because of the / im session e.g. 2019/2020
        sUriMatcher.addURI(GPContract.CONTENT_AUTHORITY, GPContract.PATH_GP + "/*/*", GP_ONE);
    }

    @Override
    public boolean onCreate() {
        // Initialise the global GPDbHelper object
        // So that other methods can access it
        mDbHelper = new GPDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case GP_ALL:
                cursor= db.query(
                        GPEntry.TABLE_NAME, // table
                        null, // columns
                        selection,
                        selectionArgs,
                        null,
                        null,
                        GPEntry.COLUMN_DETAILS +" ASC",
                        null); // orders the returned rows by details);
                break;
            case GP_ONE:
                String details = GPConstants.extractDetailsFromUri(uri);

                cursor = db.query(GPEntry.TABLE_NAME,
                        null,
                        GPEntry.COLUMN_DETAILS +" = ?",
                        new String[] { details },
                        null,
                        null,
                        null);
                break;
        }

        // Set the Notification Uri for the cursor
        // so we know what content uri the cursor was created for
        // if the data in the uri does change, then we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        String details = GPConstants.extractDetailsFromUri(uri);

        // This query returns a cursor which help when confirm
        // if a GP is already available for the selected session/semester/level
        // With a limit = "1, the amount of time required is subtly reduced
        Cursor cursor = database.query(GPEntry.TABLE_NAME,
                new String[] {GPEntry._ID},
                GPEntry.COLUMN_DETAILS +" = ?",
                new String[] { details },
                null,
                null,
                null,
                "1");

        if (cursor.getCount() == 0) {
            // New session/semester/level is being inserted
            // Insert the new gp with the given values
            long id = database.insert(GPEntry.TABLE_NAME, null, values);

            if (id == -1) {
                Log.i("###", "Data wasn't inserted successfully");
            }

            // Notify all listener that the cursor/data has changed for the gp CONTENT URI
            // This primarily notifies the CursorAdapter class
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        } else {
            return null;
        }

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case GP_ONE:
                int rowTotal = db.delete(GPEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return rowTotal;
        }

        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsTotal = database.update(GPEntry.TABLE_NAME, values, selection, selectionArgs);

        // Notify all listener that the cursor/data has changed for the gp CONTENT URI
        // This primarily notifies the CursorAdapter class
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsTotal;
    }
}
