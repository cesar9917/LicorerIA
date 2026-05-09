package com.cesar.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "licoreria.db";
    private static final int DB_VERSION = 1;
    public static final String TABLE_PRODUCTOS = "productos";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_PRODUCTOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT, " +
                "precio REAL, " +
                "stock INTEGER)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTOS);
        onCreate(db);
    }

    public long insertarProducto(String nombre, double precio, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("precio", precio);
        values.put("stock", stock);
        return db.insert(TABLE_PRODUCTOS, null, values);
    }

    public Cursor obtenerProductos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_PRODUCTOS, null);
    }

    public int actualizarProducto(int id, String nombre, double precio, int stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("precio", precio);
        values.put("stock", stock);
        return db.update(TABLE_PRODUCTOS, values, "id=?", new String[]{String.valueOf(id)});
    }

    public int borrarProducto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PRODUCTOS, "id=?", new String[]{String.valueOf(id)});
    }

    public void borrarTodo() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PRODUCTOS, null, null);
    }
}