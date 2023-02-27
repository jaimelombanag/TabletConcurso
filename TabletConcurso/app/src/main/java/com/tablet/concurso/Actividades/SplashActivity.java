package com.tablet.concurso.Actividades;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.tablet.concurso.viewModel.SocketViewModel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.arch.lifecycle.ViewModelProviders;

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
    private int REQUEST_PERMISSION =1;
    private int REQUEST_PERMISSION2 =2;
    private String file = "IP_Direccion.txt";

    SocketViewModel socketViewModel;


//    ModelInventario[] androidFlavors = {
//            new ModelInventario("CT 02", "- Sin Confirmar", R.mipmap.ic_launcher, R.mipmap.ic_launcher),
//            new ModelInventario("CT 01", "- Confirmado", R.mipmap.ic_launcher, R.mipmap.ic_launcher),
//
//    };

    ModelInventario[] androidFlavors;

    //ArrayList<ModelInventario> dataModels;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Context context = getApplicationContext();
        appState = ((Globales) context);

        /*******************************Para que La pantalla no se apague*********************/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txt_ipdireccion = (EditText) findViewById(R.id.txt_ipdireccion);
        Permisos();

//
//        try {
//            String valor = getIntent().getExtras().getString("relogin");
//            if(valor.equalsIgnoreCase("relogin")){
////                Intent sendSocket = new Intent();
////                sendSocket.putExtra("CMD", "EnvioSocket0");
////                sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
////                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);
//
//                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
//                datosTransferDTO.setFuncion(Funciones.LOGIN);
//                Gson gson = new Gson();
//                String json = gson.toJson(datosTransferDTO);
//                sendData = new ConnexionTCP(getApplicationContext());
//                sendData.sendData(json);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        list_concursantes = (ListView) findViewById(R.id.list_concursantes);
        appState.setTimerSend(0);

        //LeeIpInicial();
        ListaDatos();
        LeerIp();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constantes.bloqueo, "false");
        editor.commit();



        //Getting ViewModel for current activity
        socketViewModel = ViewModelProviders.of(this).get(SocketViewModel.class);
        socketViewModel.getRespuesta().observe(this, new Observer<DatosTransferDTO>() {
            @Override
            public void onChanged(DatosTransferDTO rtasocket) {
                Log.i(TAG, "-------- Respuesta Socket ViewModel Splash:  " + new Gson().toJson(rtasocket));


                if(rtasocket != null){
                    if(rtasocket.getFuncion().equalsIgnoreCase(Funciones.LOGIN)) {
                        androidFlavors = new ModelInventario[appState.getDatosConcursantes().size()];
                        for (int i = 0; i < appState.getDatosConcursantes().size(); i++) {

                            Log.i(TAG, "==========Nombre: " + appState.getDatosConcursantes().get(i).getNombres());
                            Log.i(TAG, "==========Id: " + appState.getDatosConcursantes().get(i).getIdConcursante());

                            androidFlavors[i] = new ModelInventario(appState.getDatosConcursantes().get(i).getNombres(), appState.getDatosConcursantes().get(i).getIdConcursante(), R.mipmap.ic_launcher, R.mipmap.ic_launcher);

                        }
                        MuestraConcursantes2();
                    }
                    if(rtasocket.getFuncion().equalsIgnoreCase(Funciones.CARGA_DATOS)) {

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        if(sharedPreferences.getString(Constantes.closeApp, "").equalsIgnoreCase("false")){
                            try{progressDialog.dismiss();}catch (Exception e){}
                            Intent activity = new Intent();
                            activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.setClass(getApplicationContext(), GridActivity.class);
                            getApplicationContext().startActivity(activity);
                            finish();
                        }

                    }
                }else{
                    Toast.makeText(getApplicationContext(), "ERROR DE CONEXION DE SOKET", Toast.LENGTH_SHORT).show();
                }



            }
        });
    }



    /**********************************************************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/
    public void LeeIpInicial(){
        String strFileName = file; // file name
        File myFile = new File(Environment.getExternalStorageDirectory() + "/Download/"); // file path
        if (!myFile.exists()) { // directory is exist or not
            myFile.mkdirs();    // if not create new
            Log.e("DataStoreSD 0 ", myFile.toString());
        } else {
            myFile = new File(Environment.getExternalStorageDirectory() + "/Download/");
            Log.e("DataStoreSD 1 ", myFile.toString());
        }

        try {
            File Notefile = new File(myFile, strFileName);
            FileWriter writer = new FileWriter(Notefile); // set file path & name to write
            writer.append("1.1.1.1"); // write string
            writer.flush();
            writer.close();
            Log.e("DataStoreSD 2 ", myFile.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Permisos(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

            //return;
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION2);

            return;
        }
    }


    /**********************************************************************************************/
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }else  if (requestCode == REQUEST_PERMISSION2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
            } else {
                // User refused to grant permission.
            }
        }
    }
    /**********************************************************************************************/
    public void LeerIp(){
        String line = null;

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(Environment.getExternalStorageDirectory() + "/Download/" + file));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while ( (line = bufferedReader.readLine()) != null )
            {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            line = stringBuilder.toString();

            line = line.replace(" ", "");
            line = line.replace("\r\n", "");
            line = line.replace("\r", "");
            line = line.replace("\n", "");


            Log.i(TAG, "-----Lo q se lee es"  +  line + "-----");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(line.equalsIgnoreCase("1.1.1.1")){
                editor.putString(Constantes.IPSocket, "192.168.122.100");
            }else{
                editor.putString(Constantes.IPSocket, line);
            }
            editor.commit();

            bufferedReader.close();
        }
        catch(FileNotFoundException ex) {
            Log.d(TAG, ex.getMessage());
        }
        catch(IOException ex) {
            Log.d(TAG, ex.getMessage());
        }
    }

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
                editor.putString(Constantes.closeApp, "false");
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
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(Constantes.IPSocket, txt_ipdireccion.getText().toString());
//        editor.commit();

        //ListaDatos();
        sendData = new ConnexionTCP(getApplicationContext());
        sendData.sendData("{\"funcion\":\"02\",\"idConcursante\":\"5\"}\n\r");
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


    }

    @Override
    public void finish() {
        super.finish();
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
