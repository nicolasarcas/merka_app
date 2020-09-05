package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RedefinirSenha extends AppCompatActivity {

    private EditText editTextRedefinirSenha;
    private Button btnRedefinirSenha;

    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redefinir_senha);

        firebaseAuth = FirebaseAuth.getInstance();

        editTextRedefinirSenha = findViewById(R.id.editTextRedefinirSenha);
        btnRedefinirSenha = findViewById(R.id.btnRedefinirSenha);

        btnRedefinirSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSenha(view);
                goToLogin();
            }
        });
    }

    private void goToLogin(){
        startActivity(new Intent(RedefinirSenha.this,Login.class));
    }

    private  void resetSenha(View view){
        if(editTextRedefinirSenha.getEditableText().toString().isEmpty()){
            Toast.makeText(RedefinirSenha.this, getString(R.string.preencherEmail),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            firebaseAuth.sendPasswordResetEmail(editTextRedefinirSenha.getEditableText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RedefinirSenha.this, getString(R.string.resset_pass_successful),
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(RedefinirSenha.this, getString(R.string.resset_pass_failure),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}