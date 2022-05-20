package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ConfirmarOrdenActivity extends AppCompatActivity {

    private EditText nombre, correo, direccion, telefono;
    private Button confirmar;
    private String totalPago = "";
    private FirebaseAuth auth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_orden);

        totalPago = getIntent().getStringExtra("Total");
        Toast.makeText(this, "Total a pagar: " + totalPago + "€", Toast.LENGTH_SHORT).show();

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        nombre = (EditText) findViewById(R.id.final_nombre);
        correo = (EditText) findViewById(R.id.final_correo);
        direccion = (EditText) findViewById(R.id.final_direccion);
        telefono = (EditText) findViewById(R.id.final_telefono);

        confirmar = (Button) findViewById(R.id.final_boton_confirmar);

        confirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarDatos();

            }
        });
        
    }

    private void verificarDatos() {

        if (TextUtils.isEmpty(nombre.getText().toString())) {
            Toast.makeText(this, "Por favor ingrese su nombre", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(direccion.getText().toString())) {
            Toast.makeText(this, "Por favor ingrese su dirección", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(correo.getText().toString())) {
            Toast.makeText(this, "Por favor ingrese su correo", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(telefono.getText().toString())) {
            Toast.makeText(this, "Por favor ingrese su teléfono", Toast.LENGTH_SHORT).show();
        } else {
            confirmarOrden();
        }
    }

    private void confirmarOrden() {

        final String currentTime, currentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        currentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat dateFormat1= new SimpleDateFormat("HH:mm:ss");
        currentTime = dateFormat1.format(calendar.getTime());

        final DatabaseReference ordenesRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Ordenes").child(currentUserID);

        HashMap<String, Object> map = new HashMap<>();
        map.put("total", totalPago);
        map.put("nombre", nombre.getText().toString());
        map.put("direccion", direccion.getText().toString());
        map.put("telefono", telefono.getText().toString());
        map.put("correo", correo.getText().toString());
        map.put("fecha", currentDate);
        map.put("hora", currentTime);
        map.put("estado", "No enviado");

        // Subirlo a la base de datos
        ordenesRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Carrito")
                            .child("Usuario compra").child(currentUserID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ConfirmarOrdenActivity.this, "Tu orden fue procesada con éxito!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ConfirmarOrdenActivity.this, PrincipalActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }
            }
        });
    }
}