package com.tablet.concurso.Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.tablet.concurso.Actividades.GridActivity;
import com.tablet.concurso.Actividades.MainActivity;
import com.tablet.concurso.Clases.Constantes;
import com.tablet.concurso.Clases.DatosConcursantesDTO;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.Clases.Funciones;
import com.tablet.concurso.Clases.Globales;
import com.tablet.concurso.R;
import com.tablet.concurso.Servicios.ConnexionTCP;

import java.util.ArrayList;

public class CoursesGVAdapter extends ArrayAdapter<DatosTransferDTO> {

    private ConnexionTCP sendData;
    private ProgressDialog progressDialog;

    private GridActivity mainActivity;

    // constructor for our list view adapter.
    public CoursesGVAdapter(@NonNull Context context, ArrayList<DatosTransferDTO> dataModalArrayList, GridActivity act) {
        super(context, 0, dataModalArrayList);
        this.mainActivity =  act;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // below line is use to inflate the
        // layout for our item of list view.
        View listitemView = convertView;
        if (listitemView == null) {
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.image_grid, parent, false);
        }

        // after inflating an item of listview item
        // we are getting data from array list inside
        // our modal class.
        final DatosTransferDTO dataModal = getItem(position);

        // initializing our UI components of list view item.
        //TextView nameTV = listitemView.findViewById(R.id.idTVtext);
        final ImageView courseIV = listitemView.findViewById(R.id.idIVimage);

        // after initializing our items we are
        // setting data to our view.
        // below line is use to set data to our text view.
        //nameTV.setText(dataModal.getNombre());



        final Bitmap originalBitmap = StringToBitMap(dataModal.getFoto());
        //asignamos el CornerRadius
        courseIV.setImageBitmap(originalBitmap);



        // in below line we are using Picasso to load image
        // from URL in our Image VIew.
        //Picasso.get().load(dataModal.getImgUrl()).into(courseIV);

        // below line is use to add item
        // click listener for our item of list view.
        listitemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on the item click on our list view.
                // we are displaying a toast message.

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                if(sharedPreferences.getString(Constantes.bloqueo, "").equalsIgnoreCase("true")){
                    Toast.makeText(getContext(), "APLICACIÓN BLOQUEADA", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), "Item clicked is : " + dataModal.getNombre(), Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(Constantes.imagenSelect, dataModal.getFoto());
                    editor.commit();


                    //MuestraProcessDialog("Enviando...");
                    DatosTransferDTO datosTransferDTO = new DatosTransferDTO();
                    ArrayList<DatosConcursantesDTO> listaNombres = new ArrayList<>();
                    DatosConcursantesDTO datosConcursantesDTO = new DatosConcursantesDTO();
                    datosConcursantesDTO.setIdConcursante(dataModal.getIdConcursante());
                    listaNombres.add(datosConcursantesDTO);


                    datosTransferDTO.setFuncion(Funciones.SEND_VALOR);
                    datosTransferDTO.setIdConcursante(sharedPreferences.getString(Constantes.idConcursantes, ""));
                    //datosTransferDTO.setValor(dataModal.getNombre());
                    datosTransferDTO.setListaNombres(listaNombres);



                    //GridActivity gridActivity = new GridActivity();
                    mainActivity.prueba(dataModal.getFoto());

                    Gson gson = new Gson();
                    String json = gson.toJson(datosTransferDTO);
                    sendData = new ConnexionTCP(getContext());
                    sendData.sendData(json);
                }



            }
        });
        return listitemView;
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

    public void MuestraProcessDialog(String mensaje){
        progressDialog = new ProgressDialog(getContext(), R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(mensaje);
        progressDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 3000);

    }

}

