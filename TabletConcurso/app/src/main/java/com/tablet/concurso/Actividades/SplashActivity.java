package com.tablet.concurso.Actividades;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tablet.concurso.Adapters.ListInventarioAdapter;
import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;
import com.tablet.concurso.ModelInventario;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.ConnexionTCP;
import com.tablet.concurso.Servicios.SocketServicio;
import com.tablet.concurso.Servicios.Temporizador;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
    private final static String FILE = "ipSocket.txt";
    private ConnexionTCP sendData;
    private EditText txt_ipdireccion;


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


        txt_ipdireccion = (EditText) findViewById(R.id.txt_ipdireccion);

        try {
            String valor = getIntent().getExtras().getString("relogin");
            if(valor.equalsIgnoreCase("relogin")){
//                Intent sendSocket = new Intent();
//                sendSocket.putExtra("CMD", "EnvioSocket0");
//                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);

                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.LOGIN);
                Gson gson = new Gson();
                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);
            }

        }catch (Exception e){
            e.printStackTrace();
        }




        list_concursantes = (ListView) findViewById(R.id.list_concursantes);

        appState.setTimerSend(0);

        establecerIpInicial();
        ListaDatos();


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.IPSocket, txt_ipdireccion.getText().toString());
        editor.commit();


//        try {
//            Intent i = new Intent(this, SocketServicio.class);
//            startService(i);
//            Intent timer = new Intent(this, Temporizador.class);
//            startService(timer);
//        }catch (Exception e ){
//            e.printStackTrace();
//        }
//
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//                Log.i(TAG, "debe enviar");
//
//                //SendInicio();
//
//
//
//            }
//        }, 3000);


    }


    /**********************************************************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/
    private void establecerIpInicial() {
        // Comprobamos si existe el archivo
        if (existsFile(FILE)) {
            // En el caso de que exista, intentamos rellenar los EditText, si no
            // se rellenan de forma correcta, el archivo.txt estaba corrupto.
            Log.i(TAG, "Existe");
            if (!saveIpDireccion()) {
                Log.i(TAG, "Salva");
                // Avisamos al usuario de que el archivo era corrupto.
                Toast.makeText(SplashActivity.this,
                        "Archivo corrupto, reiniciando parámetros...",
                        Toast.LENGTH_LONG).show();
                // Creamos de nuevo el archivo.
                crearArchivoIp();
                // Rellenamos los EditText con los valores asignados por defecto
                // en el archvio txt.
                saveIpDireccion();
            }else{
                Log.i(TAG, "No Salva");
            }

        } else {
            Log.i(TAG, "No Existe");
            // En el caso de que no existiera el archivo txt lo creamos y rellenamos
            // los EditText.
            crearArchivoIp();
            saveIpDireccion();
        }
    }

    /**********************************************************************************************/
    public boolean existsFile(String fileName) {
        for (String tmp : fileList()) {
            if (tmp.equals(fileName))
                return true;
        }
        return false;
    }
    /**********************************************************************************************/
    private void crearArchivoIp() {
        try {
            // Creamos un objeto OutputStreamWriter, que será el que nos permita
            // escribir en el archivo de texto. Si el archivo no existía se creará
            // automáticamente.
            // La ruta en la que se creará el archivo será /ruta de nuestro programa/data/data/

            OutputStreamWriter outSWMensaje = new OutputStreamWriter(
                    openFileOutput(FILE, Context.MODE_PRIVATE));
            // Escribimos los 5 tiempos iniciales en el archivo.
            outSWMensaje.write("5\n5\n5\n5\n5\n");
            // Cerramos el flujo de escritura del archivo, este paso es obligatorio,
            // de no hacerlo no se podrá acceder posteriormente al archivo.
            outSWMensaje.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**********************************************************************************************/
    private boolean saveIpDireccion() {
        try {
            // Creamos un objeto InputStreamReader, que será el que nos permita
            // leer el contenido del archivo de texto.
            InputStreamReader archivo = new InputStreamReader(
                    openFileInput(FILE));
            // Creamos un objeto buffer, en el que iremos almacenando el contenido
            // del archivo.
            BufferedReader br = new BufferedReader(archivo);
            // Por cada EditText leemos una línea y escribimos el contenido en el
            // EditText.
            String texto = br.readLine();
            //et1Alarma.setText(texto);


            // Cerramos el flujo de lectura del archivo.
            br.close();
            return true;

        } catch (Exception e) {
            return false;
        }
    }
    /**********************************************************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/
    public void SendInicio(){
        //Intent intent = new Intent();
        //intent.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
        //intent.putExtra(SocketServicio.KEY_MSG_TO_SERVICE, "JAime lombana");
        //LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //sendBroadcast(intent);


        DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
        datosTransferDTO.setFuncion(Funciones.LOGIN);
        Gson gson = new Gson();
        String json = gson.toJson(datosTransferDTO);
        sendData = new ConnexionTCP(getApplicationContext());
        sendData.sendData(json);

//        Intent sendSocket = new Intent();
//        sendSocket.putExtra("CMD", "EnvioSocket0");
//        sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);
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

//                Intent sendSocket = new Intent();
//                sendSocket.putExtra("CMD", "EnvioSocket");
//                sendSocket.putExtra("DATA", salidasAdapter.getItem(position).getVersionNumber());
//                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
//                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);


                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constantes.idConcursantes, salidasAdapter.getItem(position).getVersionNumber());
                editor.commit();

                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.CARGA_DATOS);
                datosTransferDTO.setIdConcursante(salidasAdapter.getItem(position).getVersionNumber());

                Gson gson = new Gson();
                String json = gson.toJson(datosTransferDTO);
                sendData = new ConnexionTCP(getApplicationContext());
                sendData.sendData(json);

            }
        });

    }

    public void MuestraProcessDialog(String mensaje){
        progressDialog = new ProgressDialog(SplashActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();
    }


    public void Listadatos(View v){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.IPSocket, txt_ipdireccion.getText().toString());
        editor.commit();

        ListaDatos();
    }

    public void ListaDatos(){
        DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
        datosTransferDTO.setFuncion(Funciones.LOGIN);

        Gson gson = new Gson();
        String json = gson.toJson(datosTransferDTO);
        sendData = new ConnexionTCP(getApplicationContext());
        sendData.sendData(json);
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
