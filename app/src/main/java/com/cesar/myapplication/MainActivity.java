package com.cesar.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    EditText etId, etNombre, etPrecio, etStock;
    Button btnInsertar, btnListar, btnActualizar, btnEliminar, btnWebService;
    TextView tvResultado, tvAlertas;
    DBHelper dbHelper;

    String urlApi = "https://fakestoreapi.com/products";

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
        btnWebService = findViewById(R.id.btnWebService);
        tvResultado   = findViewById(R.id.tvResultado);
        tvAlertas     = findViewById(R.id.tvAlertas);

        dbHelper = new DBHelper(this);

        btnInsertar.setOnClickListener(v -> insertarProducto());
        btnListar.setOnClickListener(v -> listarProductos());
        btnActualizar.setOnClickListener(v -> actualizarProducto());
        btnEliminar.setOnClickListener(v -> eliminarProducto());
        btnWebService.setOnClickListener(v -> cargarDesdeWebService());
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

        StringBuilder datos    = new StringBuilder();
        StringBuilder alertas  = new StringBuilder();

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
                alertas.append("⚠ STOCK BAJO: ").append(nombre)
                        .append(" (").append(stock).append(" unidades)\n");
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

        if (idTexto.isEmpty() || nombre.isEmpty() || precioTexto.isEmpty() || stockTexto.isEmpty()) {
            Toast.makeText(this, "Ingrese ID, nombre, precio y stock", Toast.LENGTH_SHORT).show();
            return;
        }

        int id        = Integer.parseInt(idTexto);
        double precio = Double.parseDouble(precioTexto);
        int stock     = Integer.parseInt(stockTexto);
        int resultado = dbHelper.actualizarProducto(id, nombre, precio, stock);

        if (resultado > 0) {
            Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            listarProductos();
        } else {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void eliminarProducto() {
        String idTexto = etId.getText().toString();

        if (idTexto.isEmpty()) {
            Toast.makeText(this, "Ingrese el ID", Toast.LENGTH_SHORT).show();
            return;
        }

        int id        = Integer.parseInt(idTexto);
        int resultado = dbHelper.borrarProducto(id);

        if (resultado > 0) {
            Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
            limpiarCampos();
            listarProductos();
        } else {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void cargarDesdeWebService() {
        tvResultado.setText("Conectando con WebService...");
        tvAlertas.setText("");

        new Thread(() -> {
            try {
                URL url = new URL(urlApi);
                HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("GET");
                conexion.setConnectTimeout(10000);
                conexion.setReadTimeout(10000);

                BufferedReader lector = new BufferedReader(
                        new InputStreamReader(conexion.getInputStream())
                );

                StringBuilder respuesta = new StringBuilder();
                String linea;
                while ((linea = lector.readLine()) != null) {
                    respuesta.append(linea);
                }
                lector.close();

                JSONArray arreglo = new JSONArray(respuesta.toString());
                dbHelper.borrarTodo();

                for (int i = 0; i < arreglo.length(); i++) {
                    JSONObject producto = arreglo.getJSONObject(i);
                    String nombre = producto.getString("title");
                    double precio = producto.getDouble("price");
                    int stock     = 20; // stock inicial por defecto al importar
                    dbHelper.insertarProducto(nombre, precio, stock);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Productos cargados desde WebService", Toast.LENGTH_SHORT).show();
                    listarProductos();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvResultado.setText("Error al conectar: " + e.getMessage());
                    Toast.makeText(this, "No se pudo cargar", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void limpiarCampos() {
        etId.setText("");
        etNombre.setText("");
        etPrecio.setText("");
        etStock.setText("");
    }
}