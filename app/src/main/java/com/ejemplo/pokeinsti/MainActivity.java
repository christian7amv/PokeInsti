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
            return insets;//a
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

    private void pedirUbicacion() {
        if (!localizador.tienePermiso()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else if (localizador.iniciar()) {
            estado.setText("Esperando ubicación...");
        } else {
            estado.setText("Activa la ubicación del móvil y vuelve a pulsar el botón.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int codigo, String[] permisos, int[] resultados) {
        super.onRequestPermissionsResult(codigo, permisos, resultados);
        if (codigo == 1) {
            if (localizador.tienePermiso()) pedirUbicacion();
            else estado.setText("Activa el permiso de ubicación precisa para jugar.");
        }
    }

    private void cambiarRadio() {
        try {
            int nuevo = Integer.parseInt(campoRadio.getText().toString());
            if (nuevo < 1 || nuevo > 500) throw new NumberFormatException();
            radio = nuevo;
            getPreferences(MODE_PRIVATE).edit().putInt("radio", radio).apply();
            etiquetaCaliente = "";
            actualizarPista();
        } catch (NumberFormatException error) {
            mensaje("Escribe un radio entre 1 y 500 metros.");
        }
    }

    private boolean ubicacionUtil() {
        return localizador.tienePermiso() && Localizador.esReciente(posicion)
                && posicion.hasAccuracy() && posicion.getAccuracy() <= radio;
    }

    private void prepararRegistro() {
        if (!nombrePendiente.isEmpty()) {
            nombrePendiente = "";
            registrar.setText(R.string.registrar);
            aviso.setText("Registro cancelado. Puedes seguir buscando.");
            return;
        }
        if (!nfc.disponible()) { mensaje("Necesitas NFC activado."); return; }
        if (!ubicacionUtil()) { mensaje("Espera una ubicación reciente y precisa."); return; }
        nombrePendiente = nombre.getText().toString().trim();
        if (nombrePendiente.isEmpty()) { mensaje("Escribe el nombre del Pokémon."); return; }
        registrar.setText(R.string.cancelar);
        aviso.setText("Acerca la etiqueta al móvil en el lugar donde vas a colocarla.");
    }

    private void leerEtiqueta(String id) {
        if (!nombrePendiente.isEmpty()) {
            if (!ubicacionUtil()) { mensaje("La ubicación no es precisa. Vuelve a intentarlo."); return; }
            boolean guardada = baseDatos.guardar(id, nombrePendiente,
                    posicion.getLatitude(), posicion.getLongitude());
            aviso.setText(guardada ? "Etiqueta registrada. Ya puedes buscarla."
                    : "Esta etiqueta ya estaba registrada.");
            nombrePendiente = "";
            registrar.setText(R.string.registrar);
        } else {
            aviso.setText(baseDatos.capturar(id));
            coleccion.setText(baseDatos.coleccion());
        }
        actualizarPista();
    }

    private void actualizarPista() {
        String cercana = "";
        float menor = Float.MAX_VALUE;
        try (Cursor etiquetas = baseDatos.pendientes()) {
            if (etiquetas.getCount() == 0) {
                estado.setText("No quedan etiquetas por encontrar. Registra alguna para empezar.");
                etiquetaCaliente = "";
                return;
            }
            if (!ubicacionUtil()) {
                estado.setText("Buscando ubicación precisa... Comprueba el permiso y la ubicación del móvil.");
                etiquetaCaliente = "";
                return;
            }
            while (etiquetas.moveToNext()) {
                float distancia = Localizador.distancia(posicion, etiquetas.getDouble(1), etiquetas.getDouble(2));
                if (distancia < menor) {
                    menor = distancia;
                    cercana = etiquetas.getString(0);
                }
            }
        }
        estado.setText(menor <= radio ? "¡CALIENTE! Hay una etiqueta cerca." : "FRÍO. Sigue buscando.");
        if (menor <= radio) {
            if (!cercana.equals(etiquetaCaliente)) {
                Vibrator vibrador = getSystemService(Vibrator.class);
                if (vibrador != null && vibrador.hasVibrator()) {
                    vibrador.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
            etiquetaCaliente = cercana;
        } else {
            etiquetaCaliente = "";
        }
    }

    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }

    @Override protected void onResume() {
        super.onResume();
        nfc.iniciar(this::leerEtiqueta);
        if (!nfc.disponible()) aviso.setText("Activa NFC en un móvil compatible para leer etiquetas.");
        if (localizador.tienePermiso()) localizador.iniciar();
        reloj.post(actualizar);
    }

    @Override protected void onPause() {
        reloj.removeCallbacks(actualizar);
        localizador.parar();
        posicion = null;
        nfc.parar();
        super.onPause();
    }

    @Override protected void onDestroy() {
        baseDatos.close();
        super.onDestroy();
    }
}


