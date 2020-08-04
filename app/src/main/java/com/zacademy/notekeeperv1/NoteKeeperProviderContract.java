package com.zacademy.notekeeperv1;

import android.net.Uri;
import android.provider.BaseColumns;

import java.net.URI;

public final class NoteKeeperProviderContract {
    private NoteKeeperProviderContract() {    }

    public static final String AUTHORITY = "com.zacademy.notekeeperv1.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    //define column constants in interfaces so as to avoid redundancy of repeating same columns
    //constants in table nested class. So the nested classes will implement these interfaces
    protected interface CoursesIdColumns {
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    protected interface CoursesColumns {
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NotesColumns {
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
    }

    public static final class Courses implements BaseColumns, CoursesIdColumns, CoursesColumns {
        public static final String PATH = "courses";
        // content://com.jwwh.jim.notekeeper.provider/courses
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes implements BaseColumns, CoursesIdColumns, NotesColumns {
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }


}
