package com.example.pickaclothapp;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView productoNom, productoDescrip, productoPrecio, productoCantidad;
    public ImageView productoImagen;
    public ItemClickListener listener;

    public ProductoViewHolder(@NonNull View itemView) {
        super(itemView);

        // Referenciamos los elementos de la activity prdductos_item_layout para poder acceder a ellos y rellenarlos después
        // con los valores que vengan de la bbdd PrincipalActivity.java
        productoNom = (TextView) itemView.findViewById(R.id.producto_nombre);
        productoDescrip = (TextView) itemView.findViewById(R.id.producto_descripcion);
        productoPrecio = (TextView) itemView.findViewById(R.id.producto_precio);
        productoCantidad = (TextView) itemView.findViewById(R.id.producto_cantidad);
        productoImagen = (ImageView) itemView.findViewById(R.id.producto_imagen);
    }

    public void setItemClickListener(ItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        listener.onClick(v, getAdapterPosition(), false);
    }
}
