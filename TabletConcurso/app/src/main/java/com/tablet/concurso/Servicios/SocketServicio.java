package com.tablet.concurso.Servicios;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.Clases.DatosConcursantesDTO;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;



/**
 * Created by jalombanag on 14/09/2016.
 */

public class SocketServicio extends Service {



    private static final String TAG = "TaxisLibresConductor";
    private final String ACTION_STRING_SERVICE = "ToService";
    private final String ACTION_STRING_ACTIVITY = "ToActivity";
    private Globales appState;
    private Context context;
    public static List<String> buffer;
    private Socket socket = null;
    private BufferedWriter escribirSocket;
    private BufferedReader leerSocket;
    private Thread thrd;
    private boolean forzarCierre =false;
    private static int tiempoReconexion = 120000;                            //Tiempo para hacer la reconexion si el servidor esta caido
    private  boolean reintentarConexion=false;



    //from MainActivity to MyService
    public static final String KEY_MSG_TO_SERVICE = "KEY_MSG_TO_SERVICE";
    public static final String ACTION_MSG_TO_SERVICE = "MSG_TO_SERVICE";

    MyServiceReceiver myServiceReceiver;

    /**********************************************************************************************/
    /****************** Hilo para la conexion del Socker con el Servidor **************************/
    /**********************************************************************************************/
    private void iniciarHilo(){
        try {
            thrd = new Thread(){
                public void run(){
                    do{
                        if (!appState.isSocketConnected()) {
                            reintentarConexion=conectarAServidor();
                            try {
                                sleep(1000);
                                Log.i(TAG, "REINTENTAR CONEXION!!!"  +  reintentarConexion);
                                appState.setSocketConnected(false);
                            } catch (InterruptedException e) {e.printStackTrace();}
                        }
                    }while (reintentarConexion && !forzarCierre);
                    thrd.currentThread().interrupt();
                }
            };
            thrd.start();
        }catch(Exception e){
            Log.i(TAG, e.getMessage());
        }
    }
    /**********************************************************************************************/
    /****************** Funcione Para Inicializar el Servicio del Socket **************************/
    /**********************************************************************************************/
    public boolean conectarAServidor(){
        if (!appState.isSocketConnected()) {
            try {

                Log.i(TAG, "=============================PRODUCCION ");socket = new Socket(Constantes.IPSocket, Constantes.PuertoSocket);

                leerSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                escribirSocket = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "LATIN1"));
                Log.i(TAG, "SOCKET CONECTADO!!!");
                Log.i(TAG, "PUERTO DE SOCKET: "  +  socket.getLocalPort());
                SharedPreferences.Editor settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                settings.putString(Constantes.puertoSocketInterno, ""+socket.getLocalPort());
                settings.commit();
                forzarCierre =false;
                reintentarConexion = true;
                appState.setSocketConnected(true);
                /*************************************************************************
                 *      si hay q enviar una solicitud de inicio de socke                 */
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                datosTransferDTO.setFuncion(Funciones.LOGIN);

                Gson gson = new Gson();
                String json = gson.toJson(datosTransferDTO);
                escribirSocket.write(json+ "\r\n");
                escribirSocket.flush();
                /*************************************************************************/
                String data ;
                try{
                    while ( (data = leerSocket.readLine()) != null) {
                        Log.i(TAG, "------------------------------------------------------Data x Socket: "+data);
                        ProcessRecepcion(data);
                    }
                }catch (Exception e){
                    //e.printStackTrace();
                }

            }catch (ConnectException ce) {
                Log.i(TAG, "EL SERVIDOR ESTA CAIDO!!!");
                desconectarDeServidor();
                appState.setSocketConnected(false);
                reintentarConexion = false;
                return  reintentarConexion;
            } catch (SocketTimeoutException steo) {
                Log.i(TAG, "SE DEMORO LA CONEXION !!!");
            } catch (SocketException se) {
                se.printStackTrace();
                Log.i(TAG, "ERROR EN LA CONEXION!!!");
                desconectarDeServidor();
            } catch(IOException ioe) {
                Log.i(TAG, "ERROR DE ENTRADA SALIDA!!!");
                desconectarDeServidor();
                appState.setSocketConnected(false);
                reintentarConexion = false;
                forzarCierre =true;
                return  reintentarConexion;
            }catch(Exception e){
                Log.i(TAG, e.getMessage());
            }
        }
        return  reintentarConexion;
    }
    /**********************************************************************************************/
    /******************  Funcione Para Finalizar el Servicio del Socket  **************************/
    /**********************************************************************************************/
    public void desconectarDeServidor(){
        appState.setSocketConnected(false);
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
                Log.i(TAG, "SE CIERRA EL SOCKET!!!");
            } catch (Exception e) {
                Log.i(TAG, e.getMessage());
            }
        }
    }
    /**********************************************************************************************/
    /****************** Funcione Para Quitar los Caracteres Especiales   **************************/
    /**********************************************************************************************/
    public static String quitarCaracteresEsp(String input) {
        String output = null;
        try {
            // Cadena de caracteres original a sustituir.
            String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
            // Cadena de caracteres ASCII que reemplazarÃ¡n los originales.
            String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
            output = input;
            for (int i = 0; i < original.length(); i++) {
                // Reemplazamos los caracteres especiales.
                output = output.replace(original.charAt(i), ascii.charAt(i));
            }//for i

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;

    }
    /**********************************************************************************************/
    /*********************   Funcion Para Procesar lo que llega del Socket  ***********************/
    /**********************************************************************************************/
    public void ProcessRecepcion(String datos){
        try {

            Gson gson=new Gson();
            DatosTransferDTO informacion = null;
            try {
                informacion = gson.fromJson(datos, DatosTransferDTO.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Log.i("TaxistaPanama", "-------------Error-------------------   " + informacion.getError());
            if (informacion.getFuncion().equalsIgnoreCase(Funciones.LOGIN)) {


                for(int i=0; i< informacion.getListaNombres().size(); i++){

                    DatosConcursantesDTO list = new DatosConcursantesDTO();
                    list.setNombres(informacion.getListaNombres().get(i).getNombres());
                    list.setIdConcursante(informacion.getListaNombres().get(i).getIdConcursante());

                    appState.getDatosConcursantes().add(list);

                }




                Intent new_intent = new Intent();
                new_intent.putExtra("CMD", "listaCOncursantes");
                new_intent.putExtra("DATA", datos);
                new_intent.setAction(ACTION_STRING_ACTIVITY);
                sendBroadcast(new_intent);

            }else  if (informacion.getFuncion().equalsIgnoreCase(Funciones.CARGA_DATOS)) {



                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constantes.nombresConcursantes, informacion.getNombre());
                editor.putString(Constantes.valorConcursantes, informacion.getValor());
                editor.putString(Constantes.fotoConcursantes, informacion.getFoto());
                editor.commit();


                Intent new_intent = new Intent();
                new_intent.putExtra("CMD", "ingreso");
                new_intent.putExtra("DATA", datos);
                new_intent.setAction(ACTION_STRING_ACTIVITY);
                sendBroadcast(new_intent);


            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /****************************************************************
     * FUNCION PARA SABER LA DISTANCIA DE DOS PINTOS EN LINEA RECTA
     ***************************************************************/
    public Integer restarCoordenadas(double latSrv, double lonSrv, double latTaxi, double lonTaxi) {
        double lon = lonTaxi - lonSrv;
        double res = Math.sin(latSrv * 0.01745329D) * Math.sin(latTaxi * 0.01745329D) + Math.cos(latSrv * 0.01745329D) * Math.cos(latTaxi * 0.01745329D) * Math.cos(lon * 0.01745329D);
        double res1 = Math.acos(res) * 57.295779510000003D;
        Double metros = res1 * 111.30200000000001D * 1000.0D;
        return metros.intValue();
    }

    /**********************************************************************************************/


    /**********************************************************************************************/
    /**************************************** Funciones con @Overide ******************************/
    /**********************************************************************************************/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        Log.i(TAG, "onCreate");
        super.onCreate();
        try {
            context = getApplicationContext();
            appState = (Globales) context;
            //myServiceReceiver = new MyServiceReceiver();
            if(android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.HONEYCOMB||android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.HONEYCOMB_MR1||android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.HONEYCOMB_MR2){
                Log.i(TAG,  "...............   Esta es una Version 3 de Android ..................");
                StrictMode.ThreadPolicy polycy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(polycy);
            }

            int SDK_INT = android.os.Build.VERSION.SDK_INT;

            if (SDK_INT > 8)
            {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();
                StrictMode.setThreadPolicy(policy);

                // Where you get exception write that code inside this.
            }



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MSG_TO_SERVICE);
        registerReceiver(serviceReceiver, intentFilter);
        //registerReceiver(myServiceReceiver, intentFilter);

        //LocalBroadcastManager.getInstance(context).registerReceiver(myServiceReceiver, intentFilter);

        LocalBroadcastManager.getInstance(context).registerReceiver(serviceReceiver, intentFilter);


        iniciarHilo();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        forzarCierre =true;
        desconectarDeServidor();
        thrd.currentThread().interrupt();

        //unregisterReceiver(myServiceReceiver);
        unregisterReceiver(serviceReceiver);
    }


    /**********************************************************************************************/
    /******************      Funcione el BoradCast del Socket       *******************************/
    /**********************************************************************************************/
    private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.i(TAG, "Funcion recibida desde la actividad En el SOCKET: "+ intent.getStringExtra("CMD"));
            if (intent.getStringExtra("CMD").equalsIgnoreCase("EnvioSocket")) {
                try {
                    String datosEnviar = intent.getStringExtra("DATA");


                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constantes.idConcursantes, datosEnviar);
                    editor.commit();

                    DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                    datosTransferDTO.setFuncion(Funciones.CARGA_DATOS);
                    datosTransferDTO.setIdConcursante(datosEnviar);

                    Gson gson = new Gson();
                    String json = gson.toJson(datosTransferDTO);

                    Log.i(TAG, "Esta conectado: " + appState.isSocketConnected());

                    Log.i(TAG, "----se envia: " + json + "\r\n");

                    escribirSocket.write(json+ "\r\n");
                    escribirSocket.flush();



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if (intent.getStringExtra("CMD").equalsIgnoreCase("EnvioSocket2")) {
                try {
                    String datosEnviar = intent.getStringExtra("DATA");

                    DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                    datosTransferDTO.setFuncion(Funciones.SEND_VALOR);
                    datosTransferDTO.setIdConcursante(datosEnviar);
                    datosTransferDTO.setValor(datosEnviar);

                    Gson gson = new Gson();
                    String json = gson.toJson(datosTransferDTO);

                    Log.i(TAG, "----se envia: " + json + "\r\n");

                    escribirSocket.write(json+ "\r\n");
                    escribirSocket.flush();


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };



    /**********************************************************************************************/
    /******************      Funcione el BoradCast del Socket       *******************************/
    /**********************************************************************************************/



    public class MyServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ACTION_MSG_TO_SERVICE)){
                String msg = intent.getStringExtra(KEY_MSG_TO_SERVICE);
                String msg2 = intent.getStringExtra(KEY_MSG_TO_SERVICE);

                msg = new StringBuilder(msg).reverse().toString();
                msg2 = new StringBuilder(msg2).toString();

                Log.i(TAG, "============REVERSE  "  +  msg);
                Log.i(TAG, "============ok  "  +  msg2);





            }
        }
    }


    /**********************************************************************************************/
    /**********************************************************************************************/

}
