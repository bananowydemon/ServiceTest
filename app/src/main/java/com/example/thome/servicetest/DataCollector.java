package com.example.thome.servicetest;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.content.IntentFilter;
import android.os.Bundle;
import java.util.Calendar;

/**
 * Klasa pobiera dane o telefonie i zapisuje w plikach oraz kontroluje rozmiar plikow
 *
 * Odpalenie serwisu, np. przez mainactivity, lub podczas dodawania widgetu
 *
 *         Intent mServiceIntent = new Intent(MainActivity.this, DataCollector.class);
 *         mServiceIntent.putExtra("collectBattery", true);
 *         MainActivity.this.startService(mServiceIntent);
 *
 *!!!!!!!!!!!!!!  uzyc job scheduler api, lub alarmmenager do tej klasy, zamiast while(true),  bo moze sie wywalac !!!!!!!!!!!
 */
public class DataCollector extends IntentService {
    private boolean collectBattery = true; // czy zbierac dane o baterii? do kazdej opcji taka zmienna
    private int maxLogFileLength = 150; // maksymalna ilosc linijek w pliku, potem kasujemy
    private String batteryLogName = "batteryLevel.txt"; // nazwa pliku z danymi o baterii
    private long numberOfonHandleIntentExecutions = 0; // ta zminna zlicza ilosc zapisow w glownej metodzie, co n uruchomien przepriowadzane jest czyszczenie plikow
    private long fileSizeControlEveryNRuns = 50; // co ile zapisow ma byc sprawdzany rozmiar pliku i skracany
    private int waitBetweenDataCollections = 600000; // w milisekundach

    public DataCollector() {
        super("DataCollector");
    }

    @Override
    protected void onHandleIntent(Intent intent) { // glowna metoda wywolywana przy uruchomieniu serwisu
        if (intent != null) {
            whatToMonitor (intent);
            Calendar calendar;
            while (true) { // petla nieskonczona

                calendar = Calendar.getInstance();
                long milisecondsAtBeginning = calendar.getTimeInMillis(); // czas w milisekundach po rozpoczeciu petli

                if (collectBattery) { // zbieranie info o baterii
                    String batteryLevel = Integer.toString(calculateBatteryLevel(getApplicationContext()));
                    LogFile.log(batteryLevel, this.batteryLogName);
                    if (numberOfonHandleIntentExecutions % fileSizeControlEveryNRuns == 0) { //co ile kontrola rozmiaru plikow
                        LogFile.fileSizeControl(this.maxLogFileLength, this.batteryLogName);
                    }
                }
                this.numberOfonHandleIntentExecutions++;

                calendar = Calendar.getInstance();
                long milisecondsAtEnd = calendar.getTimeInMillis(); // czas w milisekundach po zakonczeniu petli

                try {
                    Thread.sleep(waitBetweenDataCollections - (milisecondsAtEnd - milisecondsAtBeginning));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int calculateBatteryLevel(Context context) {

        Intent batteryIntent = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = 0;
        int scale = 1;
        try {
            level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        } catch (java.lang.NullPointerException e) {
            e.printStackTrace();
        }

        return level * 100 / scale;
    }

    private void whatToMonitor (Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("collectBattery")) {
                this.collectBattery = extras.getBoolean("collectBattery", false);
            }
        }
    }
}
