package com.example.utente.calcolaorauscita;

/**
 * Created by utente on 06/07/2015.
 * Based on the example taken from: http://android-er.blogspot.it/2013/06/generate-notification-with-sound-when.html
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    NotificationManager notificationManager;
    Notification myNotification;

    static final long[] pattern = {250,1000,250,1000,250,1000,250,1000,250, 1000,250,1000,250,1000,250};

    public static final String PREFS_NAME = "CalcolaOraUscita";
    public static final String STATO_ALLARME= "STATO_ALLARME";

    @Override
    public void onReceive(Context context, Intent intent) {


//        // TODO: Impostare il pattern nelle risorse
//        // long[] pattern = this.getResources().getIntArray(R.array.NotificationVibrationPattern);
//        int[] patternInt= context.getResources().getIntArray(R.array.NotificationVibrationPattern);
//        long [] pattern= new long[patternInt.length];
//        for (int i=0;i< patternInt.length;i++){
//            pattern[i]=(long) patternInt[i];
//        }
//        //long[] pattern = {250,1000,250,1000,250,1000,250,1000,250, 1000,250,1000,250,1000,250};

        // Messagio a schermo
        Toast.makeText(context, context.getString(R.string.NotificationAlarmExitToast), Toast.LENGTH_LONG).show();

        // TODO: Impostare il ringtone ed il volume
        // TODO: verificare bele il tipo URI come recuperarlo
        String s=intent.getStringExtra(context.getString(R.string.alarmIntentRingToneUri));
        Uri uriRingTone;
        try{
            uriRingTone=Uri.parse(s);
        } catch (Exception e){
            // Impostare un ringtone di default
            // uriRingTone="";
        }

        // Dico all'app che l'allarme è suonato
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor ed = sp.edit();

       // ed.remove(STATO_ALLARME);
        ed.putBoolean(STATO_ALLARME,false);
        ed.commit();

        Intent mainActivityIntent = new Intent(context,MainActivity.class);
        // L'allarme è suonato
        mainActivityIntent.putExtra(STATO_ALLARME,false);
/*
        // Preservo la navigazione tra le activity (vedi http://developer.android.com/guide/topics/ui/notifiers/notifications.html#HandlingNotifications)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainActivityIntent);
        PendingIntent notifyPIntent = stackBuilder.getPendingIntent(R.integer.intentMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
*/

        // Se clicco sulla notifica la faccio solo sparire
        PendingIntent notifyPIntent =
                PendingIntent.getActivity(context.getApplicationContext(), R.integer.intentMainActivity, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        myNotification = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.NotificationAlarmExitTitle))
                .setContentText(context.getString(R.string.NotificationAlarmExitText))
                .setTicker(context.getString(R.string.NotificationAlarmExitTicker))
                .setWhen(System.currentTimeMillis())
               // .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.FLAG_SHOW_LIGHTS)
                .setDefaults(Notification.FLAG_SHOW_LIGHTS)
                .setAutoCancel(true)
                .setLights(Color.BLUE|Color.YELLOW, 1500, 500)
                .setSmallIcon(R.drawable.ic_launch_white_18dp)
                .setVibrate(pattern)
                .setOnlyAlertOnce(false)
                .setOngoing(true)       // Mantengo la notifica
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentIntent(notifyPIntent)    // Main activity come activity richiamata al click (???)
                .build();

        notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.integer.MY_NOTIFICATION_ID, myNotification);

        Log.v("", "\n\n***\n"
                + "Alarm is raised \n"
                + "***\n");

        // Invio un broadcast ai widget con i valori aggiornati di ora e minuto ingresso
        Intent intentWidget = new Intent(context, CalcolaOraUscitaWidget.class);
        intentWidget.putExtra(STATO_ALLARME,false);

        context.sendBroadcast(intentWidget);
    }

}