package com.example.merka;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ProdutoViewHolder extends RecyclerView.ViewHolder {

    TextView txtNomeProduto;
    TextView txtValorProduto;
    TextView txtDescricaoProduto;

    ProdutoViewHolder(View v){
        super(v);

        this.txtNomeProduto = v.findViewById(R.id.nomeProduto);
        this.txtValorProduto = v.findViewById(R.id.valorProduto);
        this.txtDescricaoProduto = v.findViewById(R.id.descricaoProduto);
    }

}
