package com.example.merka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProdutoVitrineAdapter extends RecyclerView.Adapter <ProdutoVitrineViewHolder> {

    private List<Produto> produtos;
    private Context context;

    ProdutoVitrineAdapter(List<Produto> produtos,Context context){
        this.produtos = produtos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProdutoVitrineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(context);
        View v =inflater.inflate(R.layout.produtos_item, parent, false);
        return new ProdutoVitrineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoVitrineViewHolder holder, int position){
        Produto p = produtos.get(position);
        holder.txtNomeProduto.setText(p.nome);
        holder.txtValorProduto.setText("R$ "+p.valor);
        holder.txtDescricaoProduto.setText(p.descricao);
        if(p.picUrl.length() > 0) new EditLojaPerfil.DownloadImageTask((ImageView) holder.pic).execute(p.picUrl);
    }

    @Override
    public int getItemCount(){
        return produtos.size();
    }

}

