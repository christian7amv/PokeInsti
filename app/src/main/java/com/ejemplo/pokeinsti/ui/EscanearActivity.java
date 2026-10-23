package com.ejemplo.pokeinsti.ui;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.ejemplo.pokeinsti.PokeInstiApp;
import com.ejemplo.pokeinsti.R;
import com.ejemplo.pokeinsti.data.PokemonRepository;
import com.ejemplo.pokeinsti.service.NfcHelper;
import com.ejemplo.pokeinsti.util.PokemonImages;

public final class EscanearActivity extends BaseActivity {
    private NfcHelper nfc;
    private boolean reading;
    private TextView status;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state); screen(R.layout.activity_escanear);
        status = findViewById(R.id.scan_status);
        nfc = new NfcHelper(this);
        findViewById(R.id.scan_settings).setOnClickListener(view -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
        findViewById(R.id.scan_collection).setOnClickListener(view -> go(ColeccionActivity.class));
    }
    @Override protected void onResume() {
        super.onResume();
        ((Button) findViewById(R.id.scan_settings)).setEnabled(nfc.available());
        if (!nfc.available()) status.setText(R.string.nfc_no_disponible);
        else if (!nfc.enabled()) status.setText(R.string.nfc_desactivado);
        else { status.setText(R.string.nfc_acercar); nfc.resume(); }
    }
    @Override protected void onPause() { nfc.pause(); super.onPause(); }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (reading) return;
        final String code;
        try { code = nfc.readCode(intent); }
        catch (IllegalArgumentException invalid) { status.setText(R.string.etiqueta_desconocida); return; }
        reading = true;
        status.setText(R.string.guardando);
        PokeInstiApp.IO.execute(() -> {
            try {
                PokemonRepository.Result result = repository().capture(code);
                ui(() -> {
                    reading = false;
                    if (result.status == PokemonRepository.Status.UNKNOWN) {
                        status.setText(R.string.etiqueta_desconocida); return;
                    }
                    int message = result.status == PokemonRepository.Status.CREATED
                            ? R.string.captura_guardada : R.string.ya_capturado;
                    status.setText(getString(message, result.pokemon.nombre));
                    int resource = PokemonImages.resource(result.pokemon.imagen);
                    ((ImageView) findViewById(R.id.scan_image)).setImageResource(resource);
                    findViewById(R.id.scan_image).setContentDescription(result.pokemon.nombre);
                });
            } catch (RuntimeException failure) {
                ui(() -> { reading = false; error(R.id.scan_status); });
            }
        });
    }
}