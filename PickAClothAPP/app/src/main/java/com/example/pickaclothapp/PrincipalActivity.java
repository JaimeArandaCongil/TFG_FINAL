package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.pickaclothapp.Modal.Productos;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class PrincipalActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth auth;
    private String currentUserId;
    private DatabaseReference userRef;
    private String telefono = "";
    private DatabaseReference ProductosRef;
    private FloatingActionButton botonFlotante;
    private RecyclerView recyclerMenu;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            telefono = bundle.getString("phone");
        }

        auth = FirebaseAuth.getInstance();
        currentUserId  = auth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Usuarios");
        // Inicializamos una variable que hace referencia a la bbdd de productos
        ProductosRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Productos");
        // Inicializamos el RecyclerMenu de la activity contenido_principal.xml
        recyclerMenu = findViewById(R.id.recycler_menu);
        recyclerMenu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(layoutManager);
        // Inicializa el botón flotante de app_layout_principal
        botonFlotante = (FloatingActionButton) findViewById(R.id.fab);
        // Añade un listener para cuando se hace click sobre él, para que pase a la activity del Carrito
        botonFlotante.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (PrincipalActivity.this, CarritoActivity.class);
                startActivity(intent);
            }
        });

        // Inicializamos el toolbar de  app_layout_principal
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("PickACloth");
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        TextView nombreHeader =  (TextView) headerView.findViewById(R.id.usuario_nombre_perfil);
        CircleImageView imagenHeader = (CircleImageView) headerView.findViewById(R.id.usuario_imagen_perfil);


        userRef.child(currentUserId).addValueEventListener (new ValueEventListener() {

            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("imagen")) {
                    String imagen = snapshot.child("imagen").getValue().toString();
                    nombreHeader.setText(snapshot.child("nombre").getValue().toString());
                    Picasso.get().load(imagen).error(R.drawable.ic_house).into(imagenHeader);
                } else if (snapshot.exists()) {
                    nombreHeader.setText(snapshot.child("nombre").getValue().toString());
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {} });


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

        // --- MUESTRA DE PRODUCTOS EN EL "CATÁLOGO" (base de datos)
        // Se implementa el código necesario para visualizar los productos creados por los usuarios y subidos en la bbdd
        // (Se visualizarán en productos_item_layout.xml)


        // Transforma los productos que están dados de alta en la base de datos (sus referencias)
        // en objetos de la clase Productos
        FirebaseRecyclerOptions<Productos> options = new FirebaseRecyclerOptions.Builder<Productos>()
                // Le pasamos como parámetro la referencia a la base de datos y la clase Productos
                // Para que construya desde la base de datos objetos de este tipo
                .setQuery(ProductosRef, Productos.class).build();

        // Pasa los atributos del objeto Producto a un objeto de tipo ProductoViewHolder que permitirá visualizarlos en la aplicación
        FirebaseRecyclerAdapter<Productos, ProductoViewHolder> adapter = new FirebaseRecyclerAdapter<Productos, ProductoViewHolder> (options) {


            @Override
            protected void onBindViewHolder(@NonNull ProductoViewHolder holder, int position, @NonNull Productos model) {
                // A cada variable del objeto ProductoViewHolder le traemos y asignamos el  contenido del objeto Producto que nos llegó de bbdd
                holder.productoNom.setText(model.getNombre());
                holder.productoCantidad.setText("Cantidad: " + model.getCantidad());
                holder.productoDescrip.setText("Descripción: " + model.getDescripcion());
                holder.productoPrecio.setText(model.getPrecio() + " €");

                Picasso.get().load(model.getImagen()).into(holder.productoImagen);

                // Le ponemos un listener a la imagen para que cuando se haga click sobre ella
                // conduzca al usuario al carrito pasándole el valor del ID del producto de la base de datos
                holder.productoImagen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(PrincipalActivity.this, ProductoDetallesActivity.class);
                        intent.putExtra("pid", model.getProd_id());
                        startActivity(intent);
                    }
                });

            }


            @NonNull
            @Override
            public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.productos_item_layout, parent, false);
                // Creamos el objeto ProductViewHolder
                ProductoViewHolder holder = new ProductoViewHolder(view);
                return holder;
            }
        };

        // Le pasamos al objeto RecyclerMenu el adapter de Firebse que creamos previamente
        recyclerMenu.setAdapter(adapter);
        adapter.startListening();


    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_principal_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_carrito){
            activityCarrito();
        }
        else if(id == R.id.nav_categorias){
            activityCategoria();
        }
        else if(id == R.id.nav_buscar){
            activityBuscar();
        }
        else if(id == R.id.nav_perfil){
            activityPerfil();
        }
        else if(id == R.id.nav_salir){
            auth.signOut();
            enviarAlLogin();
        }
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;

    }


    private void activityPerfil() {
        Toast.makeText(this, "Perfil", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PrincipalActivity.this, PerfilActivity.class);
        startActivity(intent);
    }

    private void activityBuscar() {
        Toast.makeText(this, "Buscar", Toast.LENGTH_SHORT).show();
    }

    private void activityCategoria() {
        Toast.makeText(this, "Categoria", Toast.LENGTH_SHORT).show();
    }

    private void activityCarrito() {
        Toast.makeText(this, "Carrito", Toast.LENGTH_SHORT).show();
    }

    private void enviarAlSetup() {
        Intent intent = new Intent(PrincipalActivity.this, SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("phone", telefono);
        startActivity(intent);
        finish();

    }

    private void enviarAlLogin() {
        Intent intent = new Intent(PrincipalActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }


}