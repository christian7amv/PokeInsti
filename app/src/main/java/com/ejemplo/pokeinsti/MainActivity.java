package com.ejemplo.pokeinsti;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ejemplo.pokeinsti.data.BaseDatos;
import com.ejemplo.pokeinsti.service.LectorNfc;
import com.ejemplo.pokeinsti.service.Localizador;


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
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        estado = findViewById(R.id.estado);
        coleccion = findViewById(R.id.coleccion);
        aviso = findViewById(R.id.aviso);
        nombre = findViewById(R.id.nombre);
        campoRadio = findViewById(R.id.radio);
        registrar = findViewById(R.id.registrar);
        baseDatos = new BaseDatos(this);
        nfc = new LectorNfc(this);
        localizador = new Localizador(this, ubicacion -> {
            posicion = ubicacion;
            actualizarPista();
        });

        radio = getPreferences(MODE_PRIVATE).getInt("radio", 15);
        campoRadio.setText(String.valueOf(radio));
        coleccion.setText(baseDatos.coleccion());
        registrar.setOnClickListener(vista -> prepararRegistro());
        findViewById(R.id.aplicarRadio).setOnClickListener(vista -> cambiarRadio());
        findViewById(R.id.activarUbicacion).setOnClickListener(vista -> pedirUbicacion());
    }

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