package com.example.campamentoguadalupe;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class RendicionActivity extends AppCompatActivity {

    TextView txtTitulo, txtRecaudado, txtSobrante;
    ListView listViewGastos;
    Button btnAgregarGasto, btnVolver;

    ArrayList<String> listaGastos = new ArrayList<>();
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    CollectionReference alumnosRef, gastosRef;
    String campamento;

    double totalRecaudado = 0;
    double totalGastos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rendicion);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtRecaudado = findViewById(R.id.txtRecaudado);
        txtSobrante = findViewById(R.id.txtSobrante);
        listViewGastos = findViewById(R.id.listViewGastos);
        btnAgregarGasto = findViewById(R.id.btnAgregarGasto);
        btnVolver = findViewById(R.id.btnVolver);

        campamento = getIntent().getStringExtra("campamento");
        txtTitulo.setText("Rendición de Gastos\n" + campamento);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();

        alumnosRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("alumnos");

        gastosRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("gastos");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaGastos);
        listViewGastos.setAdapter(adapter);

        btnAgregarGasto.setOnClickListener(v -> mostrarDialogoAgregarGasto());
        btnVolver.setOnClickListener(v -> finish());

        listViewGastos.setOnItemLongClickListener((parent, view, position, id) -> {
            String item = listaGastos.get(position);
            String detalle = item.split(" - ")[0]; // Obtiene solo el detalle
            confirmarEliminarGasto(detalle);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarRendicion();
    }

    private void cargarRendicion() {
        totalRecaudado = 0;
        totalGastos = 0;
        listaGastos.clear();

        alumnosRef.get().addOnSuccessListener(alumnosSnap -> {
            if (!alumnosSnap.isEmpty()) {
                final int totalAlumnos = alumnosSnap.size();
                final int[] cargados = {0};

                for (var alumnoDoc : alumnosSnap) {
                    alumnosRef.document(alumnoDoc.getId()).collection("pagos")
                            .get()
                            .addOnSuccessListener(pagosSnap -> {
                                for (var pago : pagosSnap) {
                                    Double monto = pago.getDouble("monto");
                                    if (monto != null) {
                                        totalRecaudado += monto;
                                    }
                                }
                                cargados[0]++;
                                if (cargados[0] == totalAlumnos) {
                                    cargarGastos();
                                }
                            });
                }
            } else {
                cargarGastos();
            }
        });
    }

    private void cargarGastos() {
        gastosRef.get().addOnSuccessListener(gastosSnap -> {
            for (var gasto : gastosSnap) {
                String id = gasto.getId();
                String detalle = gasto.getString("detalle");
                Double monto = gasto.getDouble("monto");

                totalGastos += (monto != null ? monto : 0);
                listaGastos.add(detalle + " - $" + monto);
            }

            double sobrante = totalRecaudado - totalGastos;
            if (sobrante < 0) sobrante = 0;

            txtRecaudado.setText("Total recaudado: $" + totalRecaudado);
            txtSobrante.setText("Sobrante: $" + sobrante);

            adapter.notifyDataSetChanged();
        });
    }

    private void mostrarDialogoAgregarGasto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Gasto");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText edtDetalle = new EditText(this);
        edtDetalle.setHint("Detalle");
        layout.addView(edtDetalle);

        EditText edtMonto = new EditText(this);
        edtMonto.setHint("Monto");
        edtMonto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(edtMonto);

        builder.setView(layout);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String detalle = edtDetalle.getText().toString().trim();
            String montoStr = edtMonto.getText().toString().trim();

            if (detalle.isEmpty() || montoStr.isEmpty()) {
                Toast.makeText(this, "Complete los datos", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto = Double.parseDouble(montoStr);

            Map<String, Object> gasto = new HashMap<>();
            gasto.put("detalle", detalle);
            gasto.put("monto", monto);

            gastosRef.document(detalle).set(gasto)
                    .addOnSuccessListener(unused -> cargarRendicion());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void confirmarEliminarGasto(String detalle) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Gasto")
                .setMessage("¿Desea eliminar el gasto: " + detalle + "?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarGasto(detalle))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarGasto(String detalle) {
        gastosRef.document(detalle).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Gasto eliminado", Toast.LENGTH_SHORT).show();
                    cargarRendicion();
                });
    }
}

