package com.example.merka.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.models.Produto;
import com.example.merka.R;
import com.example.merka.utils.DownloadImageTask;

import java.util.List;

public class ProdutoVitrineAdapter extends RecyclerView.Adapter <ProdutoVitrineViewHolder> {

    private final List<Produto> produtos;
    private final Context context;

    public ProdutoVitrineAdapter(List<Produto> produtos, Context context){
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
        String valor = context.getString(R.string.reaisEspaco)+p.valor;
        holder.txtNomeProduto.setText(p.nome);
        holder.txtValorProduto.setText(valor);
        holder.txtDescricaoProduto.setText(p.descricao);
        if(p.pic.length() > 0) new DownloadImageTask((ImageView) holder.pic).execute(p.pic);
    }

    @Override
    public int getItemCount(){
        return produtos.size();
    }

}

