package com.example.campamentoguadalupe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class ActividadesActivity extends AppCompatActivity {

    ListView listView;
    Button btnAgregar, btnVolver;
    ArrayList<Actividad> lista = new ArrayList<>();
    ActividadAdapter adapter;

    FirebaseFirestore db;
    CollectionReference actividadesRef;
    String campamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividades);

        listView = findViewById(R.id.listViewActividades);
        btnAgregar = findViewById(R.id.btnAgregar);
        btnVolver = findViewById(R.id.btnVolver);

        campamento = getIntent().getStringExtra("campamento");
        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        actividadesRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("actividades");

        adapter = new ActividadAdapter(this, lista);
        listView.setAdapter(adapter);

        cargarActividades();

        // 👉 Click corto: Editar
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Actividad act = lista.get(position);
            Intent intent = new Intent(this, DetalleActividadActivity.class);
            intent.putExtra("campamento", campamento);
            intent.putExtra("id", act.getId());
            intent.putExtra("titulo", act.getTitulo());
            intent.putExtra("descripcion", act.getDescripcion());
            startActivity(intent);
        });

        // 🔥 Click largo: Eliminar
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Actividad act = lista.get(position);
            mostrarDialogoEliminar(act);
            return true;
        });

        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(this, DetalleActividadActivity.class);
            intent.putExtra("campamento", campamento);
            startActivity(intent);
        });

        btnVolver.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarActividades();
    }

    private void cargarActividades() {
        actividadesRef.get().addOnSuccessListener(query -> {
            lista.clear();
            for (DocumentSnapshot doc : query.getDocuments()) {
                String id = doc.getId();
                String titulo = doc.getString("titulo");
                String descripcion = doc.getString("descripcion");
                if (titulo != null) {
                    lista.add(new Actividad(id, titulo, descripcion));
                }
            }
            Collections.sort(lista, Comparator.comparing(Actividad::getTitulo, String.CASE_INSENSITIVE_ORDER));
            adapter.notifyDataSetChanged();
        });
    }

    // 🔥 Función para eliminar con confirmación
    private void mostrarDialogoEliminar(Actividad actividad) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar actividad")
                .setMessage("¿Desea eliminar \"" + actividad.getTitulo() + "\"?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    actividadesRef.document(actividad.getId()).delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Actividad eliminada", Toast.LENGTH_SHORT).show();
                                lista.remove(actividad);
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}





