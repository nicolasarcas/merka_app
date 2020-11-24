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

import static com.example.merka.utils.FirebaseMethods.checkEmailExists;
import static com.example.merka.utils.TextMethods.fieldsNotEmpty;
import static com.example.merka.utils.TextMethods.validUserFields;
import static com.example.merka.utils.TextMethods.validateEqualPasswords;
import static com.example.merka.utils.TextMethods.validateMinLengthPassword;

public class Registrar extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    private EditText editTextNome;
    private EditText editTextEmail;
    private EditText editTextSenha;
    private EditText editTextConfirmaSenha;

    User user = new User();

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

                user.setFields(editTextNome, editTextEmail, editTextSenha, false);

                if(validUserFields(Registrar.this, user, editTextConfirmaSenha.getEditableText().toString())){

                    FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(!progressDialog.isShowing()){
                                if (checkEmailExists(Registrar.this, snapshot, user)) {

                                    progressDialog.show();
                                    firebaseAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(Registrar.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {

                                            if (task.isSuccessful()) {

                                                writeNewUser();
                                                FirebaseAuth.getInstance().signOut();
                                                confirmacaoCadastro();
                                            } else {
                                                progressDialog.dismiss();
                                                // If sign in fails, display a message to the user.
                                                Toast.makeText(Registrar.this, getString(R.string.ToastFalhaNoRegistro), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
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


    private void writeNewUser(){
        //usando o mesmo UID do Firebase Authentication: userId
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String userId = Objects.requireNonNull(firebaseUser).getUid();

        try {//tentando cadastrar no banco
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