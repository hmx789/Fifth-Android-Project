package com.example.hassan.federalmoneyserver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.hassan.KeyCommon.treasuryInterface;




public class MainActivity extends AppCompatActivity {
    private TextView sStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starting the service
        Intent servIntent = new Intent(treasuryInterface.class.getName());
        ResolveInfo info = getPackageManager().resolveService(servIntent, 0);
        servIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

        startService(servIntent);


        sStatus = findViewById(R.id.servStatus);

    }

    @Override
    protected void onResume() {



        if (TreasuryService.serviceDestroyed)
            sStatus.setText("Service is destroyed");

        if (!TreasuryService.serviceStarted && !TreasuryService.clientBounded) {    // It's not started but but it is servers job to start it so we startService
            Intent servIntent = new Intent(treasuryInterface.class.getName());
            ResolveInfo info = getPackageManager().resolveService(servIntent, 0);
            servIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
            startService(servIntent);
            TreasuryService.serviceStarted = true;
            sStatus.setText("Started but not bound to a client");

        }

        else if (TreasuryService.serviceStarted && TreasuryService.clientBounded)
            sStatus.setText("Started and bounded to one or more clients");

        else if (TreasuryService.serviceStarted && !TreasuryService.clientBounded)
            sStatus.setText("Started but not bound to a client");

        else if (!TreasuryService.serviceStarted && TreasuryService.clientBounded)
            sStatus.setText("Bound but not started");

        super.onResume();


    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onDestroy() {
        //Stopping the service
        Intent servIntent = new Intent(treasuryInterface.class.getName());
        ResolveInfo info = getPackageManager().resolveService(servIntent, 0);
        servIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
        stopService(servIntent);
        super.onDestroy();
    }
}
