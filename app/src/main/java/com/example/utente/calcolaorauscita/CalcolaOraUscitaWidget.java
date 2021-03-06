package com.example.utente.calcolaorauscita;

/*
    TODO: aggiornare immediatamente l'ora di uscita se cambia il profilo orario del giorno attuale
            da fare anche nella main activity

 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.view.*;

import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 */
public class CalcolaOraUscitaWidget extends AppWidgetProvider {

    // Attenzione costanti cablate
    private static int Ora=8,Minuto=0;
    private static int oraProfilo=7, minutoProfilo=42;
    private static final int oraBuonoPasto=6, minutoBuonoPasto=31;
    private static boolean allarmeImpostato=false;

    private static String dataAggiornamento;

    public static final String PREFS_NAME = "CalcolaOraUscita";
    private static final String STATO_ORA ="ORA";
    private static final String STATO_MINUTO ="MINUTO";
    private static final String STATO_ORA_PROFILO ="ORA_PROFILO";
    private static final String STATO_MINUTO_PROFILO ="MINUTO_PROFILO";
    private static final String STATO_DATA_AGGIORNAMENTO= "STATO_DATA_AGGIORNAMENTO"; // Per ora la uso solo per aggiornare le label
    private static final String STATO_ALLARME= "STATO_ALLARME";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        // TODO: sembra che non venga mai invocato... DA CANCELLARE LA CHIAMATA AL METODO E IL METODO
        aggiornaWidgetData(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        Calendar cal;
        String s;
        // Construct the RemoteViews object
        // TODO: Rendere parametrico il layout sulla base di quanto definito in AndroidManifest.xml
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout);

        // Verifico quale tipo di widget sono (normale o host)
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int category = options.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
        boolean isLockScreen = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

        // Aggiorno le label
        cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,Ora);
        cal.set(Calendar.MINUTE, Minuto);

        // valuto se è il weekend per mostrare il verde se è sabato o domenica
        boolean weekend = ((cal.get(Calendar.DAY_OF_WEEK)== Calendar.SATURDAY) || (cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY));

        // Aggiorno la data di aggiornamento
        // s=String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "/" + String.format("%02d", cal.get(Calendar.MONTH)) + "/" + String.format("%04d", cal.get(Calendar.YEAR));
        //remoteViews.setTextViewText(R.id.dataAggiornamento, s);
        remoteViews.setTextViewText(R.id.dataAggiornamento, dataAggiornamento);

        //aggiorno l'ora di uscita
        cal.add(Calendar.HOUR_OF_DAY, oraProfilo);
        cal.add(Calendar.MINUTE, minutoProfilo);


        s=String.format("%02d",Ora)+ ":"+String.format("%02d",Minuto);
        remoteViews.setTextViewText(R.id.OraIngressoTXT, s);

        s=String.format("%02d",cal.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",cal.get(Calendar.MINUTE));
        remoteViews.setTextViewText(R.id.OraUscitaTXT, s);

        // Imposto il buono pasto
        cal.set(Calendar.HOUR_OF_DAY, Ora);
        cal.set(Calendar.MINUTE, Minuto);
        cal.add(Calendar.HOUR_OF_DAY, oraBuonoPasto);
        cal.add(Calendar.MINUTE, minutoBuonoPasto);

        s=String.format("%02d",cal.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",cal.get(Calendar.MINUTE));
        remoteViews.setTextViewText(R.id.OraBuonoPastoTXT, s);


        /* TODO: nell'orientamento portrait mostrare il cerchio verde su sfondo grigio nei giorni feriali e
                il quadrato verde nei festivi
                nell'orientamento landscape lasciare la visualizzazione a barra
        */

        if (weekend) {
            //remoteViews.setInt(R.id.progressBar, "setBackgroundResource", R.drawable.roundedrect_green);
            if (context.getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) {
                //remoteViews.setInt(R.id.progressBar, "setBackgroundResource", 0 );
                remoteViews.setProgressBar(R.id.progressBar, 5, 5, false);
            } else {
                //remoteViews.setInt(R.id.progressBar, "setBackgroundResource", R.drawable.roundedrect_green);
                remoteViews.setProgressBar(R.id.progressBar, 5, 5, false);
            }
            remoteViews.setInt(R.id.calcola_ora_uscitaWDG, "setBackgroundResource", R.drawable.roundedrect_green);
        } else {
            // remoteViews.setInt(R.id.progressBar, "setBackgroundResource", R.drawable.roundedrect_red);
            remoteViews.setProgressBar(R.id.progressBar, 5, Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2,false);
            remoteViews.setInt(R.id.calcola_ora_uscitaWDG, "setBackgroundResource", R.drawable.roundedrect);
        }

        // imposto il click solo se non sono un host widget
        if (!isLockScreen){
            // When we click the widget, we want to open our main activity.
            Intent launchActivity = new Intent(context,MainActivity.class);

            // Imposto i flag per evitare di aprire nuovamente l'attività: mi serve di riaprirne solo una
            launchActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Imposto l'intent per lanciare la main activity
            PendingIntent pendingIntent = PendingIntent.getActivity(context,R.integer.intentMainActivity, launchActivity, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.calcola_ora_uscitaWDG, pendingIntent);
        }

        // Imposto l'icona dell'allarme
//        Log.e("AllarmeImpostato",(allarmeImpostato?"SI":"NO"));

        if (allarmeImpostato) {
            remoteViews.setViewVisibility(R.id.allarmeImpostato,View.VISIBLE);
            remoteViews.setViewVisibility(R.id.allarmeImpostatoShadow,View.VISIBLE);
            //remoteViews.setImageViewResource(R.id.allarmeImpostato, R.drawable.ic_alarm_white_18dp);
        }else {
            remoteViews.setViewVisibility(R.id.allarmeImpostato,View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.allarmeImpostatoShadow,View.INVISIBLE);
            //remoteViews.setImageViewResource(R.id.allarmeImpostato, R.drawable.ic_alarm_black_18dp);
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive (Context context, Intent intent){
        super.onReceive(context, intent);

//        Log.e("CalcolaOraUscitaWidget", "onReceive");

        String s=context.getString(R.string.ORA_INGRESSO_WIDGET);

        aggiornaWidgetData(context, intent);

        // Aggiorno il widget
        final ComponentName appWidgets=new ComponentName(context.getPackageName(),getClass().getName());
        final AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);
        final int ids[]=appWidgetManager.getAppWidgetIds(appWidgets);
        if (ids.length > 0) {
            onUpdate(context,appWidgetManager,ids);
        }
    }

    // Aggiorno se ho un refresh
    private void aggiornaWidgetData(Context context, Intent intent){
        String s=context.getString(R.string.ORA_INGRESSO_WIDGET);

        // Recupero i parametri dell'ora di ingresso oppure dalle shared prefs
        if (intent.hasExtra(s)) {
            // TODO : Attenzione constanti cablate!
//            Log.e("INTENT","SI");
            allarmeImpostato=intent.getBooleanExtra(STATO_ALLARME,false);
            Ora = intent.getIntExtra(s, 8);
            Minuto = intent.getIntExtra(context.getString(R.string.MINUTO_INGRESSO_WIDGET), 0);
            oraProfilo=intent.getIntExtra(STATO_ORA_PROFILO,7);
            minutoProfilo=intent.getIntExtra(STATO_MINUTO_PROFILO,42);
            dataAggiornamento=intent.getStringExtra(STATO_DATA_AGGIORNAMENTO);
        } else {
            aggiornaWidgetData(context);
        }
    }

    // imposto i parametri iniziali se non ho un intent prendendoli dalle shared prefs
    private void aggiornaWidgetData (Context context){
        Resources res= context.getResources();
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        allarmeImpostato=settings.getBoolean(STATO_ALLARME,false);
        Ora = settings.getInt(STATO_ORA, 8);
        Minuto = settings.getInt(STATO_MINUTO, 0);
        oraProfilo=settings.getInt(STATO_ORA_PROFILO, 7);
        minutoProfilo=settings.getInt(STATO_MINUTO_PROFILO, 42);
        dataAggiornamento=settings.getString(STATO_DATA_AGGIORNAMENTO,res.getString(R.string.DefaultDate));
    }
}
