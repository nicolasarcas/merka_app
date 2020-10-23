package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Tela_Inicial extends AppCompatActivity {

    private TextView btnPerfil;
    private TextView btnLoja;
    private TextView btnBusca;

    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase
    private ValueEventListener userListener;
    private FirebaseUser fireUser;

    private RecyclerView lojasRecyclerView;
    private LojaAdapter adapterLoja;
    private List<Loja> lojas;

    private RecyclerView produtosRecyclerView;
    private ProdutoHorizontalAdapter adapterProduto;
    private List<Produto> produtos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela__inicial);

        mAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        lojasRecyclerView = findViewById(R.id.recyclerViewTelaInicialLojas);
        lojas = new ArrayList<>();
        adapterLoja = new LojaAdapter(lojas,this);
        lojasRecyclerView.setAdapter(adapterLoja);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lojasRecyclerView.setLayoutManager(linearLayoutManager);
        adapterLoja.OnItemClickListener(new LojaAdapter.OnLojaListener() {
            @Override
            public void onLojaClick(int position) {
                Loja l = lojas.get(position);
                Intent i = new Intent(Tela_Inicial.this,VitrineLoja.class);
                i.putExtra("id",l.id);
                Toast.makeText(Tela_Inicial.this,l.id.toString(), Toast.LENGTH_LONG).show();
                //startActivity(i);
            }
        });

        produtosRecyclerView = findViewById(R.id.recyclerViewTelaInicialProdutos);
        produtos = new ArrayList<>();
        adapterProduto = new ProdutoHorizontalAdapter(produtos,this);
        produtosRecyclerView.setAdapter(adapterProduto);
        LinearLayoutManager linearLayoutManagerProd = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        produtosRecyclerView.setLayoutManager(linearLayoutManagerProd);

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
        refUser = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid());
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

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebaseLojas();
        setupFirebaseProdutos();
    }

    private void setupFirebaseProdutos() {
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos");

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        produtos.add(ds.getValue(Produto.class));
                    }
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
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
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