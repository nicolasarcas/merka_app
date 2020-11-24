package com.example.merka.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.merka.models.User;
import com.example.merka.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Perfil extends AppCompatActivity {

    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase
    private ValueEventListener userListener;

    private TextView txtNomeUser;
    private TextView txtEmailUser;

    private boolean loja;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        mAuth = FirebaseAuth.getInstance();

        // Initialize Database
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        txtNomeUser = findViewById(R.id.txtNomeUser);
        txtEmailUser = findViewById(R.id.txtEmailUser);

        Button btnEditPerfil = findViewById(R.id.buttonEditPerfil);
        Button btnLogOut = findViewById(R.id.buttonSignOut);

        btnEditPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Perfil.this, EditPerfil.class);
                startActivity(intent);
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deslogar();
            }
        });

        TextView btnHome = findViewById(R.id.textViewHomePerfil);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Perfil.this, Tela_Inicial.class));
                finish();
            }
        });

        TextView btnLoja = findViewById(R.id.textViewLojaPerfil);
        btnLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decisaoLoja();
            }
        });

        TextView btnBusca = findViewById(R.id.textViewBuscaPerfil);
        btnBusca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Perfil.this, TelaBusca.class));
                finish();
            }
        });

    }

    private void decisaoLoja() {
        if(loja){
            startActivity(new Intent (Perfil.this, PerfilLoja.class));
        } else{
            startActivity(new Intent (Perfil.this, CriarLoja.class));
        }
        finish();
    }

    public void goToLogin(){
        startActivity (new Intent(this, Login.class));
        finish();
    }

    @Override
    protected void onStart(){
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        mAuth.getCurrentUser();
        //refUser = FirebaseDatabase.getInstance().getReference();
        refUser = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(mAuth.getUid()));

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                // [START_EXCLUDE]
                txtNomeUser.setText(Objects.requireNonNull(user).getNome());
                txtEmailUser.setText(user.getEmail());
                // [END_EXCLUDE]

                loja = user.getLoja();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Perfil.this, getString(R.string.ToastErroAoCarregarDadosUsuario), Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        refUser.removeEventListener(userListener);
    }

    public void deslogar(){

        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle(getString(R.string.msgBoxTitleDesconectar));
        msgBox.setIcon(android.R.drawable.ic_dialog_alert);
        msgBox.setMessage(getString(R.string.msgBoxMessageDesejaSeDesconectar));
        msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(Perfil.this, Tela_Inicial.class));
        finish();
    }
}