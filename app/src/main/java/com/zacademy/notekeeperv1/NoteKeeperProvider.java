package com.zacademy.notekeeperv1;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.zacademy.notekeeperv1.NoteKeeperDatabaseContract.CourseInfoEntry;

import static com.zacademy.notekeeperv1.NoteKeeperDatabaseContract.*;

public class NoteKeeperProvider extends ContentProvider {

    NoteKeeperOpenHelper mDbOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    static { //static initializer runs some code when type is loaded
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NoteKeeperProviderContract.Notes.PATH_EXPANDED, NOTES_EXPANDED);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {

        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;

            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //note_info JOIN course_info ON note_info.course_id = course_info.course_id
        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID);

        return db.query(tablesWithJoin, projection,
                null, null, null, null, sortOrder);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}