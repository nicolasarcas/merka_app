package com.example.merka.Recyclerview;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.R;

public class ProdutoViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    TextView txtNomeProduto;
    TextView txtValorProduto;
    TextView txtDescricaoProduto;

    CardView cardProdutos;
    ImageView pic;

    ProdutoViewHolder(View v){
        super(v);

        this.pic = v.findViewById(R.id.picProduto);
        this.txtNomeProduto = v.findViewById(R.id.nomeProduto);
        this.txtValorProduto = v.findViewById(R.id.valorProduto);
        this.txtDescricaoProduto = v.findViewById(R.id.descricaoProduto);

        this.cardProdutos = v.findViewById(R.id.cardProdutos);
        cardProdutos.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.add(this.getAdapterPosition(),11,11,"Alterar");
        contextMenu.add(this.getAdapterPosition(),22,22,"Excluir");
    }
}
