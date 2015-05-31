package com.example.utente.calcolaorauscita;

import java.util.*;

import android.app.AlertDialog;

import android.appwidget.AppWidgetManager;
import android.support.v7.app.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.Button;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.content.*;
import android.app.*;
import android.text.format.*;

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

    // Variabili per ripristinare i valori usando la shared prefs
    public static final String PREFS_NAME = "CalcolaOraUscita";

    Button setOraIngresso;
    Button updateOraIngresso;
    Button annullaUpdateOraIngresso;
    Button timePicker;

    protected Calendar calIn;
    protected Calendar calOut;

    // Keys per salvare e recuperare lo stato dell'attivita
    private static final String SET_BUTTON= "SET_BUTTON";
    private static final String UPDATE_BUTTON= "UPDATE_BUTTON";
    private static final String ANNULLA_BUTTON= "ANNULLA_BUTTON";
    private static final String TIMEPICKER_BUTTON= "TIMEPICKER_BUTTON";
    private static final String STATO_ORA ="ORA";
    private static final String STATO_MINUTO ="MINUTO";

    // Vriabili per memorizzare data e ora. Usate per memorizzare lo stato e per
    // ripristinare i valori corretti dopo lo stop dell'Activity
    private int Ora,Minuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setOraIngresso = (Button) findViewById(R.id.buttonSetOraIngresso);
        updateOraIngresso = (Button) findViewById(R.id.buttonUpdateOraIngresso);
        annullaUpdateOraIngresso=(Button) findViewById(R.id.buttonAnnulla);
        timePicker=(Button) findViewById(R.id.buttonTimePicker);

        setOraIngresso.setEnabled(true);
        updateOraIngresso.setEnabled(false);
        annullaUpdateOraIngresso.setEnabled(false);
        timePicker.setEnabled(true);

        calIn = Calendar.getInstance();
        calOut = Calendar.getInstance();

        // Ripristimo o imposto l'ora di ingresso e calcolo l'ora di uscita
        if (savedInstanceState != null) {
            Boolean  buttonEnabled;

            Ora= savedInstanceState.getInt(STATO_ORA);
            Minuto =savedInstanceState.getInt(STATO_MINUTO);
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
            Ora = settings.getInt(STATO_ORA, 8);
            Minuto = settings.getInt(STATO_MINUTO,0);
            calIn.set(Calendar.HOUR_OF_DAY, Ora);
            calIn.set(Calendar.MINUTE,Minuto);
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

    protected void aggiornaValori(){
        String s;

        calOut.setTime(calIn.getTime());
        calOut.add(Calendar.HOUR, 7);
        calOut.add(Calendar.MINUTE, 42);

        TextView testo;

        testo = (TextView) findViewById(R.id.textOraIngresso);

        s=String.format("%02d",calIn.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d",calIn.get(Calendar.MINUTE));
        /*
        if (debug)
            Log.i("OraIngresso ", s);
        */

        testo.setText(s);

        testo = (TextView) findViewById(R.id.textOraUscita);
        s=String.format("%02d",calOut.get(Calendar.HOUR_OF_DAY))+ ":"+String.format("%02d", calOut.get(Calendar.MINUTE));

        /*
        if (debug)
            Log.i("OraUscita ", s);
        */

        testo.setText(s);

        setOraIngresso.setEnabled(false);
        updateOraIngresso.setEnabled(true);
        annullaUpdateOraIngresso.setEnabled(false);
        timePicker.setEnabled(false);

        // Invio un broadcast ai widget con i valori aggiornati di ora e minuto ingresso
        Intent intent = new Intent(this, CalcolaOraUscitaWidget.class);
        intent.putExtra(getString(R.string.ORA_INGRESSO_WIDGET), Ora);
        intent.putExtra(getString(R.string.MINUTO_INGRESSO_WIDGET),Minuto);
        sendBroadcast(intent);

        /*
        // Aggiorno direttamente i widget
        int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), CalcolaOraUscitaWidget.class));

        for (int id : widgetIDs)
            AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.layout.calcola_ora_uscita_widget);
        */
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.TODO))
                    .setMessage(getString(R.string.TODO_MSG))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
            return true;
        }

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

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(STATO_ORA, Ora);
        editor.putInt(STATO_MINUTO, Minuto);

        // Commit the edits!
        editor.commit();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Ora=savedInstanceState.getInt(STATO_ORA);
        Minuto=savedInstanceState.getInt(STATO_MINUTO);
        calIn.set(Calendar.HOUR_OF_DAY, Ora);
        calIn.set(Calendar.MINUTE,Minuto);

        aggiornaValori();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SET_BUTTON, setOraIngresso.isEnabled());
        outState.putBoolean(UPDATE_BUTTON, updateOraIngresso.isEnabled());
        outState.putBoolean(ANNULLA_BUTTON, annullaUpdateOraIngresso.isEnabled());

        outState.putInt(STATO_ORA, Ora);
        outState.putInt(STATO_MINUTO,Minuto);
    }

    public void showTimePickerDialog(View v) {
        TimePickerDialog mTimePicker;

        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                calIn.set(Calendar.HOUR_OF_DAY,selectedHour);
                calIn.set(Calendar.MINUTE,selectedMinute);
                calOut.set(Calendar.HOUR_OF_DAY, selectedHour);
                calOut.set(Calendar.MINUTE, selectedMinute);

                Ora= selectedHour;
                Minuto = selectedMinute;
                aggiornaValori();
            }
        }, Ora, Minuto, true);//Yes 24 hour time

        mTimePicker.setTitle(getString(R.string.TIME_PICKER_TITLE));
        mTimePicker.show();
    }
}
