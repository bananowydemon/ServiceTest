package com.example.thome.servicetest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
 * Creates a new Intent to start the RSSPullService
 * IntentService. Passes a URI in the
 * Intent's "data" field.
 */
        Intent mServiceIntent = new Intent(MainActivity.this, DataCollectorService.class);
        mServiceIntent.putExtra("collectBattery", true);
        MainActivity.this.startService(mServiceIntent);
    }
}
