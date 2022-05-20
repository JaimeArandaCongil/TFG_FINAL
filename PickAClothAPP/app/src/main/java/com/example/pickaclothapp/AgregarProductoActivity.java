package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AgregarProductoActivity extends AppCompatActivity {

    private ImageView imagen_pro;
    private EditText nombre_pro, descripcion_pro, precio_compra_pro, precio_venta_pro, cantidad_pro;
    private TextView texto_agregar;
    private Button boton_agregar_pro;
    private static final int Gallery_Pick= 1;
    //Uri de la imagen
    private Uri imagenUri;
    // Clave aleatoria para identificar al producto y para la uri de descarga
    private String productoRandomKey, downloadUri;
    // Referencia al Storage de Firebase
    private StorageReference productoImagenRef;
    private DatabaseReference productoRef;
    // ProgressDialog deprecado. Buscar alternativas
    private ProgressDialog dialog;
    // En caso de que queramos añadir la categoría seleccionada en una activity previa
    private String categoria, nombre, descripcion, precio, precioCompra, cantidad, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_producto);

        categoria = getIntent().getExtras().get("categoria").toString();

        productoImagenRef = FirebaseStorage.getInstance().getReference().child("Imagenes Productos");
        productoRef = FirebaseDatabase.getInstance().getReference().child("Productos");

        Toast.makeText(this, categoria, Toast.LENGTH_SHORT).show();

        texto_agregar = (TextView) findViewById(R.id.textoAgregar);
        imagen_pro = (ImageView) findViewById(R.id.imagen_pro);
        nombre_pro = (EditText) findViewById(R.id.nombre_pro);
        descripcion_pro = (EditText) findViewById(R.id.descripcion_pro);
        precio_venta_pro = (EditText) findViewById(R.id.precio_pro);
        precio_compra_pro = (EditText) findViewById(R.id.precio_compra_pro);
        cantidad_pro = (EditText) findViewById(R.id.cantidad_pro);
        boton_agregar_pro = (Button) findViewById(R.id.boton_agregar_pro);

        dialog = new ProgressDialog(this);

        // Código para agregar la imagen a la bbdd
        imagen_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                abrirGaleria();
            }
        });

        // Código para agregar el producto
        boton_agregar_pro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validarProducto();
            }
        });

        texto_agregar.setText(categoria + "\n Agregar producto");

    }

    private void abrirGaleria() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/");
        // Deprecado --> buscar alternativas
        startActivityForResult(intent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // data es la imagen
        if (requestCode == Gallery_Pick && resultCode==RESULT_OK && data != null) {
            imagenUri = data.getData();
            imagen_pro.setImageURI(imagenUri);
        }
    }

    private void validarProducto() {

        // Cogemos el texto que ha sido insertado en la caja de texto
        nombre = nombre_pro.getText().toString();
        descripcion = descripcion_pro.getText().toString();
        precio = precio_venta_pro.getText().toString();
        precioCompra = precio_compra_pro.getText().toString();
        cantidad = cantidad_pro.getText().toString();

        if (imagenUri == null) {
            Toast.makeText(this, "Primero agrega una imagen", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(nombre)) {
            // Avisamos de que el usuario introduzca un valor en todos los campos:
            Toast.makeText(this, "Necesario introducir nombre del producto", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(descripcion)) {
            Toast.makeText(this, "Necesario introducir descripcion del producto", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cantidad)) {
            Toast.makeText(this, "Necesario introducir cantidad del producto", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(precio)) {
            Toast.makeText(this, "Necesario introducir precio del producto", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(precioCompra)) {
            Toast.makeText(this, "Necesario introducir precio de compra del producto", Toast.LENGTH_SHORT).show();
        } else {
            // Cuando todos los campos estén rellenos guardará el producto mediante este método
            GuardarInformacionProducto();
        }

    }

    private void GuardarInformacionProducto() {

        // Configuramos un dialog para que se muestre por pantalla mientras se carga el producto
        dialog.setTitle("Guardando producto");
        dialog.setMessage("Por favor espere mientras guardamos el producto");
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        // Declaramos una variable tipo Calendar para poder acceder a los datos de tiempo y fecha de alta (formateados)
        // cuyos valores usaremos para generar la random key, de modo que ningún producto coincida o se solape (distinción)
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("MM-dd-yyyy");
        currentDate = currentDateFormat.format(calendar.getTime());

        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm:ss");
        currentTime = currentTimeFormat.format(calendar.getTime());

        productoRandomKey = currentDate + currentTime;

        // Declaramos el path
        final StorageReference filePath = productoImagenRef.child(imagenUri.getLastPathSegment() + productoRandomKey + ".jpg");
        // Subimos el archivo
        final UploadTask uploadTask = filePath.putFile(imagenUri);

        // Añadimos un listener de eventos para cuando falle la tarea de subir el archivo, tras lo que sacará mensaje de error
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String mensaje = e.toString();
                Toast.makeText(AgregarProductoActivity.this, "Error:" + mensaje, Toast.LENGTH_SHORT).show();
                // En caso de que haya fallo al cargar no se mostrará el mensaje del dialog
                dialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Mensaje que mostrará en caso de que la operación sea exitosa
                Toast.makeText(AgregarProductoActivity.this, "Imagen guardada exitosamente", Toast.LENGTH_SHORT).show();

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // En caso de que la task sea fallida arroja excepción
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        downloadUri = filePath.getDownloadUrl().toString();
                        // Retorna la ubicación donde se guardó la imagen en el Storage
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        // Si la tarea se ha completado con éxito
                        if (task.isSuccessful()) {
                            downloadUri = task.getResult().toString();
                            Toast.makeText(AgregarProductoActivity.this, "Imagen guardada en base de datos", Toast.LENGTH_SHORT).show();
                            guardarEnFirebase();
                        } else {
                            Toast.makeText(AgregarProductoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private void guardarEnFirebase() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("prod_id", productoRandomKey);
        map.put("fecha", currentDate);
        map.put("hora", currentTime);
        map.put("nombre", nombre);
        map.put("descripcion", descripcion);
        map.put("cantidad", cantidad);
        map.put("precio", precio);
        map.put("precioCompra", precioCompra);
        map.put("imagen", downloadUri);
        map.put("categoria", categoria);

        productoRef.child(productoRandomKey).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {


                    // ------------------------------------- CAMBIO FRIDA JUEVVES
                    // Intent intent =  new Intent (AgregarproductoActivity.this, FragmentCategorias.class);

                    // Vídeo 13 min 4:15 cambia la última por AdminActivity.class
                    Intent intent =  new Intent (AgregarProductoActivity.this, AdminActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                    Toast.makeText(AgregarProductoActivity.this, "Subida exitosa!", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    String mensajeError = task.getException().toString();
                    Toast.makeText(AgregarProductoActivity.this, "Error" + mensajeError, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}