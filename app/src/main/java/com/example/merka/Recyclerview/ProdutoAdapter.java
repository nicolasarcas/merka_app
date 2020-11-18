package com.example.merka.Recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.Models.Produto;
import com.example.merka.R;
import com.example.merka.Utils.DownloadImageTask;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter <ProdutoViewHolder> {
    private List<Produto> produtos;
    private Context context;

    public ProdutoAdapter(List<Produto> produtos, Context context){
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
        final String valor = "R$ " + p.valor;

        holder.txtNomeProduto.setText(p.nome);
        holder.txtValorProduto.setText(valor);
        holder.txtDescricaoProduto.setText(p.descricao);
        if(p.picUrl.length() > 0) new DownloadImageTask((ImageView) holder.pic).execute(p.picUrl);
    }

    @Override
    public int getItemCount(){
        return produtos.size();
    }

}

