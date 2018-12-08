package com.example.hassan.federalmoneyclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hassan.KeyCommon.treasuryInterface;



public class MainActivity extends AppCompatActivity {
    private DatePicker dateInput;
    private EditText workingDaysInput;
    private Button unbindButton;
    int month;
    int day;
    int year;
    int duration;
    private static final String TAG = "MainActivity";
    private boolean mIsBound = false;

    treasuryInterface treasureService;


    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            treasureService = treasuryInterface.Stub.asInterface(iservice);

            mIsBound = true;

        }

        public void onServiceDisconnected(ComponentName className) {
            treasureService = null;

            mIsBound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button sendDate = findViewById(R.id.reqDate);
        Button sendYr = findViewById(R.id.reqYear);
        unbindButton = findViewById(R.id.unbind);
        workingDaysInput = findViewById(R.id.workingDayInput);

        dateInput = findViewById(R.id.dateInput);


        sendDate.setOnClickListener(new View.OnClickListener () {
            public void onClick(View v) {
                try {
                    duration = Integer.parseInt(workingDaysInput.getText().toString());
                }

                catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Working day is not a number!", Toast.LENGTH_SHORT).show();
                    return;
                }

                year = dateInput.getYear();

                if (year < 2006 || year > 2016 || duration < 5 || duration > 25) {
                    Toast.makeText(MainActivity.this, "Invalid Input!", Toast.LENGTH_SHORT).show();
                    return;
                }
                month = dateInput.getMonth();
                day = dateInput.getDayOfMonth();

                Thread dateWorker = new Thread(new sendDate(year, month + 1, day, duration));
                dateWorker.start();

            }
        });

        sendYr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                year = dateInput.getYear();
                if (year < 2006 || year > 2016) {
                    Toast.makeText(MainActivity.this,"Invalid year!",Toast.LENGTH_SHORT).show();
                    return;
                }
                Thread yearWorker = new Thread(new sendYear(year));
                yearWorker.start();
            }
        });

        unbindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBound) {
                    unbindService(mConnection);
                    mIsBound = false;
//                    Log.i(TAG,"Unbinding!");
                }
            }
        });
    }


    // Bind to treasuryInterface Service
    @Override
    protected void onResume() {
        bind2Service();
        super.onResume();
    }


    public void bind2Service() {        //Binding to the service!
        if (!mIsBound) {
            boolean b = false;

            Intent servIntent = new Intent(treasuryInterface.class.getName());

            ResolveInfo info = getPackageManager().resolveService(servIntent, 0);


            servIntent.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));
            startService(servIntent);
            b = bindService(servIntent, mConnection, Context.BIND_AUTO_CREATE);

            if (b) {
//                Log.i(TAG, "bindService() succeeded!");
                mIsBound = true;
            } else {
//                Log.i(TAG, "bindService() failed!");
            }

        }
    }


    @Override
    protected void onPause() {              // Unbind from treasuryInterface Service
        unbindService(mConnection);
        mIsBound = false;
        super.onPause();
    }


    protected void onStop() {
        super.onStop();
    }

    public class sendDate implements Runnable {
        int year;
        int month;
        int day;
        int duration;
        int[] dailyMoney;

        sendDate(int yr, int m, int d, int n) {
            this.year = yr;
            this.month = m;
            this.day = d;
            this.duration = n;
        }


        @Override
        public void run() {
            try {
                if (!mIsBound) {
                    bind2Service();
                    while(!mIsBound){
                        // if it isnt bounded yet wait till bind2service is done
                    }
                }
                dailyMoney = treasureService.dailyCash(year, month, day, duration);
                Intent dateIntent = new Intent(MainActivity.this, ListActivity.class);
                dateIntent.putExtra("dateCash",dailyMoney);
                startActivity(dateIntent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }


    }


    public class sendYear implements Runnable {
        int year;
        int[] monthlyAvgs;

        sendYear(int yr) {
            this.year = yr;
        }

        @Override
        public void run() {
            try {
                if (!mIsBound) {
                    bind2Service();
                    while(!mIsBound){
                     // if it isnt bounded yet wait till bind2service is done
                    }
                }
                monthlyAvgs = treasureService.monthlyAvgCash(year);
                Intent i = new Intent(MainActivity.this,ListActivity.class);
                i.putExtra("yearCash",monthlyAvgs);
                startActivity(i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}

