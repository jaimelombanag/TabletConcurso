package com.tablet.concurso.Actividades;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tablet.concurso.Adapters.CoursesGVAdapter;
import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.ConnexionTCP;
import com.tablet.concurso.Servicios.SocketServicio;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GridActivity extends AppCompatActivity {

    private Globales appState;
    private static final String TAG = "Concurso";
    private final String ACTION_STRING_ACTIVITY = "ToActivity";
    GridView mainGrid;
    ArrayList<DatosTransferDTO> dataModalArrayList;
    private ConnexionTCP sendData;
    private ProgressDialog progressDialog;

    private Timer multifuncion = new Timer();
    private int contadorPregunta;


    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("CMD");
            String datos = intent.getStringExtra("DATOS");
            try { progressDialog.dismiss(); }catch (Exception e){}

            if(cmd.equalsIgnoreCase("send_ok")){
                Log.i(TAG, "--------Debe empezar el Timer------------");
                startTimer();
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.MULTIFUNCION);
                datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                Gson gson = new Gson();

                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);


                appState.setTimerSend(1);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constantes.bloqueo, "true");
                editor.commit();


            }else if(cmd.equalsIgnoreCase("desbloqueo")){

                //stopTimer();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//                Intent sendSocket = new Intent();
//                sendSocket.putExtra("CMD", "EnvioSocket");
//                sendSocket.putExtra("DATA", sharedPreferences.getString(Constantes.idConcursantes, ""));
//                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);


                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constantes.bloqueo, "false");
                editor.commit();

                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.CARGA_DATOS);
                datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));

                Gson gson = new Gson();
                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);

            }else if(cmd.equalsIgnoreCase("ingreso")){

                //stopTimer();
                //CargaDatos();
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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
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

        mainGrid = (GridView) findViewById(R.id.idGVCourses);

        mainGrid.setNumColumns(3);
        mainGrid.setVerticalSpacing(1);
        mainGrid.setHorizontalSpacing(1);

        dataModalArrayList = new ArrayList<>();

        // here we are calling a method
        // to load data in our list view.
        loadDatainGridView();

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

    private void loadDatainGridView() {

        String foto1 = getString(R.string.foto1);
        foto1.trim();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String datos = sharedPreferences.getString(Constantes.response, "");
        Gson gson=new Gson();
        DatosTransferDTO informacion = null;
        try {
            informacion = gson.fromJson(datos, DatosTransferDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.i(TAG, "-- La lista de concursantes es: "  + informacion.getListaNombres().size());


        for(int i=0; i< informacion.getListaNombres().size(); i++){
            DatosTransferDTO datosTransferDTO1 = new DatosTransferDTO();
            datosTransferDTO1.setNombre(informacion.getListaNombres().get(i).getNombres());
            datosTransferDTO1.setAccion("Accion");
            datosTransferDTO1.setApuesta("Apuesta");
            datosTransferDTO1.setIdConcursante(informacion.getListaNombres().get(i).getIdConcursante());
            datosTransferDTO1.setFuncion("Funcion");
            datosTransferDTO1.setValor("Valor");
            //datosTransferDTO1.setFoto(foto1);
            datosTransferDTO1.setFoto(informacion.getListaNombres().get(i).getFoto());
            dataModalArrayList.add(datosTransferDTO1);
        }


//
//        String foto1 = getString(R.string.foto1);
//        foto1.trim();
//
//        DatosTransferDTO datosTransferDTO1 = new DatosTransferDTO();
//        datosTransferDTO1.setNombre("Jaime");
//        datosTransferDTO1.setAccion("Accion");
//        datosTransferDTO1.setApuesta("Apuesta");
//        datosTransferDTO1.setIdConcursante("1");
//        datosTransferDTO1.setFuncion("Funcion");
//        datosTransferDTO1.setValor("Valor");
//        datosTransferDTO1.setFoto(foto1);
//        dataModalArrayList.add(datosTransferDTO1);
//
//        DatosTransferDTO datosTransferDTO2 = new DatosTransferDTO();
//        datosTransferDTO2.setNombre("Claudia");
//        datosTransferDTO2.setAccion("Accion");
//        datosTransferDTO2.setApuesta("Apuesta");
//        datosTransferDTO2.setIdConcursante("1");
//        datosTransferDTO2.setFuncion("Funcion");
//        datosTransferDTO2.setValor("Valor");
//        datosTransferDTO2.setFoto("Foto");
//        dataModalArrayList.add(datosTransferDTO2);
//
//        DatosTransferDTO datosTransferDTO3 = new DatosTransferDTO();
//        datosTransferDTO3.setNombre("Claudia");
//        datosTransferDTO3.setAccion("Accion");
//        datosTransferDTO3.setApuesta("Apuesta");
//        datosTransferDTO3.setIdConcursante("1");
//        datosTransferDTO3.setFuncion("Funcion");
//        datosTransferDTO3.setValor("Valor");
//        datosTransferDTO3.setFoto("Foto");
//        dataModalArrayList.add(datosTransferDTO3);
//
//        DatosTransferDTO datosTransferDTO4 = new DatosTransferDTO();
//        datosTransferDTO4.setNombre("Claudia");
//        datosTransferDTO4.setAccion("Accion");
//        datosTransferDTO4.setApuesta("Apuesta");
//        datosTransferDTO4.setIdConcursante("1");
//        datosTransferDTO4.setFuncion("Funcion");
//        datosTransferDTO4.setValor("Valor");
//        datosTransferDTO4.setFoto("Foto");
//        dataModalArrayList.add(datosTransferDTO4);
//
//        DatosTransferDTO datosTransferDTO5 = new DatosTransferDTO();
//        datosTransferDTO5.setNombre("Claudia");
//        datosTransferDTO5.setAccion("Accion");
//        datosTransferDTO5.setApuesta("Apuesta");
//        datosTransferDTO5.setIdConcursante("1");
//        datosTransferDTO5.setFuncion("Funcion");
//        datosTransferDTO5.setValor("Valor");
//        datosTransferDTO5.setFoto("Foto");
//        dataModalArrayList.add(datosTransferDTO5);
//
//        DatosTransferDTO datosTransferDTO6 = new DatosTransferDTO();
//        datosTransferDTO6.setNombre("Claudia");
//        datosTransferDTO6.setAccion("Accion");
//        datosTransferDTO6.setApuesta("Apuesta");
//        datosTransferDTO6.setIdConcursante("1");
//        datosTransferDTO6.setFuncion("Funcion");
//        datosTransferDTO6.setValor("Valor");
//        datosTransferDTO6.setFoto("Foto");
//        dataModalArrayList.add(datosTransferDTO6);

        CoursesGVAdapter adapter = new CoursesGVAdapter(GridActivity.this, dataModalArrayList);

        // after passing this array list
        // to our adapter class we are setting
        // our adapter to our list view.
        mainGrid.setAdapter(adapter);

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