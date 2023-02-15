package com.tablet.concurso.Actividades;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.Toast;

import com.tablet.concurso.Adapters.CoursesGVAdapter;
import com.tablet.concurso.Clases.DatosTransferDTO;
import com.tablet.concurso.R;

import java.util.ArrayList;

public class GridActivity extends AppCompatActivity {

    GridView mainGrid;
    ArrayList<DatosTransferDTO> dataModalArrayList;




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        mainGrid = (GridView) findViewById(R.id.idGVCourses);

        dataModalArrayList = new ArrayList<>();

        // here we are calling a method
        // to load data in our list view.
        loadDatainGridView();

    }

    private void loadDatainGridView() {
        String foto1 = getString(R.string.foto1);
        foto1.trim();

        DatosTransferDTO datosTransferDTO1 = new DatosTransferDTO();
        datosTransferDTO1.setNombre("Jaime");
        datosTransferDTO1.setAccion("Accion");
        datosTransferDTO1.setApuesta("Apuesta");
        datosTransferDTO1.setIdConcursante("1");
        datosTransferDTO1.setFuncion("Funcion");
        datosTransferDTO1.setValor("Valor");
        datosTransferDTO1.setFoto(foto1);
        dataModalArrayList.add(datosTransferDTO1);

        DatosTransferDTO datosTransferDTO2 = new DatosTransferDTO();
        datosTransferDTO2.setNombre("Claudia");
        datosTransferDTO2.setAccion("Accion");
        datosTransferDTO2.setApuesta("Apuesta");
        datosTransferDTO2.setIdConcursante("1");
        datosTransferDTO2.setFuncion("Funcion");
        datosTransferDTO2.setValor("Valor");
        datosTransferDTO2.setFoto("Foto");
        dataModalArrayList.add(datosTransferDTO2);

        DatosTransferDTO datosTransferDTO3 = new DatosTransferDTO();
        datosTransferDTO3.setNombre("Claudia");
        datosTransferDTO3.setAccion("Accion");
        datosTransferDTO3.setApuesta("Apuesta");
        datosTransferDTO3.setIdConcursante("1");
        datosTransferDTO3.setFuncion("Funcion");
        datosTransferDTO3.setValor("Valor");
        datosTransferDTO3.setFoto("Foto");
        dataModalArrayList.add(datosTransferDTO3);

        DatosTransferDTO datosTransferDTO4 = new DatosTransferDTO();
        datosTransferDTO4.setNombre("Claudia");
        datosTransferDTO4.setAccion("Accion");
        datosTransferDTO4.setApuesta("Apuesta");
        datosTransferDTO4.setIdConcursante("1");
        datosTransferDTO4.setFuncion("Funcion");
        datosTransferDTO4.setValor("Valor");
        datosTransferDTO4.setFoto("Foto");
        dataModalArrayList.add(datosTransferDTO4);

        DatosTransferDTO datosTransferDTO5 = new DatosTransferDTO();
        datosTransferDTO5.setNombre("Claudia");
        datosTransferDTO5.setAccion("Accion");
        datosTransferDTO5.setApuesta("Apuesta");
        datosTransferDTO5.setIdConcursante("1");
        datosTransferDTO5.setFuncion("Funcion");
        datosTransferDTO5.setValor("Valor");
        datosTransferDTO5.setFoto("Foto");
        dataModalArrayList.add(datosTransferDTO5);

        DatosTransferDTO datosTransferDTO6 = new DatosTransferDTO();
        datosTransferDTO6.setNombre("Claudia");
        datosTransferDTO6.setAccion("Accion");
        datosTransferDTO6.setApuesta("Apuesta");
        datosTransferDTO6.setIdConcursante("1");
        datosTransferDTO6.setFuncion("Funcion");
        datosTransferDTO6.setValor("Valor");
        datosTransferDTO6.setFoto("Foto");
        dataModalArrayList.add(datosTransferDTO6);

        CoursesGVAdapter adapter = new CoursesGVAdapter(GridActivity.this, dataModalArrayList);

        // after passing this array list
        // to our adapter class we are setting
        // our adapter to our list view.
        mainGrid.setAdapter(adapter);

    }


}