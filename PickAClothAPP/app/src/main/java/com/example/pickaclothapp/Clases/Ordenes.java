package com.example.pickaclothapp.Clases;

public class Ordenes {
    // Se genererará una orden por cliente, no habrá datos duplicados ni desbordamientos


        private String correo, direccion, estado, fecha, hora, nombre, telefono, total;

        public Ordenes() {}

        public Ordenes(String correo, String direccion, String estado, String fecha, String hora, String nombre, String telefono, String total) {
            this.correo = correo;
            this.direccion = direccion;
            this.estado = estado;
            this.fecha = fecha;
            this.hora = hora;
            this.nombre = nombre;
            this.telefono = telefono;
            this.total = total;
        }

        public String getCorreo() {
            return correo;
        }

        public void setCorreo(String correo) {
            this.correo = correo;
        }

        public String getDireccion() {
            return direccion;
        }

        public void setDireccion(String direccion) {
            this.direccion = direccion;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getFecha() {
            return fecha;
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public String getHora() {
            return hora;
        }

        public void setHora(String hora) {
            this.hora = hora;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getTelefono() {
            return telefono;
        }

        public void setTelefono(String telefono) {
            this.telefono = telefono;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }

}

