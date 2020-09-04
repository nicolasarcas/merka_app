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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditPerfil extends AppCompatActivity {

    private Button btnConfirmarAlteracao;
    private Button btnExcluirConta;
    private Button btnCancelarAlteracao;

    private EditText txtNome;
    private EditText txtEmail;
    private EditText txtPass;
    private EditText txtConfirmPass;

    private FirebaseUser user;
    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil);

        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtPass = findViewById(R.id.txtPass);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);

        // Initialize Database
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        btnConfirmarAlteracao = findViewById(R.id.btnConfirmarAlteracao);
        btnExcluirConta = findViewById(R.id.btnExluirConta);
        btnCancelarAlteracao = findViewById(R.id.btnCancelarAlteracao);

        btnCancelarAlteracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditPerfil.this,Perfil.class));
            }
        });

        btnExcluirConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                excluiConta();
                deleteUserData();
                FirebaseAuth.getInstance().signOut();// forçando o current user sair
                goToLogin();
            }
        });

        btnConfirmarAlteracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarUser();
            }
        });
    }

    public void goToLogin(){
        startActivity (new Intent(this, MainActivity.class));
        finish();
    }

    private void goToMenu(){
        startActivity(new Intent(EditPerfil.this, Perfil.class));
        finish();
    }

    private void atualizarUser(){
        String email = txtEmail.getEditableText().toString();
        String password = txtPass.getEditableText().toString();
        String password2 = txtConfirmPass.getEditableText().toString();
        String name = txtNome.getEditableText().toString();

        if(validateFields(email,password,password2,name)){
            if(validateMinLengthPassword(password,password2)){
                if(validateEqualPasswords(password,password2)){

                    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
                    FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = fbuser.getUid();

                    User user = new User(name, email, password);

                    refUser.child("users").child(userId).setValue(user);
                    fbuser.updateEmail(email);
                    fbuser.updatePassword(password2);

                    goToMenu();
                }else{
                    Toast.makeText(EditPerfil.this, getString(R.string.different_password_warning),
                            Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(EditPerfil.this, getString(R.string.min_length_password_warning),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(EditPerfil.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void excluiConta(){
        user = mAuth.getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditPerfil.this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(EditPerfil.this, getString(R.string.delete_failure), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void deleteUserData(){//deletar dados do usuário do banco de dados
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = user.getUid();
        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("users").child(userId).removeValue();
    }

    public boolean validateFields(String login, String password1, String password2, String name){
        if(login.isEmpty() || password1.isEmpty() || password2.isEmpty() || name.isEmpty()){
            return false;
        }
        else{
            return true;
        }

    }

    public boolean validateEqualPasswords(String password1, String password2){
        if(password1.equals(password2)){
            return true;
        }else{
            return false;
        }
    }
    public boolean validateMinLengthPassword(String password1, String password2){
        if(password1.length() > 7 || password2.length() > 7){
            return true;
        }else{
            return false;
        }

    }
}