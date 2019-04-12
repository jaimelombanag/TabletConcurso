package com.tablet.concurso.Actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.MeterView;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.SocketServicio;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Concurso";
    MeterView meterView1;
    private ImageView imagen_concursantes;
    private TextView txt_nombres;
    private Button btn_valor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        meterView1 = (MeterView) findViewById(R.id.meterView1);
        imagen_concursantes = (ImageView) findViewById(R.id.imagen_concursantes);
        txt_nombres = (TextView) findViewById(R.id.txt_nombres);
        btn_valor = (Button) findViewById(R.id.btn_valor);


        //extraemos el drawable en un bitmap
        Drawable originalDrawable = getResources().getDrawable(R.drawable.foto_pareja);
        Bitmap originalBitmap = ((BitmapDrawable) originalDrawable).getBitmap();

        //creamos el drawable redondeado
        RoundedBitmapDrawable roundedDrawable =
                RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);

        //asignamos el CornerRadius
        roundedDrawable.setCornerRadius(originalBitmap.getHeight());
        imagen_concursantes.setImageDrawable(roundedDrawable);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        txt_nombres.setText(sharedPreferences.getString(Constantes.nombresConcursantes, ""));
        btn_valor.setText(sharedPreferences.getString(Constantes.valorConcursantes, ""));

    }


    public void EnviarValor(View v){
        Log.i(TAG, "===================    "  +  meterView1.getValue());

        int valorInicial = Integer.parseInt(btn_valor.getText().toString());


        if(meterView1.getValue() > valorInicial){

            Toast.makeText(getApplicationContext(), "El valor no puede ser mayor al que tiene", Toast.LENGTH_LONG).show();
        }else{

            Intent sendSocket = new Intent();
            sendSocket.putExtra("CMD", "EnvioSocket2");
            sendSocket.putExtra("DATA",  meterView1.getValue()+"");
            sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);
        }


    }
}
