package com.example.campamentoguadalupe;

public class AlumnoPago {
    public String id;
    public String apellidoNombre;
    public double totalPagado;
    public double saldoPendiente;

    public AlumnoPago(String id, String apellidoNombre, double totalPagado, double saldoPendiente) {
        this.id = id;
        this.apellidoNombre = apellidoNombre;
        this.totalPagado = totalPagado;
        this.saldoPendiente = saldoPendiente;
    }
}
