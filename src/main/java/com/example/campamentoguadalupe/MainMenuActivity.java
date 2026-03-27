package com.example.campamentoguadalupe;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainMenuActivity extends AppCompatActivity {

    TextView txtTitulo, txtPronosticoTitulo, txtUbicacionActual;
    LinearLayout layoutPronostico;
    Button btnAlumnos, btnCuotas, btnMateriales, btnActividades, btnRendicion, btnCerrarSesion;
    String campamentoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        txtTitulo = findViewById(R.id.txtTitulo);
        txtPronosticoTitulo = findViewById(R.id.txtPronosticoTitulo);
        txtUbicacionActual = findViewById(R.id.txtUbicacionActual);
        layoutPronostico = findViewById(R.id.layoutPronostico);

        btnAlumnos = findViewById(R.id.btnAlumnos);
        btnCuotas = findViewById(R.id.btnCuotas);
        btnMateriales = findViewById(R.id.btnMateriales);
        btnActividades = findViewById(R.id.btnActividades);
        btnRendicion = findViewById(R.id.btnRendicion);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion);

        campamentoSeleccionado = getIntent().getStringExtra("campamento");
        txtTitulo.setText(campamentoSeleccionado);

        obtenerPronosticoExtendido();

        btnAlumnos.setOnClickListener(v -> abrir(AlumnosActivity.class));
        btnCuotas.setOnClickListener(v -> abrir(CuotasActivity.class));
        btnMateriales.setOnClickListener(v -> abrir(MaterialesActivity.class));
        btnActividades.setOnClickListener(v -> abrir(ActividadesActivity.class));
        btnRendicion.setOnClickListener(v -> abrir(RendicionActivity.class));

        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void abrir(Class<?> clase) {
        Intent intent = new Intent(this, clase);
        intent.putExtra("campamento", campamentoSeleccionado);
        startActivity(intent);
    }

    private void obtenerPronosticoExtendido() {
        String apiKey = "90275dad5f0d74503132ed06d3d3ec0f"; // 🔒 Reemplazá con tu clave de OpenWeather
        String ciudad = "Santa Fe,AR";
        String url = "https://api.openweathermap.org/data/2.5/forecast?q=" + ciudad + "&appid=" + apiKey + "&units=metric&lang=es";

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray list = jsonObject.getJSONArray("list");

                        layoutPronostico.removeAllViews();

                        int diasMostrados = 0;
                        String ultimoDia = "";

                        for (int i = 0; i < list.length() && diasMostrados < 3; i++) {
                            JSONObject obj = list.getJSONObject(i);
                            String fechaHora = obj.getString("dt_txt");

                            if (fechaHora.contains("12:00:00")) {
                                JSONObject main = obj.getJSONObject("main");
                                JSONArray weatherArray = obj.getJSONArray("weather");
                                JSONObject weather = weatherArray.getJSONObject(0);

                                double temp = main.getDouble("temp");
                                String descripcion = weather.getString("description");
                                String iconCode = weather.getString("icon");
                                String fecha = fechaHora.substring(5, 10); // mm-dd

                                if (!fecha.equals(ultimoDia)) {
                                    ultimoDia = fecha;
                                    diasMostrados++;

                                    // Card
                                    LinearLayout card = new LinearLayout(this);
                                    card.setOrientation(LinearLayout.VERTICAL);
                                    card.setPadding(dp(12), dp(12), dp(12), dp(12));
                                    card.setBackgroundResource(R.drawable.card_background);
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(100), LinearLayout.LayoutParams.WRAP_CONTENT);
                                    params.setMargins(dp(8), dp(4), dp(8), dp(4));
                                    card.setLayoutParams(params);
                                    card.setGravity(Gravity.CENTER_HORIZONTAL);

                                    // Fecha
                                    TextView txtFecha = new TextView(this);
                                    txtFecha.setText(fecha);
                                    txtFecha.setTextSize(14);
                                    txtFecha.setTextColor(0xFF333333);
                                    txtFecha.setGravity(Gravity.CENTER);

                                    // Icono local
                                    ImageView icono = new ImageView(this);
                                    icono.setLayoutParams(new LinearLayout.LayoutParams(dp(64), dp(64)));
                                    icono.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    int iconResId = getIconResource(iconCode);
                                    icono.setImageResource(iconResId);

                                    // Descripción
                                    TextView txtDesc = new TextView(this);
                                    txtDesc.setText(descripcion);
                                    txtDesc.setTextSize(14);
                                    txtDesc.setTextColor(0xFF4CAF50);
                                    txtDesc.setGravity(Gravity.CENTER);
                                    txtDesc.setMaxLines(1);
                                    txtDesc.setEllipsize(TextUtils.TruncateAt.END);

                                    // Temperatura
                                    TextView txtTemp = new TextView(this);
                                    txtTemp.setText(Math.round(temp) + "°C");
                                    txtTemp.setTextSize(16);
                                    txtTemp.setTextColor(0xFF006600);
                                    txtTemp.setGravity(Gravity.CENTER);

                                    card.addView(txtFecha);
                                    card.addView(icono);
                                    card.addView(txtDesc);
                                    card.addView(txtTemp);

                                    layoutPronostico.addView(card);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> error.printStackTrace());

        queue.add(stringRequest);
    }

    private int dp(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int getIconResource(String iconCode) {
        switch (iconCode) {
            case "01d":
            case "01n": return R.drawable.ic_sunny;
            case "02d":
            case "02n": return R.drawable.ic_partly_cloudy;
            case "03d":
            case "03n": return R.drawable.ic_cloudy;
            case "04d":
            case "04n": return R.drawable.ic_cloudy;
            case "09d":
            case "09n": return R.drawable.ic_rain;
            case "10d":
            case "10n": return R.drawable.ic_rain;
            case "11d":
            case "11n": return R.drawable.ic_storm;
            case "13d":
            case "13n": return R.drawable.ic_snow;
            case "50d":
            case "50n": return R.drawable.ic_fog;
            default: return R.drawable.ic_cloudy;
        }
    }
}




