package com.example.campamentoguadalupe;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class DetalleAlumnoActivity extends AppCompatActivity {

    EditText edtApellido, edtNombre;
    CheckBox chkAutorizacion, chkFichaMedica;
    Button btnGuardar;

    FirebaseFirestore db;
    DocumentReference alumnoRef;

    String campamento, alumnoIdOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_alumno);

        // Mostrar botón de volver en la ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detalle del Alumno");
        }

        edtApellido = findViewById(R.id.edtApellido);
        edtNombre = findViewById(R.id.edtNombre);
        chkAutorizacion = findViewById(R.id.chkAutorizacion);
        chkFichaMedica = findViewById(R.id.chkFichaMedica);
        btnGuardar = findViewById(R.id.btnGuardar);

        campamento = getIntent().getStringExtra("campamento");
        alumnoIdOriginal = getIntent().getStringExtra("alumnoId");

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        alumnoRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("alumnos").document(alumnoIdOriginal);

        cargarDatos();

        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    // Maneja el botón "volver" de la barra superior
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void cargarDatos() {
        alumnoRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                edtApellido.setText(doc.getString("apellido"));
                edtNombre.setText(doc.getString("nombre"));
                chkAutorizacion.setChecked(doc.getBoolean("autorizacion") != null && doc.getBoolean("autorizacion"));
                chkFichaMedica.setChecked(doc.getBoolean("fichaMedica") != null && doc.getBoolean("fichaMedica"));
            }
        });
    }

    private void guardarCambios() {
        String apellido = edtApellido.getText().toString().trim();
        String nombre = edtNombre.getText().toString().trim();
        boolean autorizacion = chkAutorizacion.isChecked();
        boolean fichaMedica = chkFichaMedica.isChecked();

        if (apellido.isEmpty() || nombre.isEmpty()) {
            Toast.makeText(this, "Complete apellido y nombre", Toast.LENGTH_SHORT).show();
            return;
        }

        String nuevoId = apellido + ", " + nombre;

        Map<String, Object> data = new HashMap<>();
        data.put("apellido", apellido);
        data.put("nombre", nombre);
        data.put("autorizacion", autorizacion);
        data.put("fichaMedica", fichaMedica);

        if (nuevoId.equals(alumnoIdOriginal)) {
            alumnoRef.set(data)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show());
        } else {
            DocumentReference nuevoRef = db.collection("usuarios").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .collection("campamentos").document(campamento)
                    .collection("alumnos").document(nuevoId);

            nuevoRef.set(data).addOnSuccessListener(unused -> {
                alumnoRef.delete();
                Toast.makeText(this, "Datos guardados con nuevo nombre", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
            });
        }
    }
}



