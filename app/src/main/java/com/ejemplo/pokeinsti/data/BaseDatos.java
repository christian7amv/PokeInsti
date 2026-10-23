package com.ejemplo.pokeinsti.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDatos extends SQLiteOpenHelper {
    public BaseDatos(Context contexto) {
        super(contexto, "pokeinsti_simple.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE etiquetas (id TEXT PRIMARY KEY, pokemon TEXT NOT NULL, "
                + "latitud REAL NOT NULL, longitud REAL NOT NULL, capturada INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int anterior, int nueva) {
        // La primera versión todavía no necesita cambios de la tabla.
    }

    public boolean guardar(String id, String pokemon, double latitud, double longitud) {
        ContentValues datos = new ContentValues();
        datos.put("id", id);
        datos.put("pokemon", pokemon);
        datos.put("latitud", latitud);
        datos.put("longitud", longitud);
        return getWritableDatabase().insertWithOnConflict("etiquetas", null, datos,
                SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public Cursor pendientes() {
        return getReadableDatabase().rawQuery(
                "SELECT id, latitud, longitud FROM etiquetas WHERE capturada = 0", null);
    }

    public String capturar(String id) {
        SQLiteDatabase db = getWritableDatabase();
        try (Cursor fila = db.rawQuery(
                "SELECT pokemon, capturada FROM etiquetas WHERE id = ?", new String[]{id})) {
            if (!fila.moveToFirst()) return "Esta etiqueta no está registrada.";
            if (fila.getInt(1) == 1) return "Ya habías capturado este Pokémon.";
            ContentValues datos = new ContentValues();
            datos.put("capturada", 1);
            db.update("etiquetas", datos, "id = ?", new String[]{id});
            return "¡Has capturado a " + fila.getString(0) + "!";
        }
    }

    public String coleccion() {
        StringBuilder lista = new StringBuilder();
        try (Cursor filas = getReadableDatabase().rawQuery(
                "SELECT pokemon FROM etiquetas WHERE capturada = 1 ORDER BY pokemon", null)) {
            while (filas.moveToNext()) lista.append("• ").append(filas.getString(0)).append("\n");
        }
        return lista.length() == 0 ? "Todavía no has capturado ninguno." : lista.toString();
    }
}