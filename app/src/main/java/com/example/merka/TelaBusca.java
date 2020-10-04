package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;

import android.view.View;
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
import java.util.List;

public class TelaBusca extends AppCompatActivity {

    private TextView btnHome;
    private TextView btnPerfil;
    private TextView btnLoja;

    private SearchView searchBusca;

    private RecyclerView lojasRecyclerView;
    private LojaAdapter adapter;
    private List<Loja> lojas;

    private FirebaseUser fireUser;
    private DatabaseReference refUser;
    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_busca);

        btnHome = findViewById(R.id.textViewHomePerfil);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TelaBusca.this, Tela_Inicial.class));
                finish();
            }
        });
        btnPerfil = findViewById(R.id.textViewPerfilPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TelaBusca.this, Perfil.class));
                finish();
            }
        });
        btnLoja = findViewById(R.id.textViewLojaPerfil);
        btnLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decisaoLoja();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");
        searchBusca = findViewById(R.id.searchViewBusca);

        lojasRecyclerView = findViewById(R.id.recyclerViewBusca);
        lojas = new ArrayList<>();
        adapter = new LojaAdapter(lojas,this);
        lojasRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        lojasRecyclerView.setLayoutManager(linearLayoutManager);
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
                    startActivity(new Intent (TelaBusca.this, PerfilLoja.class));
                    finish();
                }
                else{
                    startActivity(new Intent (TelaBusca.this, CriarLoja.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(TelaBusca.this, "Falha ao carregar dados do usuário.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }

    private void setupFirebase() {
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        lojas.add(ds.getValue(Loja.class));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TelaBusca.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();

        if(searchBusca != null){
            searchBusca.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    search(s);
                    return true;
                }
            });
        }
    }

    private void search(String texto) {
        ArrayList<Loja> list = new ArrayList<>();

        for(Loja object : lojas){
            if(object.getNome().toLowerCase().contains(texto.toLowerCase())
                || object.getDescricao().toLowerCase().contains(texto.toLowerCase())){
                list.add(object);
            }
        }
        LojaAdapter lojaAdapter = new LojaAdapter(list,this);
        lojasRecyclerView.setAdapter(lojaAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(TelaBusca.this, Tela_Inicial.class));
        finish();
    }
}