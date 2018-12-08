package com.example.hassan.federalmoneyclient;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {
    ListView avgView;
    ArrayAdapter<Integer>adapter;
    int[] data;
    ArrayList<Integer> intArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        intArrayList = new ArrayList<>();
        avgView = findViewById(R.id.avgView);

        Intent intent = getIntent();

        data = intent.getIntArrayExtra("dateCash");

        if (data == null) {
            data = intent.getIntArrayExtra("yearCash");
        }

        if (data == null) { // If no data is passed nothing to display
            return;
        }

        for (int i = 0; i < data.length;i++) {
            intArrayList.add(data[i]);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, intArrayList);

        avgView.setAdapter(adapter);

    }
}
