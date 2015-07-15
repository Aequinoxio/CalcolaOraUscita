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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    NotificationManager notificationManager;
    Notification myNotification;

    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO: Impostare il pattern nelle risorse
        // long[] pattern = this.getResources().getIntArray(R.array.NotificationVibrationPattern);
        long[] pattern = {250,1000,250,1000,250,1000,250,1000,250, 1000,250,1000,250,1000,250};

        /*
        TODO: rimando indietro che l'allarme è stato eseguito

        Intent intent = new Intent(this, MainActivity.class);

        intent.putExtra(getString(R.string.alarmIntentRingToneUri), uriRingTone);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(),
                R.integer.AlarmRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
*/
        // Messagio a schermo
        Toast.makeText(context, context.getString(R.string.NotificationAlarmExitToast), Toast.LENGTH_LONG).show();

        // TODO: Impostare il ringtone ed il volume
        String s=intent.getStringExtra(context.getString(R.string.alarmIntentRingToneUri));
        Uri uriRingTone;
        try{
            uriRingTone=Uri.parse(s);
        } catch (Exception e){
         // Impostare un ringtone di default
         // uriRingTone="";
        }

        PendingIntent notifyPIntent =
                PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(), 0);

        myNotification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.NotificationAlarmExitTitle))
                .setContentText(context.getString(R.string.NotificationAlarmExitText))
                .setTicker(context.getString(R.string.NotificationAlarmExitTicker))
                .setWhen(System.currentTimeMillis())
               // .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.FLAG_SHOW_LIGHTS)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                .setAutoCancel(true)
                .setLights(Color.BLUE,1500,500)
                .setSmallIcon(R.drawable.ic_launch_white_18dp)
                .setVibrate(pattern)
                .setOnlyAlertOnce(false)
                .setOngoing(true)       // Mantengo la notifica
                .setContentIntent(notifyPIntent)    // Main activity come activity richiamata al click (???)
                .build();

        // TODO: non imposta il valore nella classe. Capire il perchè
        // non viene considerato un codice eseguibile !!!

        MainActivity.resetAllarme();

/*
        myNotification.ledARGB=0xff0000ff;;
        myNotification.ledOnMS=500;
        myNotification.ledOffMS=500;
*/

        notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.integer.MY_NOTIFICATION_ID,myNotification);

        Log.v("", "\n\n***\n"
                + "Alarm is raised \n"
                + "***\n");
    }
}
