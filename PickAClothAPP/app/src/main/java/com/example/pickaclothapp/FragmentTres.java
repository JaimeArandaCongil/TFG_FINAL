package com.example.pickaclothapp;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentTres extends Fragment {

    private View fragmento;
    private EditText nombre, ciudad, direccion, telefn;
    private Button guardar;
    private CircleImageView imagen;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private ProgressDialog dialog;
    private String currentUserId;
    private static int Galery_Pick = 1;
    private StorageReference userImagenPerfil;

    public FragmentTres() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmento = inflater.inflate(R.layout.fragment_tres, container, false);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Admin");
        dialog = new ProgressDialog(getContext());

        //URL DEL STORAGE?
        userImagenPerfil = FirebaseStorage.getInstance().getReference().child("Perfil");
        nombre=(EditText) fragmento.findViewById(R.id.perfila_nombre);
        ciudad=(EditText) fragmento.findViewById(R.id.perfila_ciudad);
        direccion=(EditText) fragmento.findViewById(R.id.perfila_direccion);
        telefn=(EditText) fragmento.findViewById(R.id.perfila_telefono);
        guardar=(Button) fragmento.findViewById(R.id.perfila_boton);
        imagen=(CircleImageView) fragmento.findViewById(R.id.perfila_imagen);

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("imagen")){

                    String nombres = snapshot.child("nombre").getValue().toString();
                    String direccions = snapshot.child("dirección").getValue().toString();
                    String ciudads = snapshot.child("ciudad").getValue().toString();
                    String telefonos = snapshot.child("telefono").getValue().toString();
                    String imagens = snapshot.child("imagen").getValue().toString();

                    Picasso.get()
                            .load(imagens)
                            .placeholder(R.drawable.logocami)
                            .into(imagen);
                    nombre.setText(nombres);
                    direccion.setText(direccions);
                    ciudad.setText(ciudads);
                    telefn.setText(telefonos);
                }else if(snapshot.exists()){
                    String nombres = snapshot.child("nombre").getValue().toString();
                    String direccions = snapshot.child("dirección").getValue().toString();
                    String ciudads = snapshot.child("ciudad").getValue().toString();
                    String telefonos = snapshot.child("telefono").getValue().toString();
                    nombre.setText(nombres);
                    direccion.setText(direccions);
                    ciudad.setText(ciudads);
                    telefn.setText(telefonos);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}});


        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                guardarInformacion();
            }
        });

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/");
                startActivityForResult(intent, Galery_Pick);

            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    if(snapshot.hasChild("imagen")){
                        String imagenstr = snapshot.child("imagen").getValue().toString();
                        Picasso.get().load(imagenstr).placeholder(R.drawable.logocami).into(imagen);
                    }else{
                        Toast.makeText(getContext(),"Seleccione una imagen de perfil", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return fragmento;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Galery_Pick && resultCode==RESULT_OK && data != null){
            Uri imagenUri = data.getData();

            CropImage.activity(imagenUri).setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1).start(getActivity());

        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                dialog.setTitle("Imagen de perfil");
                dialog.setMessage("Espere, procesando foto");
                dialog.show();
                dialog.setCanceledOnTouchOutside(true);

                final Uri resultUri = result.getUri();
                StorageReference filePath = userImagenPerfil.child(currentUserId+".jpg");
                final File url = new File(resultUri.getPath());
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            userImagenPerfil.child(currentUserId+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUri = uri.toString();
                                    userRef.child(currentUserId).child("imagen").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){
                                                Picasso.get().load(downloadUri).into(imagen);
                                                dialog.dismiss();
                                            }else{
                                                String mensaje = task.getException().getMessage();
                                                Toast.makeText(getContext(), "Error: "+mensaje, Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });

            }else {
                Toast.makeText(getContext(), "Imagen no soportada", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }
    }

    private void guardarInformacion() {

        String nombres = nombre.getText().toString().toUpperCase();
        String direcciones = direccion.getText().toString();
        String ciudades = ciudad.getText().toString();
        String phones = telefn.getText().toString();

        if(TextUtils.isEmpty(nombres)){
            Toast.makeText(getContext(), "Ingrese el nombre", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(direcciones)){
            Toast.makeText(getContext(), "Ingrese el dirección", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(ciudades)){
            Toast.makeText(getContext(), "Ingrese la ciudad", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(phones)){
            Toast.makeText(getContext(), "Ingrese su número de teléfono", Toast.LENGTH_SHORT).show();
        }else {
            dialog.setTitle("Guardando");
            dialog.setMessage("Espere...");
            dialog.show();
            dialog.setCanceledOnTouchOutside(true);

            HashMap map = new HashMap();
            map.put("nombre", nombres);
            map.put("dirección", direcciones);
            map.put("ciudad", ciudades);
            map.put("telefono", phones);
            map.put("uid", currentUserId);

            userRef.child(currentUserId).updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        enviarAlInicio();
                        dialog.dismiss();
                    }else{
                        String mensaje = task.getException().toString();
                        Toast.makeText(getContext(), "Error"+mensaje, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    private void enviarAlInicio() {
        Intent intent = new Intent(getContext(), AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}