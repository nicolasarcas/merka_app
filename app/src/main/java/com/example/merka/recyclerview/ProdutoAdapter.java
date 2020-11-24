package com.example.merka.recyclerview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.models.Produto;
import com.example.merka.R;

import java.util.List;

import static com.example.merka.utils.PicMethods.loadPic;

public class ProdutoAdapter extends RecyclerView.Adapter <ProdutoViewHolder> {
    private final List<Produto> produtos;
    private final Context context;

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
    public void onBindViewHolder(@NonNull final ProdutoViewHolder holder, int position){
        Produto p = produtos.get(position);
        final String valor = "R$ " + p.getValor();

        holder.txtNomeProduto.setText(p.getNome());
        holder.txtValorProduto.setText(valor);
        holder.txtDescricaoProduto.setText(p.getDescricao());
        loadPic(holder.pic, p.getPic(), "Produtos");
    }

    @Override
    public int getItemCount(){
        return produtos.size();
    }

}

