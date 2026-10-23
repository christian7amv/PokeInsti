package com.ejemplo.pokeinsti.service;

import android.app.Activity;
import android.nfc.NfcAdapter;
import java.util.function.Consumer;

// Raquel: lee el identificador de la etiqueta, sin escribir nada en ella.
public class LectorNfc {
    private final Activity actividad;
    private final NfcAdapter adaptador;

    public LectorNfc(Activity actividad) {
        this.actividad = actividad;
        adaptador = NfcAdapter.getDefaultAdapter(actividad);
    }

    public boolean disponible() {
        return adaptador != null && adaptador.isEnabled();
    }

    public void iniciar(Consumer<String> alLeer) {
        if (!disponible()) return;
        int opciones = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B
                | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V
                | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        adaptador.enableReaderMode(actividad, etiqueta -> {
            StringBuilder id = new StringBuilder();
            for (byte numero : etiqueta.getId()) {
                id.append(String.format(java.util.Locale.ROOT, "%02X", numero & 0xff));
            }
            if (id.length() > 0) actividad.runOnUiThread(() -> alLeer.accept(id.toString()));
        }, opciones, null);
    }

    public void parar() {
        if (adaptador != null) adaptador.disableReaderMode(actividad);
    }
}