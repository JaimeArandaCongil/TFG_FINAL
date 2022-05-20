package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth;
    private String currentUserId;
    private DatabaseReference userRef;
    private String telefono = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        bottomNavigationView = findViewById(R.id.boton_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(listener);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            telefono = bundle.getString("phone");
        }

        auth = FirebaseAuth.getInstance();
        currentUserId  = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Admin");
    }//ON CREATE

    private  BottomNavigationView.OnNavigationItemSelectedListener listener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            if(item.getItemId() == R.id.fragmentUno){
                Fragmentos(new FragmentUno());
            }
            if(item.getItemId() == R.id.fragmentProductos){
                Fragmentos(new ProductosFragment());
            }
            if(item.getItemId() == R.id.fragmentDos){
                Fragmentos(new FragmentDos());
            }
            if(item.getItemId() == R.id.fragmentTres){
                Fragmentos(new FragmentTres());
            }
            if(item.getItemId() == R.id.fragmentCuatro){
                Fragmentos(new FragmentCuatro());
            }

            return true;
        }
    };

    private void Fragmentos(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser == null) {
            enviarAlLogin();
        }else{
            verificarUsuarioExistente();
        }
    }

    private void verificarUsuarioExistente() {

        final String currentUserId = auth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId)){
                    enviarAlSetup();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void enviarAlSetup() {
        Intent intent = new Intent(AdminActivity.this, SetupAdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("phone", telefono);
        startActivity(intent);
        finish();

    }

    private void enviarAlLogin() {
        Intent intent = new Intent(AdminActivity.this, LoginAdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}