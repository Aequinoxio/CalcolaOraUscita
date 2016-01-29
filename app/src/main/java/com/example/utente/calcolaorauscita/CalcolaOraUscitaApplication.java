package com.example.utente.calcolaorauscita;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by utente on 20/09/2015.
 */

@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "https://pippokennedy.cloudant.com/acra-calcolaorauscita/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "bodurewarightleakeduends",
        formUriBasicAuthPassword = "e7afc862ed2b89e72c5a33d63839431fc87b8958",

        customReportContent = {
                // Campi obblicatori per acralyzer
                ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION,ReportField.PACKAGE_NAME,ReportField.BUILD, ReportField.STACK_TRACE,

                // Campo per sapere il cellulare su cui è installato
                ReportField.INSTALLATION_ID,

                // Campi utili per avere altre info
                ReportField.PHONE_MODEL,ReportField.CUSTOM_DATA, ReportField.LOGCAT, ReportField.SETTINGS_GLOBAL, ReportField.DEVICE_FEATURES,
                ReportField.SETTINGS_SECURE, ReportField.SETTINGS_SYSTEM, ReportField.SHARED_PREFERENCES, ReportField.THREAD_DETAILS
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_report
)

/*
@ReportsCrashes(
        httpMethod = HttpSender.Method.PUT,
        reportType = HttpSender.Type.JSON,
        formUri = "http://pippokennedy.iriscouch.com/acra-calcolaorauscita/_design/acra-storage/_update/report",
        formUriBasicAuthLogin = "pippo",
        formUriBasicAuthPassword = "kennedy",

        customReportContent = {
                // Campi obblicatori per acralyzer
                ReportField.REPORT_ID, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION,ReportField.PACKAGE_NAME,ReportField.BUILD, ReportField.STACK_TRACE,

                // Campo per sapere il cellulare su cui è installato
                ReportField.INSTALLATION_ID,

                // Campi utili per avere altre info
                ReportField.PHONE_MODEL,ReportField.CUSTOM_DATA, ReportField.LOGCAT, ReportField.SETTINGS_GLOBAL, ReportField.DEVICE_FEATURES,
                ReportField.SETTINGS_SECURE, ReportField.SETTINGS_SYSTEM, ReportField.SHARED_PREFERENCES, ReportField.THREAD_DETAILS
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_report
)
*/



public class CalcolaOraUscitaApplication extends Application {
    // File che memorizza una UUID e verificare se l'applicazione è già stata eseguita
    private static final String INSTALLATION = "INSTALLATO";

    /**
     * Verifica se l'applicazione è già stata lanciata e comunica il first run tramite ACRA
     */
    private void checkFirstRunAndSendData(){
        Context context = getApplicationContext();
        String versione = BuildConfig.VERSION_NAME;

        // Test per vedere il first run
        File installation = new File(context.getFilesDir(), INSTALLATION);

        // Se il file esiste recupero la versione salvata come prima linea
        if (installation.exists()){
            try {
                BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(installation)));
                versione = fin.readLine();
                fin.close();
            }catch (Exception fne){
                fne.printStackTrace();
            }
        }

        // Se il file non esiste o la versione è diversa da quella attuale mando il first run
        // Todo: verificare perchè non va bene la verifica della versione
        if (!installation.exists()||(versione.compareTo(BuildConfig.VERSION_NAME)!=0)) {
            // Provo a comunicare i dati al server. Se non ci riesco resta in first run
            ACRA.getErrorReporter().handleSilentException(new Throwable("Primo avvio applicazione"));

            try {
                FileOutputStream out = new FileOutputStream(installation, false);
                String id = UUID.randomUUID().toString()+"\n";

                versione=BuildConfig.VERSION_NAME +"\n";

                out.write(versione.getBytes());

                out.write(id.getBytes());
                out.close();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
                // Se arrivo qui allora ho comunicato i dati e scrivo il file
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);

        // Verifico se è la prima esecuzione e provo a mandare i dati
        checkFirstRunAndSendData();
    }
}
