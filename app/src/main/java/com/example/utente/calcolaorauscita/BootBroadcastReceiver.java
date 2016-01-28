package com.example.utente.calcolaorauscita;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by utente on 03/08/2015.
 *
 * Classe utile per resettare i parametri persistenti in caso di reboot del cellulare
 */
public class BootBroadcastReceiver extends BroadcastReceiver{
    private static final String STATO_ALLARME= "STATO_ALLARME";
    public static final String PREFS_NAME = "CalcolaOraUscita";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Resetto tutte le variabili persistenti sullo stato dell'allarme e mi disattivo

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor ed = sharedPreferences.edit();
            ed.putBoolean(STATO_ALLARME, false);
            ed.commit();

//            Log.e("BootReceiver","Lanciato");

            // Imposto il flag per il bootReceiver per disattivatmi
            ComponentName receiver = new ComponentName(context,BootBroadcastReceiver.class);
            PackageManager pm = context.getPackageManager();

            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

}
