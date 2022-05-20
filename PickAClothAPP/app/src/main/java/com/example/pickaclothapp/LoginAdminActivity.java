package com.example.pickaclothapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginAdminActivity extends AppCompatActivity {

    private EditText numero, codigo;
    private Button enviarNumero, enviarCodigo;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificacionID;
    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private FirebaseAuth auth;
    private ProgressDialog dialog;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        numero=(EditText) findViewById(R.id.numeroadmin);
        codigo=(EditText) findViewById(R.id.codigoadmin);
        enviarNumero=(Button) findViewById(R.id.enviarnumeroadmin);
        enviarCodigo=(Button) findViewById(R.id.enviarcodigoadmin);

        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);

        enviarNumero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = numero.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(LoginAdminActivity.this, "Ingresa tu numero primero...", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Validando número");
                    dialog.setMessage("Por favor espere mientars validamos su número");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginAdminActivity.this)
                            .setCallbacks(callbacks)
                            .build();
                    PhoneAuthProvider.verifyPhoneNumber(options); // EMVIA EL NUMERO

                }
            }
        });
        enviarCodigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                numero.setVisibility(View.GONE);
                enviarNumero.setVisibility((View.GONE));
                String verificacionCode = codigo.getText().toString();
                if (TextUtils.isEmpty(verificacionCode)){
                    Toast.makeText(LoginAdminActivity.this, "Ingresa el codigo recibido", Toast.LENGTH_SHORT).show();
                }else{
                    dialog.setTitle("Verificando");
                    dialog.setMessage("Espere por favor...");
                    dialog.show();
                    dialog.setCanceledOnTouchOutside(true);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificacionID, verificacionCode);
                    ingresadoConExito(credential);

                }
            }
        });

        callbacks= new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                ingresadoConExito(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                dialog.dismiss();
                Toast.makeText(LoginAdminActivity.this, "Fallo en el inicio: Número Inválido / Sin conexión a Internet / Sin código de región", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.VISIBLE);
                enviarNumero.setVisibility(View.VISIBLE);
                codigo.setVisibility(View.GONE);
                enviarCodigo.setVisibility(View.GONE);

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                verificacionID = s;
                resendingToken = token;
                dialog.dismiss();
                Toast.makeText(LoginAdminActivity.this, "Código enviado satisfactoriamente, revisa tu bandeja de entrada", Toast.LENGTH_SHORT).show();
                numero.setVisibility(View.GONE);
                enviarNumero.setVisibility(View.GONE);
                codigo.setVisibility(View.VISIBLE);
                enviarCodigo.setVisibility(View.VISIBLE);
            }
        };

    }

    private void ingresadoConExito(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dialog.dismiss();
                    Toast.makeText(LoginAdminActivity.this, "Ingresado con éxito", Toast.LENGTH_SHORT).show();
                    enviarAlaPrincipal();
                }else{
                    String error = task.getException().toString();
                    Toast.makeText(LoginAdminActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if(firebaseUser != null){
            enviarAlaPrincipal();
        }
    }

    private void enviarAlaPrincipal() {
        Intent intent = new Intent(LoginAdminActivity.this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("phone", phoneNumber);
        startActivity(intent);
        finish();

    }
}