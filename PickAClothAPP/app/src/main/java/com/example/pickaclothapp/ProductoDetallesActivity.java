package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.pickaclothapp.Modal.Productos;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductoDetallesActivity extends AppCompatActivity {

    private Button agregarCarrito;
    private ElegantNumberButton numeroBoton;
    private ImageView productoImagen;
    private TextView productoPrecio, productoDescripcion, productoNombre;
    private String productoID = "", estado = "Normal", currentUserID;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalles);

        productoID = getIntent().getStringExtra("pid");
        agregarCarrito = (Button) findViewById(R.id.boton_siguiente_detalles);
        numeroBoton=(ElegantNumberButton) findViewById(R.id.numero_boton);
        productoImagen = (ImageView) findViewById(R.id.producto_imagen_detalles);
        productoPrecio=(TextView) findViewById(R.id.producto_precio_detalles);
        productoDescripcion=(TextView) findViewById(R.id.producto_descripcion_detalles);
        productoNombre=(TextView) findViewById(R.id.producto_nombre_detalles);

        obtenerDatosProducto(productoID);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        agregarCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (estado.equals("Pedido") || estado.equals("Enviado")) {
                    Toast.makeText(ProductoDetallesActivity.this, "Esperando a que el primer pedido finalice", Toast.LENGTH_SHORT).show();
                } else {
                    agregarALaLista();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        VerificarEstadoOrden();
    }

    private void agregarALaLista() {

        String currentTime, currentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat data = new SimpleDateFormat("MM-dd-yyyy");
        currentDate = data.format(calendar.getTime());

        SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
        currentTime = time.format(calendar.getTime());

        final DatabaseReference CartListRef = FirebaseDatabase.getInstance().getReference().child("Carrito");

        final HashMap<String, Object> map = new HashMap<>();
        map.put("pid", productoID);
        map.put("nombre", productoNombre.getText().toString());
        map.put("precio", productoPrecio.getText().toString());
        map.put("fecha", currentDate);
        map.put("hora", currentTime);
        map.put("cantidad", numeroBoton.getNumber());
        map.put("descuento", "");

        CartListRef.child("Usuario compra").child(currentUserID).child("Productos").child(productoID).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    CartListRef.child("Administración").child(currentUserID).child("Productos").child(productoID).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProductoDetallesActivity.this, "Agregado", Toast.LENGTH_SHORT).show();

                            // Cuando termine la operación pasará otra vez a la activity principal para seguir agregando producto
                            Intent intent = new Intent(ProductoDetallesActivity.this, PrincipalActivity.class);
                            startActivity(intent);
                        }
                    });
                }
            }
        });

    }

    // Obtiene cada uno de los datos de los productos de la bbdd y los pinta o asigna en las variables correspondientes para la app
    private void obtenerDatosProducto(String productoID) {

        DatabaseReference productoRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Productos");
        productoRef.child(productoID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Productos productos = snapshot.getValue(Productos.class);
                    productoNombre.setText(productos.getNombre());
                    productoDescripcion.setText(productos.getDescripcion());
                    productoPrecio.setText(productos.getPrecio());
                    Picasso.get().load(productos.getImagen()).into(productoImagen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void VerificarEstadoOrden() {
        DatabaseReference ordenRef;
        ordenRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Ordenes").child(currentUserID);
        ordenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String envioEstado = snapshot.child("estado").getValue().toString();
                    if (envioEstado.equals("Enviado")) {
                        estado = "Enviado";
                    } else if (envioEstado.equals("No enviado")) {
                        estado = "Pedido";
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}