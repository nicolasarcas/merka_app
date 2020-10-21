package com.example.merka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProdutoHorizontalAdapter extends RecyclerView.Adapter<ProdutoHorizontalViewHolder> {

    private List<Produto> produtos;
    private Context context;

    ProdutoHorizontalAdapter(List<Produto> produtos, Context context){
        this.produtos = produtos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProdutoHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v =inflater.inflate(R.layout.produtos_item_horizontal, parent, false);
        return new ProdutoHorizontalViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoHorizontalViewHolder holder, int position) {
        Produto p = produtos.get(position);

        holder.txtNomeProduto.setText(p.nome);
        holder.txtValorProduto.setText("R$ "+p.valor);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }
}
