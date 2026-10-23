package com.ejemplo.pokeinsti;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private TextView estado, coleccion, aviso;
    private EditText nombre, campoRadio;
    private Button registrar;
    private BaseDatos baseDatos;
    private LectorNfc nfc;
    private Localizador localizador;
    private Location posicion;
    private int radio = 15;
    private String nombrePendiente = "";
    private String etiquetaCaliente = "";
    private final Handler reloj = new Handler(Looper.getMainLooper());
    private final Runnable actualizar = new Runnable() {
        @Override public void run() {
            actualizarPista();
            reloj.postDelayed(this, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}