package com.example.merka.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.merka.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextSenha;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogEfetuandoLogin));

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextSenha=findViewById(R.id.editTextSenha);

        Button btnEntrar = findViewById(R.id.buttonEntrar);

        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fazerLogin();
            }
        });

        Button btnCadastrar = findViewById((R.id.button));

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Login.this, Registrar.class));
            }
        });

        TextView txtForgotPass = findViewById(R.id.textViewEsqueciSenha);

        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, RedefinirSenha.class));
            }
        });
    }

    private void fazerLogin(){
        String login = editTextEmail.getEditableText().toString().toLowerCase();
        String senha = editTextSenha.getEditableText().toString();

        if(validateFields(login,senha)){

            progressDialog.show();

            firebaseAuth.signInWithEmailAndPassword(login,senha).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        progressDialog.dismiss();
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Login.this, getString(R.string.ToastFalhaNaAutenticacao),
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // ...
                }
            });
        }
        else{
            Toast.makeText(Login.this, getString(R.string.ToastPreenchaTodosCampos),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void goToMenu(){
        startActivity (new Intent(this, Tela_Inicial.class));
        finish();
    }

    public boolean validateFields(String login, String password1){
        return !login.isEmpty() && !password1.isEmpty();
    }

    private void updateUI(FirebaseUser user) {

        if(user != null){
            goToMenu();
        }
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        updateUI(currentUser);
    }
}