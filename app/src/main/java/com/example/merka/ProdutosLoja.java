package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProdutosLoja extends AppCompatActivity {

    private FloatingActionButton btnAddProduto;

    private RecyclerView produtosRecyclerView;
    private ProdutoAdapter adapter;
    private List<Produto> produtos;

    private FirebaseUser fireUser;
    private DatabaseReference refUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produtos_loja);

        btnAddProduto = findViewById(R.id.btnAddProduto);
        btnAddProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProdutosLoja.this, AdicionaProduto.class));
            }
        });

        produtosRecyclerView = findViewById(R.id.recyclerViewProdutos);
        produtos = new ArrayList<>();
        adapter = new ProdutoAdapter(produtos,this);
        produtosRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        produtosRecyclerView.setLayoutManager(linearLayoutManager);
    }
    private void setupFirebase() {
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos").child(fireUser.getUid());

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot ds : snapshot.getChildren()){
                        produtos.add(ds.getValue(Produto.class));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProdutosLoja.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProdutosLoja.this,PerfilLoja.class));
        finish();
    }
}