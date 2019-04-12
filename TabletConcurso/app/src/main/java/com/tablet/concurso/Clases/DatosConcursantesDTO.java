package com.tablet.concurso.Clases;

public class DatosConcursantesDTO {

    private String idConcursante;
    private String nombres;
    private String valor;
    private String nombreFoto;
    private String foto;

    public String getIdConcursante() {
        return idConcursante;
    }

    public void setIdConcursante(String idConcursante) {
        this.idConcursante = idConcursante;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getNombreFoto() {
        return nombreFoto;
    }

    public void setNombreFoto(String nombreFoto) {
        this.nombreFoto = nombreFoto;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
