package com.tablet.concurso;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    MeterView meterView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meterView1 = (MeterView) findViewById(R.id.meterView1);

    }


    public void Enviar(View v){
        Log.i("JAIME", "===================    "  +  meterView1.getValue());
    }
}
