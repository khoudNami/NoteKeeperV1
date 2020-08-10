package com.zacademy.notekeeperv1;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.zacademy.notekeeperv1.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        mNoteUploader = new NoteUploader(this);

        AsyncTask<JobParameters, Void, Void> task = new AsyncTask<JobParameters, Void, Void>() {
            @Override
            protected Void doInBackground(JobParameters... backgroundParameters) {
                JobParameters jobParams = backgroundParameters[0];

                String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
                Uri dataUri = Uri.parse(stringDataUri);

                mNoteUploader.doUpload(dataUri);

                if (!mNoteUploader.isCanceled())
                    jobFinished(jobParams, false);// tell scheduler we are done doing work

                return null;
            }
        };

        task.execute(params);// onStartJob immediately returns as soon as we call execute

        return true; // tell the scheduler that our process needs to be allowed to be alive until our background job is finishes, but we then need to tell it when the job is finished, jobFinished()
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Toast.makeText(this, "onJobStop called", Toast.LENGTH_SHORT).show();
        mNoteUploader.cancel();
        return true;
    }

}
