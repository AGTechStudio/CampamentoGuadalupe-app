package com.example.campamentoguadalupe;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;

public class DetallePagosActivity extends AppCompatActivity {

    TextView txtTitulo, txtResumen, txtCostoCampamento;
    ListView listViewPagos;
    Button btnAgregarPago, btnVolver;

    ArrayList<String> listaPagos = new ArrayList<>();
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    CollectionReference pagosRef;
    String campamento, alumnoId, alumnoNombre;
    double costoCampamento = 0;

    Map<String, Map<String, Object>> pagosMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_pagos);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtResumen = findViewById(R.id.txtResumen);
        txtCostoCampamento = findViewById(R.id.txtCostoCampamento);
        listViewPagos = findViewById(R.id.listViewPagos);
        btnAgregarPago = findViewById(R.id.btnAgregarPago);
        btnVolver = findViewById(R.id.btnVolver);

        campamento = getIntent().getStringExtra("campamento");
        alumnoId = getIntent().getStringExtra("alumnoId");
        alumnoNombre = getIntent().getStringExtra("alumnoNombre");
        costoCampamento = getIntent().getDoubleExtra("costoCampamento", 0);

        txtTitulo.setText("Pagos de " + alumnoNombre);
        txtCostoCampamento.setText("Costo Campamento: $" + costoCampamento);

        db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();
        pagosRef = db.collection("usuarios").document(uid)
                .collection("campamentos").document(campamento)
                .collection("alumnos").document(alumnoId)
                .collection("pagos");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaPagos);
        listViewPagos.setAdapter(adapter);

        cargarPagos();

        btnAgregarPago.setOnClickListener(v -> mostrarDialogoPago(null));

        btnVolver.setOnClickListener(v -> finish());

        listViewPagos.setOnItemClickListener((parent, view, position, id) -> {
            String pagoId = new ArrayList<>(pagosMap.keySet()).get(position);
            mostrarDialogoOpciones(pagoId);
        });
    }

    private void mostrarDialogoOpciones(String pagoId) {
        String[] opciones = {"Editar", "Eliminar"};
        new AlertDialog.Builder(this)
                .setTitle("Opciones de pago")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoPago(pagoId);
                    } else if (which == 1) {
                        eliminarPago(pagoId);
                    }
                }).show();
    }

    private void mostrarDialogoPago(String pagoId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(pagoId == null ? "Agregar Pago" : "Editar Pago");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 10);

        EditText edtMonto = new EditText(this);
        edtMonto.setHint("Monto");
        edtMonto.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        layout.addView(edtMonto);

        EditText edtFecha = new EditText(this);
        edtFecha.setHint("Fecha (dd/MM/yyyy)");
        edtFecha.setFocusable(false);
        layout.addView(edtFecha);

        edtFecha.setOnClickListener(v -> {
            Calendar calendario = Calendar.getInstance();
            DatePickerDialog dpd = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String fecha = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year);
                edtFecha.setText(fecha);
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
            dpd.show();
        });

        if (pagoId != null) {
            Map<String, Object> datos = pagosMap.get(pagoId);
            edtMonto.setText(String.valueOf(datos.get("monto")));
            edtFecha.setText((String) datos.get("fecha"));
        }

        builder.setView(layout);

        builder.setPositiveButton(pagoId == null ? "Agregar" : "Guardar", (dialog, which) -> {
            String montoStr = edtMonto.getText().toString();
            String fecha = edtFecha.getText().toString();

            if (montoStr.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Complete monto y fecha", Toast.LENGTH_SHORT).show();
                return;
            }

            double monto = Double.parseDouble(montoStr);

            Map<String, Object> pago = new HashMap<>();
            pago.put("monto", monto);
            pago.put("fecha", fecha);

            if (pagoId == null) {
                String nuevoId = pagosRef.document().getId();
                pagosRef.document(nuevoId).set(pago)
                        .addOnSuccessListener(aVoid -> cargarPagos());
            } else {
                pagosRef.document(pagoId).set(pago)
                        .addOnSuccessListener(aVoid -> cargarPagos());
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void eliminarPago(String pagoId) {
        pagosRef.document(pagoId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pago eliminado", Toast.LENGTH_SHORT).show();
                    cargarPagos();
                });
    }

    private void cargarPagos() {
        pagosRef.get().addOnSuccessListener(snapshot -> {
            listaPagos.clear();
            pagosMap.clear();
            double total = 0;

            for (var doc : snapshot) {
                String id = doc.getId();
                Double montoObj = doc.getDouble("monto");
                double monto = montoObj != null ? montoObj : 0;
                String fecha = doc.getString("fecha");

                total += monto;
                String info = fecha + " - $" + monto;
                listaPagos.add(info);

                Map<String, Object> datos = new HashMap<>();
                datos.put("monto", monto);
                datos.put("fecha", fecha);
                pagosMap.put(id, datos);
            }

            double saldo = costoCampamento - total;
            if (saldo < 0) saldo = 0;

            txtResumen.setText("Pagado: $" + total + " | Pendiente: $" + saldo);

            adapter.notifyDataSetChanged();
        });
    }
}



