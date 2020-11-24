package com.example.merka.activity;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static com.example.merka.utils.FirebaseMethods.checkEmailExists;
import static com.example.merka.utils.TextMethods.validUserFields;

public class EditPerfil extends AppCompatActivity {

    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase

    private EditText txtNome;
    private EditText txtEmail;
    private EditText txtPass;
    private EditText txtConfirmPass;

    User user = new User();

    public boolean temLoja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil);

        mAuth = FirebaseAuth.getInstance();

        txtNome = findViewById(R.id.txtNome);
        txtEmail = findViewById(R.id.txtEmail);
        txtPass = findViewById(R.id.txtPass);
        txtConfirmPass = findViewById(R.id.txtConfirmPass);

        // Initialize Database
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        Button btnConfirmarAlteracao = findViewById(R.id.btnConfirmarAlteracao);
        Button btnExcluirConta = findViewById(R.id.btnExluirConta);
        Button btnCancelarAlteracao = findViewById(R.id.btnCancelarAlteracao);

        btnCancelarAlteracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditPerfil.this, Perfil.class));
            }
        });

        btnExcluirConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder msgBox = new AlertDialog.Builder(EditPerfil.this);
                msgBox.setTitle(getString(R.string.msgBoxTitleExcluir));
                msgBox.setIcon(android.R.drawable.ic_menu_delete);
                msgBox.setMessage(getString(R.string.msgBoxMessageDesejaExcluirConta));
                msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        excluiConta();
                        deleteUserData();
                        FirebaseAuth.getInstance().signOut();// forçando o current user sair
                        goToLogin();
                    }
                });
                msgBox.setNegativeButton(getString(R.string.nao), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                msgBox.show();
            }
        });

        btnConfirmarAlteracao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                user.setFields(txtNome, txtEmail, txtPass,false);

                if(validUserFields(EditPerfil.this, user, txtConfirmPass.getEditableText().toString())){

                    FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(checkEmailExists(EditPerfil.this, snapshot, user, FirebaseAuth.getInstance().getCurrentUser().getUid())){

                                AlertDialog.Builder msgBox = new AlertDialog.Builder(EditPerfil.this);
                                msgBox.setTitle(getString(R.string.msgBoxTitleAlteraçãoDeDados));
                                msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                                msgBox.setMessage(getString(R.string.msgBoxMessageDesejaAlterarDadosSeusDados));
                                msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
                                        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                                        String userId = fbuser.getUid();

                                        refUser.child("users").child(userId).setValue(user);
                                        fbuser.updateEmail(user.getEmail());
                                        fbuser.updatePassword(user.getPassword());

                                        goToMenu();
                                    }
                                });
                                msgBox.setNegativeButton(getString(R.string.nao), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                msgBox.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }

    public void goToLogin(){
        startActivity (new Intent(this, Registrar.class));
        finish();
    }

    private void goToMenu(){
        startActivity(new Intent(EditPerfil.this, Perfil.class));
        finish();
    }

    public void excluiConta(){
        FirebaseUser user = mAuth.getCurrentUser();

        Objects.requireNonNull(user).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditPerfil.this, getString(R.string.ToastExcluidoComSucesso), Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(EditPerfil.this, getString(R.string.ToastFalhaNaExclusao), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void deleteUserData(){//deletar dados do usuário do banco de dados
        FirebaseUser user = mAuth.getCurrentUser();
        String userId = Objects.requireNonNull(user).getUid();
        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("users").child(userId).removeValue();
        if(temLoja){
            refUser.child("lojas").child(userId).removeValue();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.getCurrentUser();
        //refUser = FirebaseDatabase.getInstance().getReference();
        refUser = refUser.child(Objects.requireNonNull(mAuth.getUid()));

        // Get Post object and use the values to update the UI
        // [START_EXCLUDE]
        // [END_EXCLUDE]
        // [END_EXCLUDE]
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                // [START_EXCLUDE]
                txtNome.setText(Objects.requireNonNull(user).getNome());
                txtEmail.setText(user.getEmail());
                txtPass.setText(user.getPassword());
                txtConfirmPass.setText(user.getPassword());
                // [END_EXCLUDE]

                temLoja = user.getLoja();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditPerfil.this, getString(R.string.ToastErroAoCarregarDadosUsuario),
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditPerfil.this, Perfil.class));
        finish();
    }
}