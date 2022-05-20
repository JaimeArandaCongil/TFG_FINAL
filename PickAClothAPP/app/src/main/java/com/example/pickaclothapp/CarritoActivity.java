package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pickaclothapp.Modal.Carrito;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.Principal;

public class CarritoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button siguiente;
    private TextView totalPrecio, mensaje1;

    private  double precioTotalD = 0.0;
    private String currentUserId;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        recyclerView = (RecyclerView) findViewById(R.id.carrito_lista);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        siguiente = (Button) findViewById(R.id.siguiente_proceso);
        totalPrecio = (TextView) findViewById(R.id.precio_total);
        mensaje1 = (TextView) findViewById(R.id.mensaje1);

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        siguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CarritoActivity.this, ConfirmarOrdenActivity.class);
                intent.putExtra("Total", String.valueOf(precioTotalD));
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        verificarEstadoOrden();

        final DatabaseReference CartListRef = FirebaseDatabase.getInstance().getReference().child("Carrito");


        // Gracias a FirebaseUI, y su RecyclerView, podemos usar clases como FirebaseRecyclerOptions y FirebaseRecyclerAdapter
        // que nos permiten interactuar con la base de datos y visualizar su contenido en nuestra aplicación en tiempo real

        FirebaseRecyclerOptions<Carrito> options = new FirebaseRecyclerOptions.Builder<Carrito>()
                .setQuery(CartListRef.child("Usuario compra").child(currentUserId).child("Productos"), Carrito.class).build();

        // Le pasamos como parámetro las opciones del FirebaseRecylerOptions

        //En este método le pasamos a cada elemento o variable ViewHolder los datos (atributos) del objeto que nos llega de la bbdd
        FirebaseRecyclerAdapter<Carrito, CarritoViewHolder> adapter = new FirebaseRecyclerAdapter<Carrito, CarritoViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CarritoViewHolder holder, int position, @NonNull Carrito model) {

                // Esto sacará por pantalla por la app los valores de cada producto
                holder.carProductoNombre.setText(model.getNombre());
                holder.carProductoCantidad.setText("Cantidad: " + model.getCantidad());
                holder.carProductoPrecio.setText("Precio €: " + model.getPrecio());

                // Sirve para cálculo del importe por cada producto considerando su precio y cantidad
                double unTipoPrecio =  (Double.valueOf(model.getPrecio())) * Integer.valueOf(model.getCantidad());
                // Acumula el importe total al ir sumando el de importe producto;
                precioTotalD = precioTotalD + unTipoPrecio;
                totalPrecio.setText("Total: " + String.valueOf(precioTotalD));

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence options[] = new CharSequence[] {
                                "Editar",
                                "Eliminar"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CarritoActivity.this);
                        builder.setTitle("Opciones del producto");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0) {
                                    Intent intent = new Intent(CarritoActivity.this, ProductoDetallesActivity.class);
                                    intent.putExtra("pid", model.getPid());
                                    startActivity(intent);
                                }

                                if (i==1) {
                                    CartListRef.child("Usuario compra")
                                            .child(currentUserId)
                                            .child("Productos")
                                            .child(model.getPid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(CarritoActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(CarritoActivity.this, Principal.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                        builder.show();

                    }
                });

            }

            @NonNull
            @Override
            public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_item_layout, parent, false);
                CarritoViewHolder holder = new CarritoViewHolder(view);

                return holder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void verificarEstadoOrden() {

        DatabaseReference ordenRef;
        ordenRef = FirebaseDatabase.getInstance("https://pickaclothapp-default-rtdb.europe-west1.firebasedatabase.app").getReference().child("Ordenes").child(currentUserId);

        ordenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String estado = snapshot.child("estado").getValue().toString();
                    String nombre = snapshot.child("nombre").getValue().toString();
                    if (estado.equals("Enviado")) {
                        totalPrecio.setText("Estimado " + nombre + " su pedido ha sido enviado");
                        recyclerView.setVisibility(View.GONE);
                        mensaje1.setText("Su pedido se enviará pronto");
                        mensaje1.setVisibility(View.VISIBLE);
                        siguiente.setVisibility(View.GONE);
                    } else if (estado.equals("No enviado")){
                        totalPrecio.setText("Su orden está siendo procesada");
                        recyclerView.setVisibility(View.GONE);
                        mensaje1.setVisibility(View.VISIBLE);
                        siguiente.setVisibility(View.GONE);
                        Toast.makeText(CarritoActivity.this, "Podrás comprar más productos cuando el anterior finalice", Toast.LENGTH_SHORT).show();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}
