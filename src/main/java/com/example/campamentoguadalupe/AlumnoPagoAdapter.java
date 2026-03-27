package com.example.campamentoguadalupe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AlumnoPagoAdapter extends ArrayAdapter<AlumnoPago> {

    public AlumnoPagoAdapter(Context context, List<AlumnoPago> alumnosPagos) {
        super(context, 0, alumnosPagos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlumnoPago alumnoPago = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pago, parent, false);
        }

        TextView txtNombreAlumno = convertView.findViewById(R.id.txtNombreAlumno);
        TextView txtPagado = convertView.findViewById(R.id.txtPagado);
        TextView txtPendiente = convertView.findViewById(R.id.txtPendiente);

        txtNombreAlumno.setText(alumnoPago.apellidoNombre);
        txtPagado.setText(String.format("Pagado: $%.2f", alumnoPago.totalPagado));
        txtPendiente.setText(String.format("Pendiente: $%.2f", alumnoPago.saldoPendiente));

        return convertView;
    }
}

