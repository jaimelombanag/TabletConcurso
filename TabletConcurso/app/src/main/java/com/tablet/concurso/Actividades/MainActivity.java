package com.tablet.concurso.Actividades;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;
import com.tablet.concurso.MeterView;
import com.tablet.concurso.ModelInventario;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.ConnexionTCP;
import com.tablet.concurso.Servicios.SocketServicio;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Concurso";
    private final String ACTION_STRING_ACTIVITY = "ToActivity";
    private Globales appState;
    MeterView meterView1;
    MeterView meterView2;
    private ImageView imagen_concursantes;
    private TextView txt_nombres;
    private Button btn_valor;
    private Button btn_send;
    private ProgressDialog progressDialog;
    private Timer multifuncion = new Timer();
    private boolean bloqueo = false;
    private int contadorPregunta;
    private ConnexionTCP sendData;
    private Typeface script;



    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("CMD");
            String datos = intent.getStringExtra("DATOS");
            try { progressDialog.dismiss(); }catch (Exception e){}

            if(cmd.equalsIgnoreCase("send_ok")){


                Log.i(TAG, "--------Debe empezar el Timer------------");
                //startTimer();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.MULTIFUNCION);
                datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                Gson gson = new Gson();

                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);


                appState.setTimerSend(1);

                btn_send.setBackgroundResource(R.drawable.boton_on);
                bloqueo = true;
            }else if(cmd.equalsIgnoreCase("desbloqueo")){

                //stopTimer();
                btn_send.setBackgroundResource(R.drawable.boton);
                bloqueo = false;

                meterView1.setValue(0);
                meterView2.setValue(0);


                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                Intent sendSocket = new Intent();
//                sendSocket.putExtra("CMD", "EnvioSocket");
//                sendSocket.putExtra("DATA", sharedPreferences.getString(Constantes.idConcursantes, ""));
//                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);


                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.CARGA_DATOS);
                datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));

                Gson gson = new Gson();
                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);





            }else if(cmd.equalsIgnoreCase("ingreso")){

                //stopTimer();
                CargaDatos();
            }else if(cmd.equalsIgnoreCase("relogin")){

                //stopTimer();

                Intent activity = new Intent();
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.putExtra("relogin", "relogin");
                activity.setClass(getApplicationContext(), SplashActivity.class);
                getApplicationContext().startActivity(activity);
                finish();

            }else if(cmd.equalsIgnoreCase("close")){
                finish();
            }else if(cmd.equalsIgnoreCase("reenvio")){
                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            Log.i(TAG, "debe enviar");
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                            datosTransferDTO.setFuncion(Funciones.MULTIFUNCION);
                            datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                            Gson gson2 = new Gson();

                            String json = gson2.toJson(datosTransferDTO);
                            sendData = new ConnexionTCP(getApplicationContext());
                            sendData.sendData(json);
                        }
                    }, 5000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }


        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context context = getApplicationContext();
        appState = ((Globales) context);

        if (activityReceiver != null) {
            try {
                registerReceiver(activityReceiver, new IntentFilter(ACTION_STRING_ACTIVITY));
            } catch (Exception e) {
            }
        }

        /*******************************Para que La pantalla no se apague*********************/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        meterView1 = (MeterView) findViewById(R.id.meterView1);
        meterView2 = (MeterView) findViewById(R.id.meterView2);
        imagen_concursantes = (ImageView) findViewById(R.id.imagen_concursantes);
        txt_nombres = (TextView) findViewById(R.id.txt_nombres);
        btn_valor = (Button) findViewById(R.id.btn_valor);
        btn_send = (Button) findViewById(R.id.btn_send);


        String fuente = "fuentes/condensed_bold.ttf";
        this.script = Typeface.createFromAsset(getAssets(),fuente);
        txt_nombres.setTypeface(script);
        btn_valor.setTypeface(script);
        CargaDatos();

    }

    public void CargaDatos(){
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //extraemos el drawable en un bitmap
            Drawable originalDrawable = getResources().getDrawable(R.drawable.foto_pareja);
            //Bitmap originalBitmap = ((BitmapDrawable) originalDrawable).getBitmap();

            Bitmap originalBitmap = StringToBitMap(sharedPreferences.getString(Constantes.fotoConcursantes, ""));


            //creamos el drawable redondeado
            RoundedBitmapDrawable roundedDrawable =
                    RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);

            //asignamos el CornerRadius
            roundedDrawable.setCornerRadius(originalBitmap.getHeight());
            imagen_concursantes.setImageDrawable(roundedDrawable);


            txt_nombres.setText(sharedPreferences.getString(Constantes.nombresConcursantes, ""));
            btn_valor.setText(mascaraNumero(Long.parseLong(sharedPreferences.getString(Constantes.valorConcursantes, ""))));
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void EnviarValor(View v){

        int valorFinal;
        if(meterView1.getValue() == 0){
            if(meterView2.getValue() == 0){
                valorFinal = meterView2.getValue();
            }else{
                valorFinal = meterView2.getValue() ;
            }


        }else{
            if(meterView2.getValue() == 0){
                valorFinal = (meterView1.getValue() * 1000) + meterView2.getValue();
            }else{
                valorFinal =  (meterView1.getValue() * 1000) + meterView2.getValue();
            }



        }



        Log.i(TAG, "-------El Valor1 es:  "  +  meterView1.getValue());
        Log.i(TAG, "-------El Valor2 es:  "  +  meterView2.getValue());
        Log.i(TAG, "-------El Valor es:  "  +  valorFinal);
        if(bloqueo){
            Toast.makeText(getApplicationContext(), "Aplicación bloqueada", Toast.LENGTH_LONG).show();
        }else{
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int valorInicial = Integer.parseInt(sharedPreferences.getString(Constantes.valorConcursantes, ""));





            if(valorFinal > valorInicial){

                Toast.makeText(getApplicationContext(), "El valor no puede ser mayor al que tiene", Toast.LENGTH_LONG).show();
            }else{

                MuestraProcessDialog("Enviando...");
//                Intent sendSocket = new Intent();
//                sendSocket.putExtra("CMD", "EnvioSocket2");
//                sendSocket.putExtra("DATA",  valorFinal+"");
//                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);




                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.SEND_VALOR);
                datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                datosTransferDTO.setValor(valorFinal+"");

                Gson gson = new Gson();
                String json = gson.toJson(datosTransferDTO);

                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);


            }
        }

    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte=Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
        }
    }

    public String mascaraNumero(Long numero) {
        String ret = "";
        try {
            DecimalFormat formatter = new DecimalFormat();
            formatter.applyPattern("$ ##,###");
            ret = formatter.format(numero);
            ret = ret.replace(".", ",");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void MuestraProcessDialog(String mensaje){
        progressDialog = new ProgressDialog(MainActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();
    }


    private void startTimer(){
        try {
            multifuncion.scheduleAtFixedRate(new SendMultifuncion(), 0, 1000);
        }catch (Exception e){

            stopTimer();
            ReStartTimer();


            e.printStackTrace();
        }

    }


    public void ReStartTimer(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startTimer();


            }
        }, 2000);
    }

    private void stopTimer(){
        multifuncion.cancel();
    }

    private class SendMultifuncion extends TimerTask {
        public void run() {
            contadorPregunta++;
            if(contadorPregunta > 5){
                contadorPregunta = 0;
//                Intent sendSocket = new Intent();
//                sendSocket.putExtra("CMD", "EnvioSocket3");
//                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.MULTIFUNCION);
                datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                Gson gson = new Gson();

                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);

            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumeeeen");
        Intent i = new Intent(this, SocketServicio.class);
        stopService(i);
        if (activityReceiver != null) {
            registerReceiver(activityReceiver, new IntentFilter(ACTION_STRING_ACTIVITY));
        }
    }

    @Override
    public void finish() {
        super.finish();
        unregisterReceiver(activityReceiver);
        Intent data = new Intent();
        setResult(Activity.RESULT_CANCELED, data);
        Intent i = new Intent(this, SocketServicio.class);
        stopService(i);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                try {
                    Intent i = new Intent(this, SocketServicio.class);
                    stopService(i);
                    finish();
                }catch (Exception e ){
                    e.printStackTrace();
                }
                return true;
            case KeyEvent.KEYCODE_HOME:
                Log.i(TAG, "Se Oprimio el Boton de Back");

        }
        return super.onKeyDown(keyCode, event);
    }
}
