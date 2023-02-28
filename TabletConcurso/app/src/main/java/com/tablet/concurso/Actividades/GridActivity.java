package com.tablet.concurso.Actividades;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tablet.concurso.Adapters.CoursesGVAdapter;
import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;
import com.tablet.concurso.ModelInventario;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.ConnexionTCP;
import com.tablet.concurso.Servicios.SocketServicio;
import com.tablet.concurso.viewModel.SocketViewModel;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

public class GridActivity extends AppCompatActivity{

    private Globales appState;
    private static final String TAG = "Concurso";
    private final String ACTION_STRING_ACTIVITY = "ToActivity";
    GridView mainGrid;
    ArrayList<DatosTransferDTO> dataModalArrayList;
    private ConnexionTCP sendData;
    private ProgressDialog progressDialog;

    private Timer multifuncion = new Timer();
    private int contadorPregunta;

    ImageView imageZoom;
    private TextView txt_nombres;

    private ImageView imagen_concursantes;

    SocketViewModel socketViewModel;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        Context context = getApplicationContext();
        appState = ((Globales) context);

        /*******************************Para que La pantalla no se apague*********************/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mainGrid = (GridView) findViewById(R.id.idGVCourses);
        imageZoom = (ImageView) findViewById(R.id.img_zoom);
        imagen_concursantes = (ImageView) findViewById(R.id.imagen_concursantes);
        txt_nombres = (TextView) findViewById(R.id.txt_nombres);
        mainGrid.setNumColumns(3);
        mainGrid.setVerticalSpacing(1);
        mainGrid.setHorizontalSpacing(1);

        //conexionTcpObservable = ConnexionTCP.getInstance();
        //conexionTcpObservable.addObserver(this);

        dataModalArrayList = new ArrayList<>();

        // here we are calling a method
        // to load data in our list view.
        loadDatainGridView();

        //Getting ViewModel for current activity
        socketViewModel = ViewModelProviders.of(this).get(SocketViewModel.class);
        socketViewModel.getRespuesta().observe(this, new Observer<DatosTransferDTO>() {
            @Override
            public void onChanged(DatosTransferDTO rtasocket) {
                Log.i(TAG, "-------- Respuesta Socket ViewModel GridActivity:  " + new Gson().toJson(rtasocket));

                if(rtasocket.getFuncion().equalsIgnoreCase(Funciones.SEND_VALOR)) {
                    Log.i(TAG, "--------Debe empezar el Timer------------");

                    if(rtasocket.getValor().equalsIgnoreCase("OK")){
                        startTimer();
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                        datosTransferDTO.setFuncion(Funciones.MULTIFUNCION);
                        datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                        Gson gson = new Gson();


                        prueba(sharedPreferences.getString(Constantes.imagenSelect, ""));

                        String json = gson.toJson(datosTransferDTO);
                        sendData = new ConnexionTCP(getApplicationContext());
                        sendData.sendData(json);

                        appState.setTimerSend(1);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constantes.bloqueo, "true");
                        editor.commit();
                    }else{
                        alertDialogMessage(rtasocket.getValor());
                    }

                }else if(rtasocket.getFuncion().equalsIgnoreCase(Funciones.MULTIFUNCION)) {
                    if(rtasocket.getAccion().equalsIgnoreCase("0")){
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

                    }else  if(rtasocket.getAccion().equalsIgnoreCase("1")){

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
                    }else  if(rtasocket.getAccion().equalsIgnoreCase("2")){

                        Intent activity = new Intent();
                        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.putExtra("relogin", "relogin");
                        activity.setClass(getApplicationContext(), SplashActivity.class);
                        getApplicationContext().startActivity(activity);
                        finish();
                    }else  if(rtasocket.getAccion().equalsIgnoreCase("3")){
                        finish();
                    }

                }

            }
        });

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

    public void prueba(String image){
        Log.i(TAG, "Se recibe desde Adapter");
        final Bitmap originalBitmap = StringToBitMap(image);
        //asignamos el CornerRadiusç
        imageZoom.setVisibility(View.VISIBLE);
        imageZoom.setImageBitmap(originalBitmap);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.zoom);
        imageZoom.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageZoom.setVisibility(View.GONE);
            }
        }, 5500);
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte= Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }catch(Exception e){
            e.getMessage();
            return null;
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

        CoursesGVAdapter adapter = new CoursesGVAdapter(GridActivity.this, dataModalArrayList, this);


        // after passing this array list
        // to our adapter class we are setting
        // our adapter to our list view.
        mainGrid.setAdapter(adapter);
        Cargafoto(informacion.getFoto(), informacion.getNombre());
    }

    public void Cargafoto(String foto, String nombre){
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            //extraemos el drawable en un bitmap
            Drawable originalDrawable = getResources().getDrawable(R.drawable.foto_pareja);
            //Bitmap originalBitmap = ((BitmapDrawable) originalDrawable).getBitmap();
            Bitmap originalBitmap = StringToBitMap(foto);
            //creamos el drawable redondeado
            RoundedBitmapDrawable roundedDrawable =
                    RoundedBitmapDrawableFactory.create(getResources(), originalBitmap);
            //asignamos el CornerRadius
            roundedDrawable.setCornerRadius(originalBitmap.getHeight());
            imagen_concursantes.setImageDrawable(roundedDrawable);
            txt_nombres.setText(nombre + "  ");

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "resumeeeen");
        Intent i = new Intent(this, SocketServicio.class);
        stopService(i);
    }

    @Override
    public void finish() {
        super.finish();
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
                    alertDialog();
//                    Intent i = new Intent(this, SocketServicio.class);
//                    stopService(i);
//                    finish();
                }catch (Exception e ){
                    e.printStackTrace();
                }
                return true;
            case KeyEvent.KEYCODE_HOME:
                Log.i(TAG, "Se Oprimio el Boton de Back");

        }
        return super.onKeyDown(keyCode, event);
    }

    public void alertDialog(){
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Salir y Reiniciar");
        dialogo1.setMessage("¿ Desea cerrar y reiniciar la aplicación ?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

                stopTimer();

                finish();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constantes.closeApp, "true");
                        editor.commit();

                        Intent activity = new Intent();
                        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.setClass(getApplicationContext(), SplashActivity.class);
                        getApplicationContext().startActivity(activity);
                    }
                }, 3500);

            }
        });
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

            }
        });
        dialogo1.show();
    }

    public void alertDialogMessage(String message){
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle(message);
        //dialogo1.setMessage("¿ Desea cerrar y reiniciar la aplicación ?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {


            }
        });
        dialogo1.show();
    }


}