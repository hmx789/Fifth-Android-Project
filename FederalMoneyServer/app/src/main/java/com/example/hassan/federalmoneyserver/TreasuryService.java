package com.example.hassan.federalmoneyserver;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.hassan.KeyCommon.treasuryInterface;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class TreasuryService extends Service {
    private static final String TAG = "TreasuryService";
    public static String status = "Service not started";
    private static final String URL = "http://api.treasury.io/cc7znvq/47d80ae900e04f2/sql/?q=";
    public static boolean serviceStarted = false;
    public static boolean clientBounded  = false;
    public static boolean serviceDestroyed = false;

    private final treasuryInterface.Stub mBinder = new treasuryInterface.Stub() {

        // Test method to see if proxy is working
        public String[] getKey() {
            String[] giveitback = new String[]{"hi", "hello", "why"};
            return giveitback;
        }

        public int[] monthlyAvgCash(int aYear) {
            @SuppressLint("DefaultLocale")  /// Query to get avg per month for a year
            String query = String.format("select  avg(\"close_today\") as avgCash from t1 where \"year\" = %d and \"is_total\" = 1 group by (\"month\") order by \"month\";",aYear);
            try {
                query = URLEncoder.encode(query,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int[] data = null;
            HttpURLConnection httpURLCon = null;
            try {
                // Connecting to the treasury database
                httpURLCon = (HttpURLConnection) new URL(URL+query).openConnection();
                InputStream in = new BufferedInputStream(httpURLCon.getInputStream());
                data = readStream(in);  //getting int array of the data
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (null != httpURLCon) {
                    httpURLCon.disconnect();
                }
            }

            return data;
        }

        public int[] dailyCash(int aYear, int aMonth, int aDay, int aNumber) {
            String startDate = String.format(Locale.US,"%d-%02d-%02d",aYear,aMonth,aDay); //Formatting start date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Calendar c = Calendar.getInstance();
            int addDuration = aNumber + (aNumber/ 5) * 2; //Accounting for weekends
            try {
                c.setTime(sdf.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            c.add(Calendar.DATE,addDuration);       // Using calendar to add number of days to date input
            String endDate = sdf.format(c.getTime());   //Getting string formatted correctly for query
            String query = String.format(Locale.US,"select \"open_today\" as avgCash from t1 where \"is_total\" = 1 and \"date\" >= '%s' and \"date\" <= '%s' order by date LIMIT %d",startDate,endDate,aNumber+1);
            try {
                query = URLEncoder.encode(query,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int[] data = null;      // Array to hold the cash on hand for each day
            HttpURLConnection httpURLCon = null;
            try {
                httpURLCon = (HttpURLConnection) new URL(URL+query).openConnection();
                InputStream in = new BufferedInputStream(httpURLCon.getInputStream());
                data = readStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (null != httpURLCon) {
                    httpURLCon.disconnect();
                }
            }

            return data;
        }
    };

    private int[] readStream(InputStream in) {
        BufferedReader reader = null;
        JSONArray jArray= null;
        StringBuffer data = new StringBuffer("");
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
        } catch (IOException e) {
//            Log.e(TAG, "Can't read from reader!");
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            jArray = new JSONArray(data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int[] avgs = new int[jArray.length()];
        for (int i = 0; i < jArray.length();i++) {
            try {
                avgs[i] = jArray.getJSONObject(i).getInt("avgCash");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return avgs;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i(TAG,"Starting the service");
        serviceStarted = true;  // Starting the service
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
//        Log.i(TAG,"Binding to service!");
        clientBounded = true;       // Binding to a client
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
//        Log.i(TAG,"Binding to service (rebind)!");
        clientBounded = true;       // Binding to a client
        super.onRebind(intent);
    }


    @Override
    public boolean onUnbind(Intent intent) {
//        Log.i(TAG,"Unbinded!");
        clientBounded = false;          // Unbinding from a client
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
//        Log.i(TAG,"Destroying the service");
        clientBounded = false;
        serviceStarted = false;     // Destroying the service
        serviceDestroyed = true;
        super.onDestroy();
    }


}
