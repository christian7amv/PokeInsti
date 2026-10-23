package com.ejemplo.pokeinsti.service;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Parcelable;
import java.util.Arrays;

public final class NfcHelper {
    private final NfcAdapter adapter;
    private final Activity activity;
    private boolean foregroundEnabled;
    public NfcHelper(Activity activity) {
        this.activity = activity;
        NfcManager manager = (NfcManager) activity.getSystemService(Context.NFC_SERVICE);
        adapter = manager == null ? null : manager.getDefaultAdapter();
    }
    public boolean available() { return adapter != null; }
    public boolean enabled() { return available() && adapter.isEnabled(); }
    public void resume() {
        if (!enabled()) return;
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent pending = PendingIntent.getActivity(activity, 0, intent, flags);
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try { filter.addDataType("text/plain"); }
        catch (IntentFilter.MalformedMimeTypeException error) { throw new IllegalStateException(error); }
        adapter.enableForegroundDispatch(activity, pending, new IntentFilter[]{filter}, null);
        foregroundEnabled = true;
    }
    public void pause() {
        if (foregroundEnabled) adapter.disableForegroundDispatch(activity);
        foregroundEnabled = false;
    }
    @SuppressWarnings("deprecation")
    public String readCode(Intent intent) {
        if (!NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()))
            throw new IllegalArgumentException("No se recibió una etiqueta NDEF");
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (messages != null) for (Parcelable parcel : messages) {
            if (!(parcel instanceof NdefMessage)) continue;
            for (NdefRecord record : ((NdefMessage) parcel).getRecords()) {
                if (record.getTnf() == NdefRecord.TNF_WELL_KNOWN &&
                        Arrays.equals(record.getType(), NdefRecord.RTD_TEXT))
                    return NdefTextDecoder.decode(record.getPayload());
            }
        }
        throw new IllegalArgumentException("La etiqueta no contiene un registro de texto NDEF");
    }
}