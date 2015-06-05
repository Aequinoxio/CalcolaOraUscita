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
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.view.*;

import java.util.Calendar;

/**
 * Implementation of App Widget functionality.
 */
public class CalcolaOraUscitaWidget extends AppWidgetProvider {

    // TODO: Attenzione costanti cablate
    private static int Ora=8,Minuto=0;
    private static int oraProfilo=7, minutoProfilo=42;
    private static int oraBuonoPasto=6, minutoBuonoPasto=31;

    public static final String PREFS_NAME = "CalcolaOraUscita";
    private static final String STATO_ORA ="ORA";
    private static final String STATO_MINUTO ="MINUTO";
    private static final String STATO_ORA_PROFILO ="ORA_PROFILO";
    private static final String STATO_MINUTO_PROFILO ="MINUTO_PROFILO";


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
           //     R.layout.calcola_ora_uscita_widget_icon_2x2);

        // Verifico quale tipo di widget sono (normale o host)
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int category = options.getInt(AppWidgetManager.OPTION_APPWIDGET_HOST_CATEGORY, -1);
        boolean isLockScreen = category == AppWidgetProviderInfo.WIDGET_CATEGORY_KEYGUARD;

        // Aggiorno le label
        // TODO: usare il profilo giornaliero prendendolo dalla shared prefs (da fare nella main activity o nei parametri dell'intent)
        cal=Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,Ora);
        cal.set(Calendar.MINUTE, Minuto);
        cal.add(Calendar.HOUR_OF_DAY, oraProfilo);
        cal.add(Calendar.MINUTE, minutoProfilo);

        s=String.format("%02d",Ora)+ ":"+String.format("%02d",Minuto);
        remoteViews.setTextViewText(R.id.OraIngressoTXT, s);

        s=String.format("%02d",cal.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",cal.get(Calendar.MINUTE));
        remoteViews.setTextViewText(R.id.OraUscitaTXT, s);

        // Imposto il buono pasto
        cal.set(Calendar.HOUR_OF_DAY,Ora);
        cal.set(Calendar.MINUTE, Minuto);
        cal.add(Calendar.HOUR_OF_DAY, oraBuonoPasto);
        cal.add(Calendar.MINUTE, minutoBuonoPasto);

        s=String.format("%02d",cal.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",cal.get(Calendar.MINUTE));
        remoteViews.setTextViewText(R.id.OraBuonoPastoTXT, s);

        // imposto il click solo se non sono un host widget
        if (!isLockScreen){
            // When we click the widget, we want to open our main activity.
            Intent launchActivity = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0 , launchActivity, 0 );

            remoteViews.setOnClickPendingIntent(R.id.calcola_ora_uscitaWDG, pendingIntent);
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive (Context context, Intent intent){
        super.onReceive(context, intent);

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
        // TODO: verificare se sia il metodo migliore oppure se occorre lasciare una istanza di aggiornamneto in background o altro
        if (intent.hasExtra(s)) {
            // TODO : Attenzione constanti cablate!
            Ora = intent.getIntExtra(s, 8);
            Minuto = intent.getIntExtra(context.getString(R.string.MINUTO_INGRESSO_WIDGET), 0);
            oraProfilo=intent.getIntExtra(STATO_ORA_PROFILO,7);
            minutoProfilo=intent.getIntExtra(STATO_MINUTO_PROFILO,42);

        } else {
            aggiornaWidgetData(context);
        }
    }

    // imposto i parametri iniziali se non ho un intent prendendoli dalle shared prefs
    private void aggiornaWidgetData (Context context){
        // Restore preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        Ora = settings.getInt(STATO_ORA, 8);
        Minuto = settings.getInt(STATO_MINUTO, 0);
        oraProfilo=settings.getInt(STATO_ORA_PROFILO, 7);
        minutoProfilo=settings.getInt(STATO_MINUTO_PROFILO, 42);
    }
}


