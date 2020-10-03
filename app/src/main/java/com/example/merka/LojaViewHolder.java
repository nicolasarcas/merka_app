package com.example.merka;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class LojaViewHolder extends RecyclerView.ViewHolder {

    TextView txtNomeLoja;
    TextView txtDescLoja;
    TextView txtContatoLoja;
    TextView txtEnderecoLoja;

    LojaViewHolder(View v){
        super(v);

        this.txtNomeLoja = v.findViewById(R.id.nomeLoja);
        this.txtDescLoja = v.findViewById(R.id.descricaoLoja);
        this.txtContatoLoja = v.findViewById(R.id.contatoLoja);
        this.txtEnderecoLoja = v.findViewById(R.id.enderecoLoja);
    }
}
