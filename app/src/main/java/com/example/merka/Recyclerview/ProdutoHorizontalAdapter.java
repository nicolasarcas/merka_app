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

public class ProdutoHorizontalAdapter extends RecyclerView.Adapter <ProdutoHorizontalViewHolder> {

    private List<Produto> produtos;
    private Context context;

    public ProdutoHorizontalAdapter(List<Produto> produtos, Context context){
        this.context = context;
        this.produtos = produtos;
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
        holder.txtValorProduto.setText("R$"+p.valor);
        if(p.picUrl.length() > 0) new DownloadImageTask((ImageView) holder.pic).execute(p.picUrl);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }
}
