package com.example.campamentoguadalupe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.*;

public class MaterialesActivity extends AppCompatActivity {

    TextView txtTitulo;
    ListView listViewMateriales;
    Button btnAgregar;

    ArrayList<Material> listaMateriales = new ArrayList<>();
    MaterialAdapter adapter;

    FirebaseFirestore db;
    CollectionReference materialesRef;
    String campamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_materiales);

        txtTitulo = findViewById(R.id.txtTitulo);
        listViewMateriales = findViewById(R.id.listViewMateriales);
        btnAgregar = findViewById(R.id.btnAgregar);

        campamento = getIntent().getStringExtra("campamento");
        txtTitulo.setText("Listado de Materiales\n" + campamento);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "Error de autenticación", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        materialesRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("materiales");

        adapter = new MaterialAdapter(this, listaMateriales);
        listViewMateriales.setAdapter(adapter);

        cargarMateriales();

        btnAgregar.setOnClickListener(v -> mostrarDialogoAgregar());

        listViewMateriales.setOnItemClickListener((parent, view, position, id) -> {
            Material material = listaMateriales.get(position);
            eliminarMaterial(material);
        });
    }

    private void mostrarDialogoAgregar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Material");

        final EditText input = new EditText(this);
        input.setHint("Nombre del Material");
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = input.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(this, "Nombre vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = materialesRef.document().getId();
            Material material = new Material(id, nombre, false);

            materialesRef.document(id).set(material)
                    .addOnSuccessListener(unused -> {
                        listaMateriales.add(material);
                        ordenarLista();
                        Toast.makeText(this, "Material agregado", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void cargarMateriales() {
        materialesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            listaMateriales.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                String id = doc.getId();
                String nombre = doc.getString("nombre");
                Boolean completadoObj = doc.getBoolean("completado");

                if (nombre == null) nombre = "Sin nombre";
                boolean completado = completadoObj != null && completadoObj;

                listaMateriales.add(new Material(id, nombre, completado));
            }
            ordenarLista();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al cargar materiales", Toast.LENGTH_SHORT).show()
        );
    }

    private void ordenarLista() {
        Collections.sort(listaMateriales, Comparator.comparing(m -> m.nombre.toLowerCase()));
        adapter.notifyDataSetChanged();
    }

    public void actualizarEstadoMaterial(Material material) {
        materialesRef.document(material.id)
                .update("completado", material.completado)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                );
    }

    private void eliminarMaterial(Material material) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Eliminar " + material.nombre + "?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    materialesRef.document(material.id).delete()
                            .addOnSuccessListener(unused -> {
                                listaMateriales.remove(material);
                                ordenarLista();
                                Toast.makeText(this, "Material eliminado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("No", null)
                .show();
    }
}

