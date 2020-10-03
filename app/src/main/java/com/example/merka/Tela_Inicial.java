package com.example.merka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Tela_Inicial extends AppCompatActivity {

    private TextView btnPerfil;
    private TextView btnLoja;
    private TextView btnBusca;

    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase
    private ValueEventListener userListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela__inicial);

        mAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        btnPerfil = findViewById(R.id.textViewPerfilPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Tela_Inicial.this, Perfil.class));
            }
        });

        btnLoja = findViewById(R.id.textViewLojaPerfil);
        btnLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decisaoLoja();
            }
        });

        btnBusca = findViewById(R.id.textViewBuscaPerfil);
        btnBusca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Tela_Inicial.this, TelaBusca.class));
            }
        });
    }

    private void decisaoLoja() {
        mAuth.getCurrentUser();
        refUser = refUser.child(mAuth.getUid());
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                // [START_EXCLUDE]
                if(user.store){
                    startActivity(new Intent (Tela_Inicial.this, PerfilLoja.class));
                }
                else{
                    startActivity(new Intent (Tela_Inicial.this, CriarLoja.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Tela_Inicial.this, "Falha ao carregar dados do usuário.",
                        Toast.LENGTH_SHORT).show();
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
}