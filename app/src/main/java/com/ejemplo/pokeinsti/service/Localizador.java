package com.ejemplo.pokeinsti.service;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import java.util.function.Consumer;

public class Localizador implements LocationListener {
    private final Context contexto;
    private final LocationManager gestor;
    private final Consumer<Location> alRecibir;

    public Localizador(Context contexto, Consumer<Location> alRecibir) {
        this.contexto = contexto;
        this.alRecibir = alRecibir;
        gestor = (LocationManager) contexto.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean tienePermiso() {
        return contexto.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public boolean iniciar() {
        parar();
        if (!tienePermiso() || gestor == null) return false;
        boolean iniciado = false;
        try {
            for (String proveedor : gestor.getProviders(true)) {
                if (proveedor.equals(LocationManager.GPS_PROVIDER)
                        || proveedor.equals(LocationManager.NETWORK_PROVIDER)) {
                    gestor.requestLocationUpdates(proveedor, 3000, 0, this);
                    iniciado = true;
                }
            }
        } catch (SecurityException error) {
            parar();
            return false;
        }
        return iniciado;
    }

    public void parar() {
        if (gestor != null) gestor.removeUpdates(this);
    }

    public static boolean esReciente(Location ubicacion) {
        if (ubicacion == null) return false;
        long edad = SystemClock.elapsedRealtimeNanos() - ubicacion.getElapsedRealtimeNanos();
        return edad >= 0 && edad <= 30_000_000_000L;
    }

    public static float distancia(Location actual, double latitud, double longitud) {
        float[] resultado = new float[1];
        Location.distanceBetween(actual.getLatitude(), actual.getLongitude(),
                latitud, longitud, resultado);
        return resultado[0];
    }

    @Override public void onLocationChanged(Location ubicacion) { alRecibir.accept(ubicacion); }
    @Override public void onProviderDisabled(String proveedor) { alRecibir.accept(null); }
    @Override public void onProviderEnabled(String proveedor) { iniciar(); }
    @Override public void onStatusChanged(String proveedor, int estado, Bundle datos) { }
}
