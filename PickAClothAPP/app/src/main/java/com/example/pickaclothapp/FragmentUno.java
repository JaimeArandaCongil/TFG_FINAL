package com.example.pickaclothapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class FragmentUno extends Fragment {

    private View fragmento;
    private ImageView camiseta, pantalones, falda, camisa;
    private ImageView abrigo, vestido, zapatos, gorra;

    public FragmentUno() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmento = inflater.inflate(R.layout.fragment_uno, container, false);

        // Inicializamos los elementos de la activity
        camiseta = (ImageView) fragmento.findViewById(R.id.camiseta);
        pantalones = (ImageView) fragmento.findViewById(R.id.pantalon);
        falda = (ImageView) fragmento.findViewById(R.id.falda);
        camisa = (ImageView) fragmento.findViewById(R.id.camisa);
        abrigo = (ImageView) fragmento.findViewById(R.id.abrigo);
        vestido = (ImageView) fragmento.findViewById(R.id.vestido);
        zapatos = (ImageView) fragmento.findViewById(R.id.zapatos);
        gorra = (ImageView) fragmento.findViewById(R.id.gorra);

        camiseta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Camisetas");
                startActivity(intent);
            }
        });

        pantalones.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Pantalones");
                startActivity(intent);
            }
        });

        falda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Faldas");
                startActivity(intent);
            }
        });

        camisa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Camisas");
                startActivity(intent);
            }
        });

        abrigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Abrigos");
                startActivity(intent);
            }
        });

        vestido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Vestidos");
                startActivity(intent);
            }
        });

        zapatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Zapatos");
                startActivity(intent);
            }
        });

        gorra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Gorras");
                startActivity(intent);
            }
        });

        camiseta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AgregarProductoActivity.class);
                intent.putExtra("categoria", "Camiseta");
                startActivity(intent);
            }
        });


        return fragmento;

    }
}