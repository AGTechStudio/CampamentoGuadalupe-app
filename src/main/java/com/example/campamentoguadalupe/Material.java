package com.example.campamentoguadalupe;

public class Material {
    public String id;
    public String nombre;
    public boolean completado;

    public Material() {
        // Constructor vacío para Firebase
    }

    public Material(String id, String nombre, boolean completado) {
        this.id = id;
        this.nombre = nombre;
        this.completado = completado;
    }
}

