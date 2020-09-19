package com.example.merka;

import androidx.annotation.NonNull;
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

public class PerfilLoja extends AppCompatActivity {

    private Button btnEditarLoja;

    private TextView txtNomeLoja;
    private TextView txtContatoLoja;
    private TextView txtEnderecoLoja;
    private TextView txtDescricao;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;

    private TextView btnHome;
    private TextView btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_loja);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        txtNomeLoja=findViewById(R.id.txtPerfilNomeLoja);
        txtContatoLoja=findViewById(R.id.txtPerfilContatoLoja);
        txtEnderecoLoja=findViewById(R.id.txtPerfilEnderecoLoja);
        txtDescricao=findViewById(R.id.txtPerfilDescricaoLoja);

        btnEditarLoja=findViewById(R.id.buttonEditarLoja);
        btnEditarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PerfilLoja.this,EditLojaPerfil.class));
            }
        });

        btnHome = findViewById(R.id.textViewHomePerfil);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (PerfilLoja.this, Tela_Inicial.class));
            }
        });

        btnPerfil = findViewById(R.id.textViewPerfilPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (PerfilLoja.this, Perfil.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(firebaseAuth.getUid());

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Loja loja = snapshot.getValue(Loja.class);

                txtNomeLoja.setText(loja.nome);
                txtContatoLoja.setText(loja.contato);
                txtEnderecoLoja.setText(loja.endereco);
                txtDescricao.setText(loja.descricao);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PerfilLoja.this, "Falha ao carregar dados da loja.",
                        Toast.LENGTH_SHORT).show();
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

