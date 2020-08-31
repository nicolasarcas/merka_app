package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;

    private EditText editTextEmail;
    private EditText editTextSenha;
    private  EditText editTextConfirmaSenha;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextEmail = findViewById(R.id.editTextNovoEmailAddress);
        editTextSenha = findViewById(R.id.editTextNovaSenha);
        editTextConfirmaSenha = findViewById(R.id.editTextConfirmaSenha);
        firebaseAuth = FirebaseAuth.getInstance();

        btnLogin = findViewById(R.id.buttonIrParaLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

    }

    private void criarNovoUsuario(View view){
        String login = editTextEmail.getEditableText().toString();
        String pass = editTextSenha.getEditableText().toString();
        String confirmaSenha = editTextConfirmaSenha.getEditableText().toString();

        firebaseAuth.createUserWithEmailAndPassword(login,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(MainActivity.this, authResult.getUser().getEmail(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent (MainActivity.this, Login.class);
                startActivity(intent);

                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}