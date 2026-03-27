package com.example.campamentoguadalupe;

import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class MaterialAdapter extends ArrayAdapter<Material> {

    public MaterialAdapter(Context context, List<Material> materiales) {
        super(context, 0, materiales);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Material material = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_material, parent, false);
        }

        TextView txtNombre = convertView.findViewById(R.id.txtNombreMaterial);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);

        txtNombre.setText(material.nombre);
        checkBox.setChecked(material.completado);

        // Maneja el cambio del CheckBox
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            material.completado = isChecked;
            ((MaterialesActivity) getContext()).actualizarEstadoMaterial(material);
        });

        return convertView;
    }
}

