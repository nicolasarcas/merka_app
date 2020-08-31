package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private Button btnEntrar;
    private Button btnCadastrar;

    private EditText editTextEmail;
    private EditText editTextSenha;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextSenha=findViewById(R.id.editTextSenha);

        btnEntrar=findViewById(R.id.buttonEntrar);

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Login.this, Tela_Inicial.class);
                startActivity(intent);
            }
        });

        btnCadastrar =findViewById((R.id.button));

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Login.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void fazerLogin(View view){
        String login = editTextEmail.getEditableText().toString();
        String senha = editTextSenha.getEditableText().toString();
        firebaseAuth.signInWithEmailAndPassword(login, senha).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent = new Intent (Login.this, Tela_Inicial.class);
                startActivity(intent);

            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}