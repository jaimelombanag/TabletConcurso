package com.tablet.concurso.Actividades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.tablet.concurso.Adapters.ListInventarioAdapter;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;
import com.tablet.concurso.ModelInventario;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.SocketServicio;
import com.tablet.concurso.Servicios.Temporizador;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "Concurso";
    private final String ACTION_STRING_SERVICE = "ToService";
    private final String ACTION_STRING_ACTIVITY = "ToActivity";
    private Globales appState;
    private ListView list_concursantes;
    private ListInventarioAdapter salidasAdapter;
    private ProgressDialog progressDialog;


//    ModelInventario[] androidFlavors = {
//            new ModelInventario("CT 02", "- Sin Confirmar", R.mipmap.ic_launcher, R.mipmap.ic_launcher),
//            new ModelInventario("CT 01", "- Confirmado", R.mipmap.ic_launcher, R.mipmap.ic_launcher),
//
//    };

    ModelInventario[] androidFlavors;

    //ArrayList<ModelInventario> dataModels;

    private final BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("CMD");
            String datos = intent.getStringExtra("DATOS");
            try { progressDialog.dismiss(); }catch (Exception e){}
            if(cmd.equalsIgnoreCase("listaCOncursantes")){

                androidFlavors = new ModelInventario[appState.getDatosConcursantes().size()];
                for(int i=0; i < appState.getDatosConcursantes().size(); i++){

                    Log.i(TAG, "==========Nombre: "  +  appState.getDatosConcursantes().get(i).getNombres());
                    Log.i(TAG, "==========Id: "  +  appState.getDatosConcursantes().get(i).getIdConcursante());

                    androidFlavors[i] = new ModelInventario(appState.getDatosConcursantes().get(i).getNombres(), appState.getDatosConcursantes().get(i).getIdConcursante(), R.mipmap.ic_launcher, R.mipmap.ic_launcher);

                }

                MuestraConcursantes2();

            }else if(cmd.equalsIgnoreCase("ingreso")){

                Intent activity = new Intent();
                activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.setClass(getApplicationContext(), MainActivity.class);
                getApplicationContext().startActivity(activity);
                finish();

            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
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

        try {
            String valor = getIntent().getExtras().getString("relogin");
            if(valor.equalsIgnoreCase("relogin")){
                Intent sendSocket = new Intent();
                sendSocket.putExtra("CMD", "EnvioSocket0");
                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);
            }

        }catch (Exception e){
            e.printStackTrace();
        }




        list_concursantes = (ListView) findViewById(R.id.list_concursantes);


        try {
            Intent i = new Intent(this, SocketServicio.class);
            startService(i);
            Intent timer = new Intent(this, Temporizador.class);
            startService(timer);
        }catch (Exception e ){
            e.printStackTrace();
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "debe enviar");

                //SendInicio();



            }
        }, 3000);


    }


    public void SendInicio(){
        //Intent intent = new Intent();
        //intent.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
        //intent.putExtra(SocketServicio.KEY_MSG_TO_SERVICE, "JAime lombana");
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //sendBroadcast(intent);

        Intent sendSocket = new Intent();
        sendSocket.putExtra("CMD", "EnvioSocket0");
        sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);
    }

    public void MuestraConcursantes(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(SplashActivity.this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle("Seleccione los concursantes:");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SplashActivity.this, android.R.layout.select_dialog_singlechoice);

        final ArrayList<String> Nombres = new ArrayList<>();
        final ArrayList<String> idConcursante = new ArrayList<>();




        for(int i=0; i < appState.getDatosConcursantes().size(); i++){
            arrayAdapter.add(appState.getDatosConcursantes().get(i).getNombres());
            Nombres.add(appState.getDatosConcursantes().get(i).getNombres());
            idConcursante.add(appState.getDatosConcursantes().get(i).getIdConcursante());
        }
//        arrayAdapter.add("Hardik");
//        arrayAdapter.add("Archit");
//        arrayAdapter.add("Jignesh");
//        arrayAdapter.add("Umang");
//        arrayAdapter.add("Gatti");

        builderSingle.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        final CharSequence[] Correos = Nombres.toArray(new String[Nombres.size()]);
        final CharSequence[] IdPlaca = idConcursante.toArray(new String[idConcursante.size()]);

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(SplashActivity.this);
                builderInner.setMessage(strName);
                builderInner.setTitle("Su selección es:");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //String SelectId = IdPlaca[which].toString();

                        Log.i(TAG, "----El Id es:  "  +  arrayAdapter.getCount());

                        DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                        datosTransferDTO.setFuncion(Funciones.CARGA_DATOS);
                        datosTransferDTO.setIdConcursante("");


                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }


    public void MuestraConcursantes2(){


        salidasAdapter = new ListInventarioAdapter(this, Arrays.asList(androidFlavors));


        // Get a reference to the ListView, and attach this adapter to it.

        list_concursantes.setAdapter(salidasAdapter);


        list_concursantes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG,  salidasAdapter.getItem(position).getVersionName()+"");
                Log.i(TAG,  salidasAdapter.getItem(position).getVersionNumber()+"");

                MuestraProcessDialog("Ingresando...");

                Intent sendSocket = new Intent();
                sendSocket.putExtra("CMD", "EnvioSocket");
                sendSocket.putExtra("DATA", salidasAdapter.getItem(position).getVersionNumber());
                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);

            }
        });

    }

    public void MuestraProcessDialog(String mensaje){
        progressDialog = new ProgressDialog(SplashActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumeeeen");

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

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                try {
                    Intent i = new Intent(this, SocketServicio.class);
                    stopService(i);
                    Intent timer = new Intent(this, Temporizador.class);
                    stopService(timer);
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
