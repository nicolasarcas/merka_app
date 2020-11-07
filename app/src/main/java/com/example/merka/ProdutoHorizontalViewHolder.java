package com.example.merka;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ProdutoHorizontalViewHolder extends RecyclerView.ViewHolder {

    TextView txtNomeProduto;
    TextView txtValorProduto;
    ImageView pic;

    ProdutoHorizontalViewHolder(View v){
        super(v);

        this.pic = v.findViewById(R.id.picProdutoHorizontal);
        this.txtNomeProduto = v.findViewById(R.id.nomeProdutoHorizontal);
        this.txtValorProduto = v.findViewById(R.id.valorProdutoHorizontal);
    }
}
