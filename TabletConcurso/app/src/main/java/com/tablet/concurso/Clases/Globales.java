package com.tablet.concurso.Clases;

import android.app.Application;

import java.util.ArrayList;

public class Globales extends Application {

    private String TAG = "TaxisLibres";
    private boolean socketConnected = false;
    private int timerSend;

    ArrayList<DatosConcursantesDTO> datosConcursantes;



    @Override
    public void onCreate() {
        super.onCreate();
        datosConcursantes = new ArrayList<>();

    }


    public boolean isSocketConnected() {
        return socketConnected;
    }

    public void setSocketConnected(boolean socketConnected) {
        this.socketConnected = socketConnected;
    }


    public ArrayList<DatosConcursantesDTO> getDatosConcursantes() {
        return datosConcursantes;
    }

    public void setDatosConcursantes(ArrayList<DatosConcursantesDTO> datosConcursantes) {
        this.datosConcursantes = datosConcursantes;
    }

    public int getTimerSend() {
        return timerSend;
    }

    public void setTimerSend(int timerSend) {
        this.timerSend = timerSend;
    }
}
