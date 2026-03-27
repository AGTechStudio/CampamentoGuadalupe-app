package com.example.campamentoguadalupe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class AlumnosActivity extends AppCompatActivity {

    TextView txtTitulo;
    ListView listViewAlumnos;
    Button btnAgregar;

    ArrayList<Alumno> listaAlumnos = new ArrayList<>();
    AlumnoAdapter adapter;

    FirebaseFirestore db;
    CollectionReference alumnosRef;
    String campamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumnos);

        // FLECHA VOLVER EN ACTIONBAR
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txtTitulo = findViewById(R.id.txtTitulo);
        listViewAlumnos = findViewById(R.id.listViewAlumnos);
        btnAgregar = findViewById(R.id.btnAgregar);

        campamento = getIntent().getStringExtra("campamento");
        txtTitulo.setText("Listado de Alumnos\n" + campamento);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        alumnosRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("alumnos");

        adapter = new AlumnoAdapter(this, listaAlumnos);
        listViewAlumnos.setAdapter(adapter);

        cargarAlumnos();

        // 👉 Click corto: entra al detalle
        listViewAlumnos.setOnItemClickListener((parent, view, position, id) -> {
            Alumno alumnoSeleccionado = listaAlumnos.get(position);

            Intent intent = new Intent(this, DetalleAlumnoActivity.class);
            intent.putExtra("campamento", campamento);
            intent.putExtra("alumnoId", alumnoSeleccionado.id);
            startActivity(intent);
        });

        // 👉 Mantener presionado: eliminar alumno
        listViewAlumnos.setOnItemLongClickListener((parent, view, position, id) -> {
            Alumno alumnoSeleccionado = listaAlumnos.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Alumno")
                    .setMessage("¿Desea eliminar a " + alumnoSeleccionado.getNombreCompleto() + "?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        alumnosRef.document(alumnoSeleccionado.id).delete()
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Alumno eliminado", Toast.LENGTH_SHORT).show();
                                    listaAlumnos.remove(alumnoSeleccionado);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true;  // Importante: indica que el evento fue consumido
        });

        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());
    }

    // MÉTODO PARA MANEJAR LA FLECHA DE VOLVER
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void mostrarDialogoAgregar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Alumno");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);

        final EditText inputApellido = new EditText(this);
        inputApellido.setHint("Apellido");
        layout.addView(inputApellido);

        final EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre");
        layout.addView(inputNombre);

        builder.setView(layout);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String apellido = inputApellido.getText().toString().trim();
            String nombre = inputNombre.getText().toString().trim();

            if (apellido.isEmpty() || nombre.isEmpty()) {
                Toast.makeText(this, "Complete ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = apellido + ", " + nombre;

            Map<String, Object> data = new HashMap<>();
            data.put("apellido", apellido);
            data.put("nombre", nombre);
            data.put("autorizacion", false);
            data.put("fichaMedica", false);

            alumnosRef.document(id).set(data)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Alumno agregado", Toast.LENGTH_SHORT).show();
                        cargarAlumnos();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void cargarAlumnos() {
        alumnosRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            listaAlumnos.clear();
            for (var doc : queryDocumentSnapshots) {
                String id = doc.getId();
                String apellido = doc.getString("apellido");
                String nombre = doc.getString("nombre");
                boolean autorizacion = doc.getBoolean("autorizacion") != null && doc.getBoolean("autorizacion");
                boolean fichaMedica = doc.getBoolean("fichaMedica") != null && doc.getBoolean("fichaMedica");

                listaAlumnos.add(new Alumno(id, apellido, nombre, autorizacion, fichaMedica));
            }
            ordenarLista();
        });
    }

    private void ordenarLista() {
        Collections.sort(listaAlumnos, (a1, a2) -> a1.getNombreCompleto().compareToIgnoreCase(a2.getNombreCompleto()));
        adapter.notifyDataSetChanged();
    }
}






