package com.example.campamentoguadalupe;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class DetalleActividadActivity extends AppCompatActivity {

    EditText edtTitulo, edtDescripcion;
    Button btnGuardar, btnVolver;

    FirebaseFirestore db;
    CollectionReference actividadesRef;
    String campamento, idActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_actividad);

        edtTitulo = findViewById(R.id.edtTitulo);
        edtDescripcion = findViewById(R.id.edtDescripcion);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVolver = findViewById(R.id.btnVolver);

        campamento = getIntent().getStringExtra("campamento");
        idActividad = getIntent().getStringExtra("id");
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");

        if (titulo != null) edtTitulo.setText(titulo);
        if (descripcion != null) edtDescripcion.setText(descripcion);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        actividadesRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("actividades");

        btnGuardar.setOnClickListener(v -> guardarActividad());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void guardarActividad() {
        String titulo = edtTitulo.getText().toString().trim();
        String descripcion = edtDescripcion.getText().toString().trim();

        if (titulo.isEmpty()) {
            Toast.makeText(this, "Ingrese un título", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = (idActividad != null) ? idActividad : actividadesRef.document().getId();

        Map<String, Object> data = new HashMap<>();
        data.put("titulo", titulo);
        data.put("descripcion", descripcion);

        actividadesRef.document(id).set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
                });
    }
}

