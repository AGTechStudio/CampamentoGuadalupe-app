package com.example.campamentoguadalupe;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class CuotasActivity extends AppCompatActivity {

    TextView txtTitulo;
    ListView listViewCuotas;
    Button btnCostoCampamento;

    ArrayList<AlumnoPago> listaAlumnosPagos = new ArrayList<>();
    AlumnoPagoAdapter adapter;

    FirebaseFirestore db;
    DocumentReference campamentoRef;
    CollectionReference alumnosRef;
    String campamento;

    double costoCampamento = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuotas);

        // 👉 Habilita flecha "volver"
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        txtTitulo = findViewById(R.id.txtTitulo);
        listViewCuotas = findViewById(R.id.listViewCuotas);
        btnCostoCampamento = findViewById(R.id.btnCostoCampamento);

        campamento = getIntent().getStringExtra("campamento");
        txtTitulo.setText("Gestión de Cuotas\n" + campamento);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        campamentoRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento);
        alumnosRef = campamentoRef.collection("alumnos");

        adapter = new AlumnoPagoAdapter(this, listaAlumnosPagos);
        listViewCuotas.setAdapter(adapter);

        btnCostoCampamento.setOnClickListener(v -> mostrarDialogoCostoCampamento());

        listViewCuotas.setOnItemClickListener((parent, view, position, id) -> {
            AlumnoPago alumnoPago = listaAlumnosPagos.get(position);
            Intent intent = new Intent(this, DetallePagosActivity.class);
            intent.putExtra("campamento", campamento);
            intent.putExtra("alumnoId", alumnoPago.id);
            intent.putExtra("alumnoNombre", alumnoPago.apellidoNombre);
            intent.putExtra("costoCampamento", costoCampamento);
            startActivity(intent);
        });
    }

    // 👉 Maneja el click en la flecha de volver
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCostoCampamento();
    }

    private void mostrarDialogoCostoCampamento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Costo del Campamento");

        final EditText input = new EditText(this);
        input.setHint("Ej: 100000");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String valor = input.getText().toString();
            if (!valor.isEmpty()) {
                costoCampamento = Double.parseDouble(valor);
                campamentoRef.update("costoCampamento", costoCampamento)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Costo actualizado", Toast.LENGTH_SHORT).show();
                            cargarAlumnosConPagos();
                        });
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void cargarCostoCampamento() {
        campamentoRef.get().addOnSuccessListener(doc -> {
            if (doc.exists() && doc.getDouble("costoCampamento") != null) {
                costoCampamento = doc.getDouble("costoCampamento");
            } else {
                costoCampamento = 0;
            }
            cargarAlumnosConPagos();
        });
    }

    private void cargarAlumnosConPagos() {
        listaAlumnosPagos.clear();
        alumnosRef.get().addOnSuccessListener(alumnosSnap -> {
            if (alumnosSnap.isEmpty()) {
                adapter.notifyDataSetChanged();
                return;
            }

            List<AlumnoPago> tempList = new ArrayList<>();
            final int totalAlumnos = alumnosSnap.size();
            final int[] cargados = {0};

            for (var alumnoDoc : alumnosSnap) {
                String apellido = alumnoDoc.getString("apellido");
                String nombre = alumnoDoc.getString("nombre");
                String docId = alumnoDoc.getId();

                if (apellido == null) apellido = "";
                if (nombre == null) nombre = "";

                String nombreCompleto = apellido + ", " + nombre;

                alumnosRef.document(docId).collection("pagos").get()
                        .addOnSuccessListener(pagosSnap -> {
                            double totalPagado = 0;
                            for (var pagoDoc : pagosSnap) {
                                Double monto = pagoDoc.getDouble("monto");
                                if (monto != null) {
                                    totalPagado += monto;
                                }
                            }
                            double saldo = costoCampamento - totalPagado;
                            if (saldo < 0) saldo = 0;

                            tempList.add(new AlumnoPago(docId, nombreCompleto, totalPagado, saldo));
                            cargados[0]++;

                            if (cargados[0] == totalAlumnos) {
                                Collections.sort(tempList, Comparator.comparing(a -> a.apellidoNombre.toLowerCase()));
                                listaAlumnosPagos.clear();
                                listaAlumnosPagos.addAll(tempList);
                                adapter.notifyDataSetChanged();
                            }
                        });
            }
        });
    }
}



