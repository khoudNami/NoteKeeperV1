package com.zacademy.notekeeperv1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NoteReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_ID = "com.jwhh.jim.notekeeper.extra.NOTE_ID";
    public static final String EXTRA_NOTE_TITLE = "com.jwhh.jim.notekeeper.extra.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.jwhh.jim.notekeeper.extra.NOTE_TEXT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);

        NoteReminderNotificationV.notify(context, noteTitle, noteText, noteId);

    }
}