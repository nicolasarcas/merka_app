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

public class ProdutoHorizontalAdapter extends RecyclerView.Adapter <ProdutoHorizontalViewHolder> {

    private final List<Produto> produtos;
    private final Context context;

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
    public void onBindViewHolder(@NonNull final ProdutoHorizontalViewHolder holder, int position) {
        Produto p = produtos.get(position);
        String valor = context.getString(R.string.reais)+ p.getValor();
        holder.txtNomeProduto.setText(p.getNome());
        holder.txtValorProduto.setText(valor);
        loadPic(holder.pic, p.getPic(), "Produtos");
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }
}
