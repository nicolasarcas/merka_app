package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private Button btnEntrar;
    private Button btnCadastrar;

    private EditText editTextEmail;
    private EditText editTextSenha;

    private TextView txtForgotPass;

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
                fazerLogin(view);
            }
        });

        btnCadastrar =findViewById((R.id.button));

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Login.this, MainActivity.class));
            }
        });

        txtForgotPass = findViewById(R.id.textViewEsqueciSenha);

        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetSenha(view);
            }
        });
    }

    private  void resetSenha(View view){
        if(editTextEmail.getEditableText().toString().isEmpty()){
            Toast.makeText(Login.this, getString(R.string.preencherEmail),
                    Toast.LENGTH_SHORT).show();
        }
        else{
            firebaseAuth.sendPasswordResetEmail(editTextEmail.getEditableText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(Login.this, getString(R.string.resset_pass_successful),
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Login.this, getString(R.string.resset_pass_failure),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void fazerLogin(View view){
        String login = editTextEmail.getEditableText().toString();
        String senha = editTextSenha.getEditableText().toString();

        if(validateFields(login,senha)){

            firebaseAuth.signInWithEmailAndPassword(login,senha).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        updateUI(user);
                    } else {

                        // If sign in fails, display a message to the user.
                        Toast.makeText(Login.this, getString(R.string.authentication_failure),
                                Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // ...
                }
            });
        }
        else{
            Toast.makeText(Login.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void goToMenu(){
        startActivity (new Intent(this, Tela_Inicial.class));
        finish();
    }

    public boolean validateFields(String login, String password1){
        if(login.isEmpty() || password1.isEmpty()){
            return false;
        }
        else{
            return true;
        }
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