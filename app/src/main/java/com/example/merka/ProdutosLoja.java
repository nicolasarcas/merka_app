package com.example.merka;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ProdutosLoja extends AppCompatActivity {

    private Button btnAddProduto;

    private RecyclerView produtosRecyclerView;
    private ProdutoAdapter adapter;
    private List<Produto> produtos;
    private FirebaseUser fireUser;

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
    }
}