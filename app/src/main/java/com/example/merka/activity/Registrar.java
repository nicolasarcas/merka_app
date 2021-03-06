package com.example.merka.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.merka.models.User;
import com.example.merka.R;
import com.example.merka.utils.FirebaseMethods;
import com.example.merka.utils.TextMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Registrar extends AppCompatActivity {

    private EditText editTextNome;
    private EditText editTextEmail;
    private EditText editTextSenha;
    private  EditText editTextConfirmaSenha;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;
    private boolean idf = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogEfetuandoCadastro));

        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextSenha = findViewById(R.id.editNovaTextSenha);
        editTextConfirmaSenha = findViewById(R.id.editTextConfirmaSenha);
        firebaseAuth = FirebaseAuth.getInstance();
        Button btnLogin = findViewById(R.id.buttonIrParaLogin);
        Button btnCadastrar = findViewById(R.id.buttonCriarConta);

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criarNovoUsuario();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Registrar.this, Login.class);
                startActivity(intent);
            }
        });

    }

    private void criarNovoUsuario(){
        final String login = editTextEmail.getEditableText().toString().trim().toLowerCase();
        final String pass = editTextSenha.getEditableText().toString();
        String pass2 = editTextConfirmaSenha.getEditableText().toString();
        final String nome = TextMethods.formatText(editTextNome.getEditableText().toString());

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference().child("users");

        if(validateFields(login,pass,pass2,nome)){
            if(Patterns.EMAIL_ADDRESS.matcher(login).matches()){
                if(validateMinLengthPassword(pass,pass2)){
                    if(validateEqualPasswords(pass,pass2)){

                        refUser.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if(!progressDialog.isShowing()){
                                    if(FirebaseMethods.checkEmailExists(login, snapshot, idf)){
                                        printToast(getString(R.string.ToastEmailJaCadastrado));
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        progressDialog.show();

                        firebaseAuth.createUserWithEmailAndPassword(login,pass2).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    idf = true;
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    String userId = Objects.requireNonNull(user).getUid();
                                    writeNewUser(userId, nome, login, pass);
                                    FirebaseAuth.getInstance().signOut();
                                    confirmacaoCadastro();

                                } else {
                                    progressDialog.dismiss();
                                    // If sign in fails, display a message to the user.
                                    printToast(getString(R.string.ToastFalhaNoRegistro));
                                }
                            }
                        });

                    }
                    else printToast(getString(R.string.ToastInsiraSenhasIguais));

                }
                else printToast(getString(R.string.ToastSenhaComNoMinimoOitoDigitos));
            }
            else printToast(getString(R.string.ToastEmailInvalido));
        }
        else printToast(getString(R.string.ToastPreenchaTodosCampos));
    }

    private void printToast(String value){
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }

    public void confirmacaoCadastro(){

        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle(getString(R.string.msgBoxTitleCadastro));
        msgBox.setIcon(android.R.drawable.ic_popup_reminder);
        msgBox.setMessage(getString(R.string.msgBoxMessageCadastroEfetuadoRealizeLogin));
        msgBox.setPositiveButton(getString(R.string.confirmar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
                progressDialog.dismiss();
                goToLogin();
            }
        });
        msgBox.show();
    }


    public void goToLogin(){
        startActivity (new Intent(this, Login.class));
    }

    public boolean validateFields(String login, String password1, String password2, String name){
        return !login.isEmpty() && !password1.isEmpty() && !password2.isEmpty() && !name.isEmpty();
    }

    public boolean validateEqualPasswords(String password1, String password2){
        return password1.equals(password2);
    }

    public boolean validateMinLengthPassword(String password1, String password2){
        return password1.length() > 7 || password2.length() > 7;
    }


    private void writeNewUser(String userId, String name, String email, String password){
        //usando o mesmo UID do Firebase Authentication: userId

        try {//tentando cadastrar no banco
            User user = new User(name, email, password, false);
            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("users").child(userId).setValue(user);

        }
        catch(DatabaseException e){
            progressDialog.dismiss();
            Toast.makeText(Registrar.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Registrar.this, Login.class));
        finish();
    }
}