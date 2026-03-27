package com.example.campamentoguadalupe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CampamentoActivity extends AppCompatActivity {

    EditText edtCampamento;
    Button btnAgregar;
    ListView listViewCampamentos;

    ArrayList<String> listaCampamentos = new ArrayList<>();
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    CollectionReference campamentosRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campamento);

        edtCampamento = findViewById(R.id.edtCampamento);
        btnAgregar = findViewById(R.id.btnAgregar);
        listViewCampamentos = findViewById(R.id.listViewCampamentos);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        campamentosRef = db.collection("usuarios")
                .document(mAuth.getCurrentUser().getUid())
                .collection("campamentos");

        // Adapter con diseño personalizado
        adapter = new ArrayAdapter<>(this, R.layout.item_campamento, R.id.txtCampamento, listaCampamentos);
        listViewCampamentos.setAdapter(adapter);

        cargarCampamentos();

        btnAgregar.setOnClickListener(v -> {
            String nombre = edtCampamento.getText().toString().trim();

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Ingrese un nombre de campamento", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("nombre", nombre);

            campamentosRef.document(nombre).set(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Campamento agregado", Toast.LENGTH_SHORT).show();
                        edtCampamento.setText("");

                        // Vuelve a cargar desde Firestore para reflejar cambios reales
                        cargarCampamentos();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show());
        });


        // Ir al menú principal del campamento
        listViewCampamentos.setOnItemClickListener((parent, view, position, id) -> {
            String campamentoSeleccionado = listaCampamentos.get(position);
            Intent intent = new Intent(this, MainMenuActivity.class);
            intent.putExtra("campamento", campamentoSeleccionado);
            startActivity(intent);
        });

        // Eliminar campamento (mantener presionado)
        listViewCampamentos.setOnItemLongClickListener((parent, view, position, id) -> {
            String campamentoSeleccionado = listaCampamentos.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Campamento")
                    .setMessage("¿Estás seguro de eliminar \"" + campamentoSeleccionado + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        campamentosRef.document(campamentoSeleccionado).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Campamento eliminado", Toast.LENGTH_SHORT).show();
                                    listaCampamentos.remove(position);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

            return true;
        });
    }

    // Cargar campamentos desde Firestore
    private void cargarCampamentos() {
        campamentosRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            listaCampamentos.clear();
            for (var doc : queryDocumentSnapshots) {
                String nombre = doc.getString("nombre");
                listaCampamentos.add(nombre);
            }
            adapter.notifyDataSetChanged();
        });
    }
}

