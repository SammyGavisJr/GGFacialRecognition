package com.example.sameer.samplebluetoothserver;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Sameer on 5/1/2015.
 */
public class DisplayName extends Activity
{
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        Bundle bundle = getIntent().getExtras();
        String name = bundle.getString("name");
        TextView textView = (TextView) findViewById(R.id.name);
        textView.setText(name);
    }
}
