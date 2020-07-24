package com.zacademy.notekeeperv1;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import static com.zacademy.notekeeperv1.NoteKeeperDatabaseContract.*;

public class NoteActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int LOADER_NOTES = 0;
    private final String TAG = getClass().getSimpleName();

    public static final String NOTE_ID = "com.jwhh.jim.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.jwhh.jim.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final int ID_NOT_SET = -1;

    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;

    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private boolean mIsCancelling;
    private int mNoteId;

    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinnerCourses = (Spinner) findViewById(R.id.spinner_courses);
        mTextNoteTitle = (EditText) findViewById(R.id.text_note_title);
        mTextNoteText = (EditText) findViewById(R.id.text_note_text);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
                new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                new int[]{android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

        loadCourseData();

        readDisplayStateValues();

        if (savedInstanceState == null) {
            saveOriginalNoteValues();
        } else {
            restoreOriginalNoteValues(savedInstanceState);
        }

        if (!mIsNewNote)
            //    loadNoteData();
            getSupportLoaderManager().initLoader(LOADER_NOTES, null, this);
        Log.d(TAG, "onCreate");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIsCancelling) {
            Log.i(TAG, "Cancelling note at position: " + mNoteId);
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNoteId);
            } else {
                storePreviousNoteValues();
            }
        } else {
            saveNote();
        }
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {// called by the LoaderManager to request a loader that knows how to load our note data i.e CursorLoader
        CursorLoader loader = null;
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Toast.makeText(this, "Loader Reset", Toast.LENGTH_SHORT).show();
        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null)
                mNoteCursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if (id == R.id.action_next) {
            moveNext();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
        mAdapterCourses.changeCursor(cursor);
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }

        Log.i(TAG, "mNoteId: " + mNoteId);
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);

    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNoteId = dm.createNewNote();
//        mNote = dm.getNotes().get(mNoteId);
    }

    private void saveOriginalNoteValues() {
        if (mIsNewNote)
            return;
        mOriginalNoteCourseId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();
    }

    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);
    }

    private CursorLoader createLoaderNotes() {
        return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String selection = NoteInfoEntry._ID + " = ?";

                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                        selection, selectionArgs, null, null, null);
            }
        };
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
    }

    private void loadNoteData() {

        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                selection, selectionArgs, null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
    }

    private void displayNote() {//because its one record to be displayed, there is no need for while loop
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);

        int courseIndex = getIndexOfCourse(courseId);
        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourse(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        int courseRowIndex = 0;

        boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId))
                break;

            courseRowIndex++;
            more = cursor.moveToNext();

        }
        return courseRowIndex;
    }

    private void moveNext() {
        saveNote();
        ++mNoteId;
        mNote = DataManager.getInstance().getNotes().get(mNoteId);

        saveOriginalNoteValues();
        displayNote();
        invalidateOptionsMenu();
    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    private void sendEmail() {
        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
                course.getTitle() + "\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }


}


//
//    private final String TAG = getClass().getSimpleName();
//    public static final String NOTE_ID = "com.zacademy.notekeeperv1.NOTE_INFO_POSITION";
//    public static final int ID_NOT_SET = -1;
//    private NoteInfo mNote;
//    private boolean mIsNewNote;
//    private Spinner mSpinnerCourses;
//    private EditText mTextNoteTitle;
//    private EditText mTextNoteText;
//    private int mNoteId;
//    private boolean mIsCancelling;
//    private NoteActivityViewModel mViewModel;
//    private NoteKeeperOpenHelper mDbOpenHelper;
//    private Cursor mNoteCursor;
//    private int mCourseIdPos;
//    private int mNoteTitlePos;
//    private int mNoteTextPos;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_note);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        mDbOpenHelper = new NoteKeeperOpenHelper(this);
//
//        ViewModelProvider viewModelProvider = new ViewModelProvider(getViewModelStore(),
//                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
//
//        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
//
//        if (mViewModel.mIsNewlyCreated && savedInstanceState != null) {
//            mViewModel.restoreState(savedInstanceState);
//        }
//        mViewModel.mIsNewlyCreated = false;
//
//        mSpinnerCourses = findViewById(R.id.spinner_courses);
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//
//        //Create adapter to associate courses list with spinnerCourses Spinner.
//        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item,
//                courses);//list values read in by CourseInfo.toString()
//        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mSpinnerCourses.setAdapter(adapterCourses);
//
//        //Receive Intent from NoteListActivity; extract its extra; use the extra(Parcelable, then later position) to display the selected note from NoteListActivity in NoteActivity
//        readDisplayStateValues();
//
//        saveOriginalNoteValues();
//
//        mTextNoteTitle = findViewById(R.id.text_note_title);
//        mTextNoteText = findViewById(R.id.text_note_text);
//
//
//        //use intent extra values capture by readDisplayState, to display the note;
//        if (!mIsNewNote) {
//            loadNoteData();
//        }
//        Log.d(TAG, "NoteActivity onCreate() called");
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
////        Log.d("CALLED", "NoteActivity onStart() called");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
////        Log.d("CALLED", "NoteActivity onResume() called");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mIsCancelling) {
//            Log.i(TAG, "Cancelling note at position: " + mNoteId);
//            if (mIsNewNote) {
//                DataManager.getInstance().removeNote(mNoteId);
//            } else {
//                storePreviousNoteValues();
//            }
//
//        } else {
//            saveNote();
//        }
//
//        Log.d(TAG, "NoteActivity onPause() called");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
////        Log.d("CALLED", "NoteActivity onStop() called");
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
////        Log.d("CALLED", "NoteActivity onRestart() called");
//    }
//
//    @Override
//    protected void onDestroy() {
//        mDbOpenHelper.close();
//        super.onDestroy();
////        Log.d("CALLED", "NoteActivity onDestroy() called");
//    }
//
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        if (outState != null)
//            mViewModel.saveState(outState);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_send_mail) {
//            sendEmail();
//            return true;
//        } else if (id == R.id.action_cancel) {
//            mIsCancelling = true;
//            finish();
//            return true;
//        } else if (id == R.id.action_next) {
//            moveNext();
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_note, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        MenuItem item = menu.findItem(R.id.action_next);
//        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
//        item.setEnabled(mNoteId < lastNoteIndex);
//        return super.onPrepareOptionsMenu(menu);
//    }
//
//    private void readDisplayStateValues() {
//        Intent intent = getIntent();
//        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
//        mIsNewNote = mNoteId == ID_NOT_SET;
//        if (mIsNewNote) {
//            createNewNote();
//        }
//
//        Log.i(TAG, "mNoteNewPosition: " + mNoteId);
//        //mNote = DataManager.getInstance().getNotes().get(mNoteId);//make it member variable so that displayNotes() can also access it// List.get(position): Returns the element at the specified position in this list.
//    }
//
//    private void loadNoteData() {
//        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
//
//        String courseId = "android_intents";
//        String titleStart = "dynamic";
//
//        String selection = NoteInfoEntry._ID + " = ?";
//
//        String[] selectionArgs = {Integer.toString(mNoteId)};
//
//        String[] noteColumns = {
//                NoteInfoEntry.COLUMN_NOTE_TITLE,
//                NoteInfoEntry.COLUMN_NOTE_TEXT,
//                NoteInfoEntry.COLUMN_COURSE_ID};
//
//        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
//                selection, selectionArgs, null, null, null);
//
//        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
//        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
//        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
//
//        mNoteCursor.moveToNext();
//
//        displayNote();
//
//    }
//
//    private void displayNote() {
//
//        String courseId = mNoteCursor.getString(mCourseIdPos);
//        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
//        String noteText = mNoteCursor.getString(mNoteTextPos);
//
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
//        int courseIndex = courses.indexOf(course);
//        mSpinnerCourses.setSelection(courseIndex);
//        mTextNoteTitle.setText(noteTitle);
//        mTextNoteText.setText(noteText);
//    }
//
//    private void sendEmail() {
//        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
//        String subject = mTextNoteTitle.getText().toString();
//        String text = "Check out what I learned in the pluralsight \"" + course.getTitle() + "\"\n" + mTextNoteText.getText();
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("message/rfc2822");
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//        intent.putExtra(Intent.EXTRA_TEXT, text);
//        startActivity(intent);
//    }
//
//    private void saveNote() { // set the values of the note we currently have reference to
//        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
//        mNote.setTitle(mTextNoteTitle.getText().toString());
//        mNote.setText(mTextNoteText.getText().toString());
//    }
//
//    private void createNewNote() {
//        DataManager dm = DataManager.getInstance();
//        mNoteId = dm.createNewNote();
////        mNote = dm.getNotes().get(mNewNotePosition);
//    }
//
//    private void saveOriginalNoteValues() {
//        if (mIsNewNote)
//            return;
//        mViewModel.mOriginalNoteCourseId = mNote.getCourse().getCourseId();
//        mViewModel.mOriginalNoteTitle = mNote.getTitle();
//        mViewModel.mOriginalNoteText = mNote.getText();
//
//    }
//
//    private void storePreviousNoteValues() {
//        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mOriginalNoteCourseId);
//        mNote.setCourse(course);
//        mNote.setTitle(mViewModel.mOriginalNoteTitle);
//        mNote.setText(mViewModel.mOriginalNoteText);
//    }
//
//    private void moveNext() {
//        saveNote();
//        ++mNoteId;
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);
//        saveOriginalNoteValues();
//        displayNote();
//        invalidateOptionsMenu();
//    }


