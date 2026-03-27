package com.example.campamentoguadalupe;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

public class AlumnoAdapter extends ArrayAdapter<Alumno> {

    public AlumnoAdapter(@NonNull Context context, @NonNull List<Alumno> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_alumno, parent, false);
        }

        Alumno alumno = getItem(position);

        TextView txtAlumno = convertView.findViewById(R.id.txtAlumno);
        LinearLayout itemLayout = convertView.findViewById(R.id.itemAlumnoLayout);

        txtAlumno.setText(alumno.getNombreCompleto());

        if (alumno.isAutorizacion() && alumno.isFichaMedica()) {
            itemLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.verde_completo));
        } else {
            itemLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.naranja_incompleto));
        }

        return convertView;
    }
}

