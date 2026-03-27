package com.example.campamentoguadalupe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ActividadAdapter extends ArrayAdapter<Actividad> {

    public ActividadAdapter(Context context, List<Actividad> actividades) {
        super(context, 0, actividades);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Actividad actividad = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_actividad, parent, false);
        }

        TextView txtNombre = convertView.findViewById(R.id.txtNombreActividad);
        txtNombre.setText(actividad.getTitulo());

        return convertView;
    }
}


