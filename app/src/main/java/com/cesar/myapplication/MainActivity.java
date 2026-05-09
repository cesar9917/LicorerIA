package com.cesar.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText etId, etNombre, etPrecio, etStock;
    Button btnInsertar, btnListar, btnActualizar, btnEliminar;
    TextView tvResultado, tvAlertas;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etId       = findViewById(R.id.etId);
        etNombre   = findViewById(R.id.etNombre);
        etPrecio   = findViewById(R.id.etPrecio);
        etStock    = findViewById(R.id.etStock);

        btnInsertar   = findViewById(R.id.btnInsertar);
        btnListar     = findViewById(R.id.btnListar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnEliminar   = findViewById(R.id.btnEliminar);

        tvResultado = findViewById(R.id.tvResultado);
        tvAlertas   = findViewById(R.id.tvAlertas);

        dbHelper = new DBHelper(this);

        btnInsertar.setOnClickListener(v -> insertarProducto());
        btnListar.setOnClickListener(v -> listarProductos());
        btnActualizar.setOnClickListener(v -> actualizarProducto());
        btnEliminar.setOnClickListener(v -> eliminarProducto());
    }

    private void insertarProducto() {
        String nombre      = etNombre.getText().toString();
        String precioTexto = etPrecio.getText().toString();
        String stockTexto  = etStock.getText().toString();

        if (nombre.isEmpty() || precioTexto.isEmpty() || stockTexto.isEmpty()) {
            Toast.makeText(this, "Ingrese nombre, precio y stock", Toast.LENGTH_SHORT).show();
            return;
        }

        double precio = Double.parseDouble(precioTexto);
        int stock     = Integer.parseInt(stockTexto);

        long resultado = dbHelper.insertarProducto(nombre, precio, stock);

        if (resultado > 0) {
            Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            listarProductos();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    private void listarProductos() {
        Cursor cursor = dbHelper.obtenerProductos();

        tvAlertas.setText("");

        if (cursor.getCount() == 0) {
            tvResultado.setText("No hay productos registrados");
            cursor.close();
            return;
        }

        StringBuilder datos   = new StringBuilder();
        StringBuilder alertas = new StringBuilder();

        while (cursor.moveToNext()) {

            int id        = cursor.getInt(0);
            String nombre = cursor.getString(1);
            double precio = cursor.getDouble(2);
            int stock     = cursor.getInt(3);

            datos.append("ID: ").append(id)
                    .append(" | ").append(nombre)
                    .append(" | S/ ").append(precio)
                    .append(" | Stock: ").append(stock)
                    .append("\n");

            if (stock <= 10) {
                alertas.append("⚠ STOCK BAJO: ")
                        .append(nombre)
                        .append(" (")
                        .append(stock)
                        .append(" unidades)\n");
            }
        }

        tvResultado.setText(datos.toString());

        if (alertas.length() > 0) {
            tvAlertas.setText(alertas.toString());
        }

        cursor.close();
    }

    private void actualizarProducto() {

        String idTexto     = etId.getText().toString();
        String nombre      = etNombre.getText().toString();
        String precioTexto = etPrecio.getText().toString();
        String stockTexto  = etStock.getText().toString();

        if (idTexto.isEmpty() || nombre.isEmpty()
                || precioTexto.isEmpty() || stockTexto.isEmpty()) {

            Toast.makeText(this,
                    "Ingrese ID, nombre, precio y stock",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        int id        = Integer.parseInt(idTexto);
        double precio = Double.parseDouble(precioTexto);
        int stock     = Integer.parseInt(stockTexto);

        int resultado = dbHelper.actualizarProducto(id, nombre, precio, stock);

        if (resultado > 0) {

            Toast.makeText(this,
                    "Producto actualizado",
                    Toast.LENGTH_SHORT).show();

            limpiarCampos();
            listarProductos();

        } else {

            Toast.makeText(this,
                    "Producto no encontrado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarProducto() {

        String idTexto = etId.getText().toString();

        if (idTexto.isEmpty()) {

            Toast.makeText(this,
                    "Ingrese el ID",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        int id = Integer.parseInt(idTexto);

        int resultado = dbHelper.borrarProducto(id);

        if (resultado > 0) {

            Toast.makeText(this,
                    "Producto eliminado",
                    Toast.LENGTH_SHORT).show();

            limpiarCampos();
            listarProductos();

        } else {

            Toast.makeText(this,
                    "Producto no encontrado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos() {

        etId.setText("");
        etNombre.setText("");
        etPrecio.setText("");
        etStock.setText("");
    }
}