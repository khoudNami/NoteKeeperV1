package com.zacademy.notekeeperv1;

import android.app.IntentService;
import android.content.Intent;
import android.os.StrictMode;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NoteBackupService extends IntentService {

    public static final String EXTRA_COURSE_ID = "com.zacademy.notekeeperv1.extra.COURSE_ID";

    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra(EXTRA_COURSE_ID);
            NoteBackup.doBackup(this, backupCourseId);
        }
    }
}
