package com.tablet.concurso.Servicios;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.tablet.concurso.Clases.Globales;

import java.util.Timer;
import java.util.TimerTask;

public class Temporizador extends Service {

    private static String TAG = "Concurso";
    private final String ACTION_STRING_ACTIVITY = "ToActivity";
    private Timer timer = new Timer();
    private Globales appState;
    private Context context;
    private final String ACTION_STRING_SERVICE = "ToServiceTimer";
    private static int tiempoReconexion = 10;                            //Tiempo para hacer la reconexion si el servidor esta caido
    private int contador;



    public Temporizador() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        Log.i(TAG, "**************      SE INICIALIZA SERVICIO DE  TIEMPO    ************************");
        super.onCreate();
        startService();

    }


    private void startService() {
        context = getApplicationContext();
        appState = (Globales) getApplicationContext();
        timer.scheduleAtFixedRate(new mainTask(), 0, 1000);

    }

    /******************************************************************************************************************************/
    /*****************************      CLASE PARA VERIFICACION DEL TIMER              ********************************************/
    /******************************************************************************************************************************/
    private class mainTask extends TimerTask {


        public void run() {
            contador++;
            if(contador > tiempoReconexion) {
                contador = 0;
                Log.i(TAG, "======TIMER: " + appState.isSocketConnected());
                if(!appState.isSocketConnected()){
                    Intent sendSocket = new Intent();
                    sendSocket.putExtra("CMD", "reconecta");
                    sendSocket.setAction(SocketServicio.ACTION_MSG_TO_SERVICE);
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(sendSocket);
                }
            }

            if(appState.getTimerSend()==1){


            }


        }
    }


}
