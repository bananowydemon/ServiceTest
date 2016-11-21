package com.example.thome.servicetest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Handler;
import android.os.Message;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.widget.Toast;

import java.util.Calendar;

/**
 * do: AndroidManifest.xml
 *
 * ...
 * <service
 * android:name=".DataCollectorService"
 * ...
 *
 * dodac linijke:
 *
 * ...
 * <service
 * android:name=".DataCollectorService"
 * android:process=":ServiceProcess"
 *...
 *
 * !!!!!!!!!!!!!! Lepsza opcja:
 * uzyc job scheduler api, lub alarmmenager niz while(true), bo moze sie wywalac !!!!!!!!!!!!!!!!!
 *
 * Odpalenie serwisu, np. przez mainactivity, lub podczas dodawania widgetu
 *
 *         Intent mServiceIntent = new Intent(MainActivity.this, DataCollectorService.class);
 *         mServiceIntent.putExtra("collectBattery", true);
 *         MainActivity.this.startService(mServiceIntent);
 */

public class DataCollectorService extends Service {



    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private boolean collectBattery = true; // czy zbierac dane o baterii? do kazdej opcji taka zmienna
        private int maxLogFileLength = 150; // maksymalna ilosc linijek w pliku, potem kasujemy
        private String batteryLogName = "batteryLevel.txt"; // nazwa pliku z danymi o baterii
        private long numberOfonHandleIntentExecutions = 0; // ta zminna zlicza ilosc zapisow w glownej metodzie, co n uruchomien przepriowadzane jest czyszczenie plikow
        private long fileSizeControlEveryNRuns = 50; // co ile zapisow ma byc sprawdzany rozmiar pliku i skracany
        private int waitBetweenDataCollections = 600000; // w milisekundach
        private Intent intent = new Intent();

        public void getIntent(Intent assignedIntent) {
            this.intent = assignedIntent;
        }

        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            try {                executeMonitoring(this.intent);
            } catch (Exception e) {
                // Restore interrupt status.
                Thread.currentThread().interrupt();
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }

        protected void executeMonitoring(Intent intent) { // glowna metoda wywolywana przy uruchomieniu serwisu
            if (intent != null) {
                whatToMonitor(intent);

            }

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

        private int calculateBatteryLevel(Context context) {

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

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // provide intent to the Handler
        mServiceHandler.getIntent(intent);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +100, restartServicePI);

    }


}
