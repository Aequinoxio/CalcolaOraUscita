package com.example.utente.calcolaorauscita;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.AlertDialog;

import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.*;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.content.*;
import android.app.*;
import android.text.format.*;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    public static class TimePickerFragment1 extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }

    ////////////////////////////////////////////////////////////////////////


   // final static int RQS_1 = 1; // per lanciare l'allarme
   // final static int RQS_RINGTONEPICKER = 1 ; //per scegliere il ringtone

    // DEBUG per le istanze multiple della main activity
    private static int istanzeMainActivity=0;

    // Workaround per evitare che anche con l'annulla il metodo onTimeSet imposti il valore dell'ora
    private  boolean timePickerChoseTime=false;

    // Variabili per ripristinare i valori usando la shared prefs
    public static final String PREFS_NAME = "CalcolaOraUscita";

    Button setOraIngresso;
    Button updateOraIngresso;
    Button annullaUpdateOraIngresso;
    Button timePicker;

    protected Calendar calIn;
    protected Calendar calOut;
    protected Calendar calBP;
    // Ringtone
    private Uri uriRingTone;

    // Profilo orario giornaliero diverso per ciascun giorno della settimana
    private int[] profiloOraGiorno;
    private int[] profiloMinutoGiorno;

    // Keys per salvare e recuperare lo stato dell'attivita
    private static final String SET_BUTTON= "SET_BUTTON";
    private static final String UPDATE_BUTTON= "UPDATE_BUTTON";
    private static final String ANNULLA_BUTTON= "ANNULLA_BUTTON";
    private static final String TIMEPICKER_BUTTON= "TIMEPICKER_BUTTON";
    private static final String STATO_ORA ="ORA";
    private static final String STATO_MINUTO ="MINUTO";
    private static final String STATO_PROFILO_ORA_ARRAY="STATO_PROFILO_ORA_ARRAY";
    private static final String STATO_PROFILO_MINUTO_ARRAY="STATO_PROFILO_MINUTO_ARRAY";
    private static final String STATO_ORA_PROFILO ="ORA_PROFILO";
    private static final String STATO_MINUTO_PROFILO ="MINUTO_PROFILO";
    private static final String STATO_DATA_AGGIORNAMENTO= "STATO_DATA_AGGIORNAMENTO"; // Per ora la uso solo per aggiornare le label

    // TODO: Usi futuri
    //protected static final String STATO_ALLARME= "STATO_ALLARME";

    // N.B. startActivityForRequest accetta solo valori a 16 bit (!!!)
    private static final int RingTonePickerRequestCode = R.integer.RingTonePickerRequestCode & 0xffff;

    // TODO: Usi futuri
    // Tengo traccia se ho impostato l'allarme
    //static boolean allarmeImpostato;

    // Variabili per impostare le notifiche
    Notification myNotification=null;
    NotificationCompat.Builder myNotificationBuilder=null;
    NotificationManager notificationManager=null;

    // Variabili per memorizzare data e ora. Usate per memorizzare lo stato e per
    // ripristinare i valori corretti dopo lo stop dell'Activity
    private int Ora,Minuto;
    private String dataAggiornamento;

    // Costanti per calcolare il buono pasto (6:31)
    private static final int oraBuonoPasto = 6;
    private static final int minutoBuonoPasto = 31;

    // Appoggio per il timepicker del profilo orario
    private int profiloOra, profiloMinuto;
    private int profiloLBLClicked;  // id della label cliccata TODO: Penso che si possa fare di meglio
    private int giornoSettimanaProfiloClick; // giorno della settimana ricavato dalle label su cui si e' cliccato TODO: Si puo' fare di meglio, non riesco a referenziare il giorno definito all'esterno della classe inlinea - probabilmente va ridefinito il timepicker come dialog a se

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // VAriabile per verificare se l'allarme è impostato
        // TODO: va tolto se si decide di non mostrare nulla
//        TODO: Usi futuri
//        allarmeImpostato=false;

        // Tengo conto delle istanze allocate per ogni futura evenienza
        // TODO: da togliere se non serve più
        istanzeMainActivity++;

        // In debug mode accendo quello che mi serve per il debug
        if (BuildConfig.DEBUG){
            Button b = (Button) findViewById(R.id.btnRingTone);
            b.setVisibility(View.GONE);

            // DEBUG, accendo il visualizzatore di istanze
            TextView t = (TextView) findViewById(R.id.lblMainActivityIstanze);
            t.setText(String.format("%d",istanzeMainActivity));

        } else {
            Button b = (Button) findViewById(R.id.btnRingTone);
            b.setVisibility(View.GONE);
            TextView t = (TextView) findViewById(R.id.lblMainActivityIstanze);
            t.setVisibility(View.GONE);
        }

        // alloco 5 interi per ore e minuti che costituiscono il profilo orario giornaliero
        // + 2 per sabato e domenica che per ora non modifico
        profiloOraGiorno = new int[7];
        profiloMinutoGiorno = new int [7];

        setOraIngresso = (Button) findViewById(R.id.buttonSetOraIngresso);
        updateOraIngresso = (Button) findViewById(R.id.buttonUpdateOraIngresso);
        annullaUpdateOraIngresso=(Button) findViewById(R.id.buttonAnnulla);
        timePicker=(Button) findViewById(R.id.buttonTimePicker);

        setOraIngresso.setEnabled(true);
        updateOraIngresso.setEnabled(false);
        annullaUpdateOraIngresso.setEnabled(false);
        timePicker.setEnabled(true);

        // Imposto le variabili core
        calIn = Calendar.getInstance();
        calOut = Calendar.getInstance();
        calBP=Calendar.getInstance();

        //
        uriRingTone=Uri.parse("");

        // Ripristino o imposto l'ora di ingresso e calcolo l'ora di uscita
        if (savedInstanceState != null) {
            Boolean  buttonEnabled;

            // Recupero stato
            Ora= savedInstanceState.getInt(STATO_ORA);
            Minuto =savedInstanceState.getInt(STATO_MINUTO);
            profiloOraGiorno=savedInstanceState.getIntArray(STATO_PROFILO_ORA_ARRAY);
            profiloMinutoGiorno=savedInstanceState.getIntArray(STATO_PROFILO_MINUTO_ARRAY);

            try {
                dataAggiornamento=savedInstanceState.getString(STATO_DATA_AGGIORNAMENTO);
                SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
                calIn.setTime(sdf.parse(dataAggiornamento));
            }catch(Exception e){
                calIn = Calendar.getInstance();
            }

            calIn.set(Calendar.HOUR_OF_DAY, Ora);
            calIn.set(Calendar.MINUTE,Minuto);

            aggiornaValori();

            buttonEnabled=savedInstanceState.getBoolean(SET_BUTTON);
            setOraIngresso.setEnabled(buttonEnabled);

            buttonEnabled=savedInstanceState.getBoolean(UPDATE_BUTTON);
            updateOraIngresso.setEnabled(buttonEnabled);

            buttonEnabled=savedInstanceState.getBoolean(ANNULLA_BUTTON);
            annullaUpdateOraIngresso.setEnabled(buttonEnabled);

            buttonEnabled=savedInstanceState.getBoolean(TIMEPICKER_BUTTON);
            timePicker.setEnabled(buttonEnabled);

        } else {
            // Restore preferences
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            // Recupero l'ora salvata a suo tempo
            Ora = settings.getInt(STATO_ORA, 8);
            Minuto = settings.getInt(STATO_MINUTO,0);

//            TODO: Usi futuri
//            allarmeImpostato=settings.getBoolean(STATO_ALLARME, false);

            // Recupero il profilo orario
            // ora
            String savedString;
            savedString = settings.getString(STATO_PROFILO_ORA_ARRAY, getString(R.string.DEFAULT_PROFILO_ORA));
            StringTokenizer st = new StringTokenizer(savedString,getString(R.string.SEPARATORE_CAMPI));
            for (int i = 0; i < 7; i++) {
                profiloOraGiorno[i] = Integer.parseInt(st.nextToken());
            }

            // minuto
            savedString = settings.getString(STATO_PROFILO_MINUTO_ARRAY, getString(R.string.DEFAULT_PROFILO_MINUTO));
            st = new StringTokenizer(savedString,getString(R.string.SEPARATORE_CAMPI));
            for (int i = 0; i < 7; i++) {
                profiloMinutoGiorno[i] = Integer.parseInt(st.nextToken());
            }

            try {
                dataAggiornamento=settings.getString(STATO_DATA_AGGIORNAMENTO, getString(R.string.DefaultDate));
                SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());
                calIn.setTime(sdf.parse(dataAggiornamento));
            }catch(Exception e){
                calIn = Calendar.getInstance();
            }

            calIn.set(Calendar.HOUR_OF_DAY, Ora);
            calIn.set(Calendar.MINUTE, Minuto);
            aggiornaValori();
        }

        setOraIngresso.setOnClickListener(
            new Button.OnClickListener() {
                public void onClick(View v) {
                    calIn = Calendar.getInstance();
                    calOut = Calendar.getInstance();
                    Ora=calIn.get(Calendar.HOUR_OF_DAY);
                    Minuto=calIn.get(Calendar.MINUTE);

                    aggiornaValori();

                    setAlarm(calOut);

                }
            }
        );

        updateOraIngresso.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setOraIngresso.setEnabled(true);
                        updateOraIngresso.setEnabled(false);
                        annullaUpdateOraIngresso.setEnabled(true);
                        timePicker.setEnabled(true);
                    }
                }
        );

        annullaUpdateOraIngresso.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setOraIngresso.setEnabled(false);
                        updateOraIngresso.setEnabled(true);
                        annullaUpdateOraIngresso.setEnabled(false);
                        timePicker.setEnabled(false);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RingTonePickerRequestCode)
            if (resultCode == RESULT_OK)
                uriRingTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            else
                uriRingTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


    }
    private void setAlarm(Calendar targetCal){

        Log.v("", "\n\n***\n"
                + this.getString(R.string.setAlarmToastInfo) + " " + targetCal.getTime() + "\n"
                + "***\n");

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(getString(R.string.alarmIntentRingToneUri), uriRingTone);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getBaseContext(),
                R.integer.AlarmRequestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);

        // Visualizzo un messaggio come feedback
        Toast myToast = Toast.makeText(MainActivity.this,
                this.getString(R.string.setAlarmToastInfo)+"\n"
                        + android.text.format.DateFormat.format(
                            "HH:mm:ss - dd/MM/yy",
                            targetCal.getTimeInMillis()
                          ),
                Toast.LENGTH_LONG);
        myToast.show();

        // TODO: Vedere come ripassare l'informazione dall'AlarmReceiver all'Activity
//        TODO: Usi futuri
//        allarmeImpostato=true;

        setNotification();
    }
    private void setNotification(){
        Context context= getBaseContext();
        // TODO: Impostare il pattern nelle risorse
        // int[] intPatternTemp =this.getResources().getIntArray(R.array.NotificationVibrationPattern);
        // List<long> a = Arrays.asList(intPatternTemp )

        long[] pattern = {250,1000,250,1000,250,1000,250,1000,250, 1000,250,1000,250,1000,250};

//        // Lancio la main activity se clicco sulla notifica - FATTO MEGLIO SECONDO QUANTO DICE LA GUIDA ANDROID
//        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
//        // Lancio solo una attività e cancello tutte le eventuali altre
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        // al click riattivo la main activity
//        PendingIntent notifyPIntent = PendingIntent.getActivity(context, R.integer.intentMainActivity,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//

        // Preservo la navigazione tra le activity (vedi http://developer.android.com/guide/topics/ui/notifiers/notifications.html#HandlingNotifications)
        Intent mainActivityIntent = new Intent(context,MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(mainActivityIntent);
        PendingIntent notifyPIntent = stackBuilder.getPendingIntent(R.integer.intentMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: Usi futuri
//        // check per vedere se l'allarme è impostato
//        // TODO: Non funziona, una volta impostato l'allarme torna sempre il pending intent
//        PendingIntent p = PendingIntent.getBroadcast(getBaseContext(),R.integer.AlarmRequestCode,
//                new Intent(getApplicationContext(), AlarmReceiver.class),
//                PendingIntent.FLAG_NO_CREATE) ;
//
//        allarmeImpostato= (p != null);
//
//        if (p!= null){
//            Log.d("myTag", "Alarm is already active");
//        }

        if (myNotificationBuilder==null)
            myNotificationBuilder=new NotificationCompat.Builder(context);

        myNotification = myNotificationBuilder
            .setContentTitle(context.getString(R.string.NotificationAlarmExitTitle)
                            //TODO: Usi futuri
                            // + ((allarmeImpostato) ? " (" + context.getString(R.string.NotificationStartTextAlarmSet) + ")" : "")
            )
            .setContentText(context.getString(R.string.NotificationStartText) + ": " +
                            String.format("%02d", calOut.get(Calendar.HOUR_OF_DAY)) + ": " + String.format("%02d", calOut.get(Calendar.MINUTE)) + "\n" +
                            context.getString(R.string.NotificationStartTicker2) + " " +
                            String.format("%02d", calBP.get(Calendar.HOUR_OF_DAY)) + ": " + String.format("%02d", calBP.get(Calendar.MINUTE))
            )
            .setTicker(context.getString(R.string.NotificationStartTicker1) + " " +
                            String.format("%02d", calOut.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calOut.get(Calendar.MINUTE)) + "\n" +
                            context.getString(R.string.NotificationStartTicker2) + " " +
                            String.format("%02d", calBP.get(Calendar.HOUR_OF_DAY)) + ": " + String.format("%02d", calBP.get(Calendar.MINUTE))
            )
            .setWhen(System.currentTimeMillis())
          //  .setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.FLAG_SHOW_LIGHTS)
            .setDefaults(Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
            .setLights(Color.BLUE|Color.YELLOW, 1500, 500)
            .setSmallIcon(R.drawable.ic_launch_white_18dp)
            .setPriority(Notification.PRIORITY_HIGH)
            .setVibrate(pattern)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setOngoing(true)           // Mantengo la notifica
            // .setProgress(100,0,false)
            .setUsesChronometer(false)
            .setContentIntent(notifyPIntent)    // Main activity come activity richiamata al click
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .build();

//      myNotification.ledARGB=0xFF0000FF;
//      myNotification.ledOnMS=500;
//      myNotification.ledOffMS=500;

        notificationManager =
                (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(R.integer.MY_NOTIFICATION_ID, myNotification);
    }
    protected void aggiornaValori(){
        String s;
        TextView testo;
        Resources res = getResources();
        boolean weekend= false;

        // Aggiorno l'ora per il buono pasto
        calBP.setTime(calIn.getTime());
        calBP.add(Calendar.HOUR_OF_DAY, oraBuonoPasto);
        calBP.add(Calendar.MINUTE, minutoBuonoPasto);
        testo = (TextView) findViewById(R.id.textBuonoPasto);
        s=String.format("%02d",calBP.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",calBP.get(Calendar.MINUTE));
        testo.setText(s);

        // Aggiorno l'ora di uscita con il profilo orario del giorno attuale
        calOut.setTime(calIn.getTime());

        int textID=0; // ID temporaneo per recuperare la textview corrispondente al giorno della settimana

        // Verifico il giorno della settimana per segnarlo (indipendentemente dalla data di aggiornamento)
        //int giornoSettimana =calOut.get(Calendar.DAY_OF_WEEK); // TODO: Workaround per ora. In futuro usare una lista ed foreach
        Calendar calAppoggio=Calendar.getInstance();
        int giornoSettimana =calAppoggio.get(Calendar.DAY_OF_WEEK); // TODO: Workaround per ora. In futuro usare una lista ed foreach

        // Imposto la label per il giorno corrente
        testo = (TextView) findViewById(R.id.lblDataOdierna);
        testo.setText(
                String.format("%02d", calAppoggio.get(Calendar.DAY_OF_MONTH)) + "/" + String.format("%02d", calAppoggio.get(Calendar.MONTH)+1) + "/" + String.format("%04d", calAppoggio.get(Calendar.YEAR))
        );

        // Libero memoria
        calAppoggio=null;

        weekend = ((Calendar.getInstance().get(Calendar.DAY_OF_WEEK)== Calendar.SATURDAY) || (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY));
        switch (giornoSettimana){ // TODO: workaround per ora. Ridefinire in modo da usare tutti i giorni della settimana
            case Calendar.SATURDAY :    textID=R.id.sabLBL; giornoSettimana=5; break;
            case Calendar.SUNDAY:       textID=R.id.domLBL; giornoSettimana=6; break ;
            case Calendar.MONDAY:       textID=R.id.lunLBL; giornoSettimana=0; break;
            case Calendar.TUESDAY:      textID=R.id.marLBL; giornoSettimana=1; break;
            case Calendar.WEDNESDAY:    textID=R.id.merLBL; giornoSettimana=2;break;
            case Calendar.THURSDAY:     textID=R.id.gioLBL; giornoSettimana=3;break;
            case Calendar.FRIDAY:       textID=R.id.venLBL; giornoSettimana=4;break;
        }

        // imposto il colore del giorno attuale in base al fatto che sia o meno il weekend
        // imposto la progress bar della settimana e la azzero se sono nel weekend
        // TODO: attenzione costante 5 giorni lavorativi cablata
        ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setMax(5);
        //testo = (TextView) findViewById(R.id.workDayLBL);
        if (weekend) {
            //testo.setBackgroundResource(R.drawable.roundedrect_green);
            //pb.setBackgroundResource(R.drawable.roundedrect_green);
            pb.setProgress(5);
        } else {
            //testo.setBackgroundResource(R.drawable.roundedrect_red);
            //pb.setBackgroundResource(R.drawable.roundedrect_red);
            pb.setSecondaryProgress(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1);
            pb.setProgress(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-2);
        }

        profiloOra=profiloOraGiorno[giornoSettimana];
        profiloMinuto=profiloMinutoGiorno[giornoSettimana];

        // In debug mode imposto il timer di uscita ad un tempo brevissimo
        if (BuildConfig.DEBUG) {
            calOut= calIn.getInstance();
            calOut.add(Calendar.SECOND, 30);
        } else {
            calOut.add(Calendar.HOUR, profiloOra);
            calOut.add(Calendar.MINUTE, profiloMinuto);
        }
        testo = (TextView) findViewById(R.id.textOraIngresso);
        s=String.format("%02d",calIn.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",calIn.get(Calendar.MINUTE));
        testo.setText(s);

        testo = (TextView) findViewById(R.id.textOraUscita);
        s=String.format("%02d",calOut.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d", calOut.get(Calendar.MINUTE));
        testo.setText(s);

        // Evidenzio il giorno corrente della settimana
        // Cancello il vecchio sfondo
        testo = (TextView) findViewById(R.id.lunLBL);
        testo.setBackground(null);
        testo = (TextView) findViewById(R.id.marLBL);
        testo.setBackground(null);
        testo = (TextView) findViewById(R.id.merLBL);
        testo.setBackground(null);
        testo = (TextView) findViewById(R.id.gioLBL);
        testo.setBackground(null);
        testo = (TextView) findViewById(R.id.venLBL);
        testo.setBackground(null);
        testo = (TextView) findViewById(R.id.sabLBL);
        testo.setBackground(null);
        testo = (TextView) findViewById(R.id.domLBL);
        testo.setBackground(null);

        // imposto il nuovo sfondo solo se non è un weekend, per i weekend uso il verde
        testo = (TextView) findViewById(textID);
        if (!weekend) {
            testo.setBackground(res.getDrawable(android.R.color.holo_red_light));
        } else {
            testo.setBackground(res.getDrawable(android.R.color.holo_green_light));
        }

        // Imposto le label dei profili orari
        testo = (TextView) findViewById(R.id.profiloLunVal);
        testo.setText(String.format("%02d", profiloOraGiorno[0]) + ":" + String.format("%02d", profiloMinutoGiorno[0]));

        testo = (TextView) findViewById(R.id.profiloMarVal);
        testo.setText(String.format("%02d",profiloOraGiorno[1])+ ":"+String.format("%02d",profiloMinutoGiorno[1]));

        testo = (TextView) findViewById(R.id.profiloMerVal);
        testo.setText(String.format("%02d", profiloOraGiorno[2]) + ":" + String.format("%02d", profiloMinutoGiorno[2]));

        testo = (TextView) findViewById(R.id.profiloGioVal);
        testo.setText(String.format("%02d",profiloOraGiorno[3])+ ":"+String.format("%02d",profiloMinutoGiorno[3]));

        testo = (TextView) findViewById(R.id.profiloVenVal);
        testo.setText(String.format("%02d",profiloOraGiorno[4])+ ":"+String.format("%02d",profiloMinutoGiorno[4]));

        // Aggiorno la data di aggiornamento
        testo = (TextView) findViewById(R.id.dataAggiornamento);
        // N.B. occorre aggiungere 1 ai mesi perchè il loro intervallo è 0-11 e non 1-12
        dataAggiornamento=String.format("%02d", calIn.get(Calendar.DAY_OF_MONTH)) + "/" + String.format("%02d", calIn.get(Calendar.MONTH)+1) + "/" + String.format("%04d", calIn.get(Calendar.YEAR));
        testo.setText(dataAggiornamento);

        // Imposto i pulsanti
        setOraIngresso.setEnabled(false);
        updateOraIngresso.setEnabled(true);
        annullaUpdateOraIngresso.setEnabled(false);
        timePicker.setEnabled(false);

        // Invio un broadcast ai widget con i valori aggiornati di ora e minuto ingresso
        Intent intent = new Intent(this, CalcolaOraUscitaWidget.class);
        intent.putExtra(getString(R.string.ORA_INGRESSO_WIDGET), Ora);
        intent.putExtra(getString(R.string.MINUTO_INGRESSO_WIDGET), Minuto);

        intent.putExtra(STATO_ORA_PROFILO, profiloOra);
        intent.putExtra(STATO_MINUTO_PROFILO, profiloMinuto);
        intent.putExtra(STATO_DATA_AGGIORNAMENTO,dataAggiornamento);

        sendBroadcast(intent);

        // TODO: Sto provando ad aggiornare anche gli altri widget anche se mi sembra strano farlo così...
        intent = new Intent(this, CalcolaOraUscitaWidget_2x2.class);
        intent.putExtra(getString(R.string.ORA_INGRESSO_WIDGET), Ora);
        intent.putExtra(getString(R.string.MINUTO_INGRESSO_WIDGET),Minuto);

        intent.putExtra(STATO_ORA_PROFILO, profiloOra);
        intent.putExtra(STATO_MINUTO_PROFILO, profiloMinuto);
        intent.putExtra(STATO_DATA_AGGIORNAMENTO,dataAggiornamento);

        sendBroadcast(intent);

        intent = new Intent(this, CalcolaOraUscitaWidget_1x1.class);
        intent.putExtra(getString(R.string.ORA_INGRESSO_WIDGET), Ora);
        intent.putExtra(getString(R.string.MINUTO_INGRESSO_WIDGET),Minuto);

        intent.putExtra(STATO_ORA_PROFILO, profiloOra);
        intent.putExtra(STATO_MINUTO_PROFILO, profiloMinuto);
        intent.putExtra(STATO_DATA_AGGIORNAMENTO,dataAggiornamento);

        sendBroadcast(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        /*
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.TODO))
                    .setMessage(getString(R.string.TODO_MSG)+"\n"+getString(R.string.TODO_MSG2))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
            return true;
        }
*/

        if (id == R.id.action_about) {
            String s = getString(R.string.app_name) +"\nVersione " + BuildConfig.VERSION_NAME ;
            s+="\nby "+ getString(R.string.Autore);
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.action_about)
                    .setMessage(s)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();

        // Salvo tutto in SharedPreferences
        // TODO: Trovare un metodo migliore (forse conviene usare una classe ad hoc (preferenceActivity) see http://stackoverflow.com/questions/3876680/is-it-possible-to-add-an-array-or-object-to-sharedpreferences-on-android

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(STATO_ORA, Ora);
        editor.putInt(STATO_MINUTO, Minuto);

        // Salvo il profilo orario
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < profiloOraGiorno.length; i++) {
            str.append(profiloOraGiorno[i]).append(getString(R.string.SEPARATORE_CAMPI));
        }
        editor.putString(STATO_PROFILO_ORA_ARRAY, str.toString());

        str.delete(0, str.length());
        for (int i = 0; i < profiloMinutoGiorno.length; i++) {
            str.append(profiloMinutoGiorno[i]).append(getString(R.string.SEPARATORE_CAMPI));
        }
        editor.putString(STATO_PROFILO_MINUTO_ARRAY, str.toString());

        editor.putInt(STATO_ORA_PROFILO, profiloOra);
        editor.putInt(STATO_MINUTO_PROFILO, profiloMinuto);

        editor.putString(STATO_DATA_AGGIORNAMENTO, dataAggiornamento);

        // Commit the edits!
        editor.commit();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Recupero l'ora e minuto salvati e lo stato dell'array dei profili da savedInstanceState
        Ora=savedInstanceState.getInt(STATO_ORA);
        Minuto=savedInstanceState.getInt(STATO_MINUTO);
        profiloOraGiorno=savedInstanceState.getIntArray(STATO_PROFILO_ORA_ARRAY);
        profiloMinutoGiorno=savedInstanceState.getIntArray(STATO_PROFILO_MINUTO_ARRAY);

        dataAggiornamento=savedInstanceState.getString(STATO_DATA_AGGIORNAMENTO);

/*
    N.B. la classe SimpleDateFormat imposta i mesi nell'intervallo 0-11 anzichè 1-12
         fare attenzione al mese che va indietro di uno
    */
        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yyyy",Locale.getDefault());

        try {
            calIn.setTime(sdf.parse(dataAggiornamento));
        }catch(Exception e) {
            calIn = Calendar.getInstance();
        }

        calIn.set(Calendar.HOUR_OF_DAY, Ora);
        calIn.set(Calendar.MINUTE, Minuto);

        aggiornaValori();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SET_BUTTON, setOraIngresso.isEnabled());
        outState.putBoolean(UPDATE_BUTTON, updateOraIngresso.isEnabled());
        outState.putBoolean(ANNULLA_BUTTON, annullaUpdateOraIngresso.isEnabled());

        outState.putInt(STATO_ORA, Ora);
        outState.putInt(STATO_MINUTO, Minuto);

        outState.putIntArray(STATO_PROFILO_ORA_ARRAY, profiloOraGiorno);
        outState.putIntArray(STATO_PROFILO_MINUTO_ARRAY, profiloMinutoGiorno);
        outState.putString(STATO_DATA_AGGIORNAMENTO, dataAggiornamento);

        // TODO: Usi futuri
        // TODO: vedere come impostare e salvare lo stato allarme. possibile problema: quando l'allarme è impostato, l'app è cancellata, l'allarme suona, l'app potrebbe riprendere uno stato
        // inconsistente con quello dell'allarme vero (allarme non suonato per l'app, suonato l'allarme)
    }

    public void showTimePickerDialog(View v) {
        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                if (timePickerChoseTime) {
                    calIn.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calIn.set(Calendar.MINUTE, selectedMinute);
                    calOut.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calOut.set(Calendar.MINUTE, selectedMinute);

                    Ora = selectedHour;
                    Minuto = selectedMinute;
                    aggiornaValori();

                    setAlarm(calOut);
                }
            }
        }, Ora, Minuto, true);//Yes 24 hour time

        mTimePicker.setButton(DialogInterface.BUTTON_NEGATIVE,getString(R.string.timePickerCancel),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    timePickerChoseTime=false;
                   // Log.v("","Annullato il timepicker");
                }
            }
        });

        mTimePicker.setButton(DialogInterface.BUTTON_POSITIVE,getString(R.string.timePickerAccept),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    timePickerChoseTime=true;
                    // Log.v("","Impostato il timepicker");
                }
            }
        });

        mTimePicker.setOnDismissListener(new Dialog.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                timePickerChoseTime=false;
                // Non faccio nulla
            }
        });

        mTimePicker.setTitle(getString(R.string.TIME_PICKER_TITLE));

        mTimePicker.show();
    }

    public void showRingTonePicker(View v){
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

        // Il metodo accetta solo valori a 16 bit (!!!)
        startActivityForResult(intent, RingTonePickerRequestCode);
    }
    public void showNotification(View v) {
        setNotification();
    }
    public void showTimePickerDialogProfilo(View v) {
        TimePickerDialog mTimePicker;

        profiloLBLClicked=v.getId();

        // Modifico solo i profili dei 5 giorni lavorativi
        switch (profiloLBLClicked){ // TODO: workaround per ora
            case R.id.profiloLunVal: giornoSettimanaProfiloClick=0 ; break;
            case R.id.profiloMarVal: giornoSettimanaProfiloClick=1 ;break;
            case R.id.profiloMerVal: giornoSettimanaProfiloClick=2 ;break;
            case R.id.profiloGioVal: giornoSettimanaProfiloClick=3 ;break;
            case R.id.profiloVenVal: giornoSettimanaProfiloClick=4 ;break;
        }

        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                profiloOra=selectedHour;
                profiloMinuto=selectedMinute;
                aggiornaProfili();
            }
        }, profiloOraGiorno[giornoSettimanaProfiloClick], profiloMinutoGiorno[giornoSettimanaProfiloClick], true);//Yes 24 hour time

        mTimePicker.setTitle(getString(R.string.TIME_PICKER_TITLE));
        mTimePicker.show();
    }

    private void aggiornaProfili(){
        // Aggiorno i valori e la label
        profiloOraGiorno[giornoSettimanaProfiloClick]=profiloOra;
        profiloMinutoGiorno[giornoSettimanaProfiloClick]=profiloMinuto;

        TextView testo = (TextView) findViewById(profiloLBLClicked);
        testo.setText(String.format("%02d", profiloOra) + ":" + String.format("%02d", profiloMinuto));
    }
}
