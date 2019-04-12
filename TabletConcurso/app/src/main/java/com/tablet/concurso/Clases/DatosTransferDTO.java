package com.tablet.concurso.Clases;

import java.util.ArrayList;

public class DatosTransferDTO {


    private String funcion;
    private String nombre;
    private String valor;
    private String foto;
    private String idConcursante;
    private ArrayList<DatosConcursantesDTO> listaNombres;
    private String apuesta;


    public String getFuncion() {
        return funcion;
    }

    public void setFuncion(String funcion) {
        this.funcion = funcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getIdConcursante() {
        return idConcursante;
    }

    public void setIdConcursante(String idConcursante) {
        this.idConcursante = idConcursante;
    }

    public ArrayList<DatosConcursantesDTO> getListaNombres() {
        return listaNombres;
    }

    public void setListaNombres(ArrayList<DatosConcursantesDTO> listaNombres) {
        this.listaNombres = listaNombres;
    }

    public String getApuesta() {
        return apuesta;
    }

    public void setApuesta(String apuesta) {
        this.apuesta = apuesta;
    }
}
