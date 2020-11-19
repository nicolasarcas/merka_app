package com.example.merka.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.merka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenha extends AppCompatActivity {

    private EditText editTextRedefinirSenha;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextRedefinirSenha = findViewById(R.id.editTextRedefinirSenha);
        Button btnRedefinirSenha = findViewById(R.id.btnRedefinirSenha);

        btnRedefinirSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSenha();
            }
        });
    }

    private void goToLogin(){
        startActivity(new Intent(RedefinirSenha.this, Login.class));
    }

    private  void resetSenha(){
        if(editTextRedefinirSenha.getEditableText().toString().isEmpty()){
            Toast.makeText(RedefinirSenha.this, getString(R.string.TextViewHintPreencherEmail),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            firebaseAuth.sendPasswordResetEmail(editTextRedefinirSenha.getEditableText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RedefinirSenha.this, getString(R.string.ToastEmailDeRecuperacaoEnviado),
                                        Toast.LENGTH_SHORT).show();
                                goToLogin();
                            }
                            else{
                                Toast.makeText(RedefinirSenha.this, getString(R.string.ToastFalhaAoEnviarEmailDeRecuperacao),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(RedefinirSenha.this, Login.class));
        finish();
    }
}