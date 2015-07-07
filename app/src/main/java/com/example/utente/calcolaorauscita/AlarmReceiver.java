package com.example.utente.calcolaorauscita;

/**
 * Created by utente on 06/07/2015.
 * Based on the example taken from: http://android-er.blogspot.it/2013/06/generate-notification-with-sound-when.html
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    private static final int MY_NOTIFICATION_ID=1;
    NotificationManager notificationManager;
    Notification myNotification;
    private final String myBlog = "http://android-er.blogspot.com/";

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.AlarmExitTitle), Toast.LENGTH_LONG).show();

        /* No action
        Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(myBlog));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                myIntent,
                Intent.FLAG_ACTIVITY_NEW_TASK);
*/
        myNotification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.AlarmExitTitle))
                .setContentText(context.getString(R.string.AlarmExitText))
                .setTicker(context.getString(R.string.AlarmExitTicker))
                .setWhen(System.currentTimeMillis())
// no action                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_launch_white_18dp)
                        .build();

        notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(MY_NOTIFICATION_ID, myNotification);

        Log.v("", "\n\n***\n"
                + "Alarm is raised \n"
                + "***\n");
    }
}
