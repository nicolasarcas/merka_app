package com.example.merka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter <ProdutoViewHolder> {
    private List<Produto> produtos;
    private Context context;

    ProdutoAdapter(List<Produto> produtos,Context context){
        this.produtos = produtos;
        this.context = context;
    }
    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(context);
        View v =inflater.inflate(R.layout.produtos_item, parent, false);
        return new ProdutoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position){
        Produto p = produtos.get(position);

        holder.txtNomeProduto.setText(p.nome);
        holder.txtValorProduto.setText("R$ "+p.valor);
        holder.txtDescricaoProduto.setText(p.descricao);
    }

    @Override
    public int getItemCount(){
        return produtos.size();
    }

}

