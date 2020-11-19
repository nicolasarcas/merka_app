package com.example.merka.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.models.Loja;
import com.example.merka.models.Produto;
import com.example.merka.models.User;
import com.example.merka.R;
import com.example.merka.recyclerview.LojaAdapter;
import com.example.merka.recyclerview.ProdutoHorizontalAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Tela_Inicial extends AppCompatActivity {

    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase

    private LojaAdapter adapterLoja;
    private List<Loja> lojas;

    private ProdutoHorizontalAdapter adapterProduto;
    private List<Produto> produtos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela__inicial);

        mAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        RecyclerView lojasRecyclerView = findViewById(R.id.recyclerViewTelaInicialLojas);
        lojas = new ArrayList<>();
        adapterLoja = new LojaAdapter(lojas,this);
        lojasRecyclerView.setAdapter(adapterLoja);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lojasRecyclerView.setLayoutManager(linearLayoutManager);
        adapterLoja.OnItemClickListener(new LojaAdapter.OnLojaListener() {
            @Override
            public void onLojaClick(int position) {
                Loja l = lojas.get(position);
                Intent i = new Intent(Tela_Inicial.this, VitrineLoja.class);
                i.putExtra("idLoja", l.id);
                i.putExtra("comingFrom","inicio");
                startActivity(i);
                finish();
            }
        });

        RecyclerView produtosRecyclerView = findViewById(R.id.recyclerViewTelaInicialProdutos);
        produtos = new ArrayList<>();
        adapterProduto = new ProdutoHorizontalAdapter(produtos,this);
        produtosRecyclerView.setAdapter(adapterProduto);
        LinearLayoutManager linearLayoutManagerProd = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false); //***
        produtosRecyclerView.setLayoutManager(linearLayoutManagerProd);

        TextView btnPerfil = findViewById(R.id.textViewPerfilPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Tela_Inicial.this, Perfil.class));
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
                startActivity(new Intent(Tela_Inicial.this, TelaBusca.class));
                startActivity(new Intent(Tela_Inicial.this, TelaBusca.class));
                finish();
            }
        });
    }

    private void decisaoLoja() {
        mAuth.getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference().child("users").child(Objects.requireNonNull(mAuth.getUid()));
        // Get Post object and use the values to update the UI
        // [START_EXCLUDE]
        // [END_EXCLUDE]
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                // [START_EXCLUDE]
                if (Objects.requireNonNull(user).store) {
                    startActivity(new Intent(Tela_Inicial.this, PerfilLoja.class));
                } else {
                    startActivity(new Intent(Tela_Inicial.this, CriarLoja.class));
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Tela_Inicial.this, getString(R.string.ToastErroAoCarregarDadosUsuario),
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebaseLojas();
        setupFirebaseProdutos();
    }

    private void setupFirebaseProdutos() {

        DatabaseReference refUserProduto = FirebaseDatabase.getInstance().getReference().child("produtos");

        refUserProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    produtos.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        for(DataSnapshot d : ds.getChildren()){
                            produtos.add(d.getValue(Produto.class));
                        }
                    }
                    Collections.shuffle(produtos);
                    adapterProduto.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Tela_Inicial.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFirebaseLojas() {

        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    lojas.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        lojas.add(ds.getValue(Loja.class));
                    }
                    Collections.shuffle(lojas);
                    adapterLoja.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Tela_Inicial.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}