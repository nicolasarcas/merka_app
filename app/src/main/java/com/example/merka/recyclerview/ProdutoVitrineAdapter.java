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
    public void onBindViewHolder(@NonNull final ProdutoVitrineViewHolder holder, int position){
        Produto p = produtos.get(position);
        String valor = context.getString(R.string.reaisEspaco)+p.getValor();
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

