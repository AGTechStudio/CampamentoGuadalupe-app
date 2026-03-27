package com.example.campamentoguadalupe;

public class Alumno {
    String id;
    String apellido;
    String nombre;
    boolean autorizacion;
    boolean fichaMedica;

    public Alumno() {
        // Constructor vacío necesario para Firebase
    }

    public Alumno(String id, String apellido, String nombre, boolean autorizacion, boolean fichaMedica) {
        this.id = id;
        this.apellido = apellido;
        this.nombre = nombre;
        this.autorizacion = autorizacion;
        this.fichaMedica = fichaMedica;
    }

    public String getId() {
        return id;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isAutorizacion() {
        return autorizacion;
    }

    public boolean isFichaMedica() {
        return fichaMedica;
    }

    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }
}


