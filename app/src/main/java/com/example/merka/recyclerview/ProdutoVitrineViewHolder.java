package com.example.merka.recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.R;

public class ProdutoVitrineViewHolder extends RecyclerView.ViewHolder  {

    final TextView txtNomeProduto;
    final TextView txtValorProduto;
    final TextView txtDescricaoProduto;

    final ImageView pic;

    ProdutoVitrineViewHolder(View v){
        super(v);

        this.pic = v.findViewById(R.id.picProduto);
        this.txtNomeProduto = v.findViewById(R.id.nomeProduto);
        this.txtValorProduto = v.findViewById(R.id.valorProduto);
        this.txtDescricaoProduto = v.findViewById(R.id.descricaoProduto);

    }

}
