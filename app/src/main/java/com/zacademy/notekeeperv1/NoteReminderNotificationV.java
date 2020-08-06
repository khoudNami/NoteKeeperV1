package com.zacademy.notekeeperv1;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NoteReminderNotificationV {

    private static final String NOTIFICATION_TAG = "NoteReminder";


    public static void notify(final Context context, final String noteTitle, final String noteText, int noteId) {
        final Resources res = context.getResources();
        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.logo);

        Intent noteActivityIntent = new Intent(context, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")

                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_note_reminder)
                .setContentTitle("Review note1")
                .setContentText(noteText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(picture)
                .setTicker("Review note")

                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(noteText)
                        .setBigContentTitle(noteTitle)
                        .setSummaryText("Review note2"))

                .setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        noteActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))


                .addAction(
                0,
                "View all notes",
                PendingIntent.getActivity(
                        context,
                        0,
                        new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT))

                .setAutoCancel(true);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}