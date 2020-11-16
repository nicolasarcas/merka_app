package com.example.merka.Recyclerview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.R;

public class LojaViewHolder extends RecyclerView.ViewHolder{

    TextView txtNomeLoja;
    TextView txtDescLoja;
    TextView txtContatoLoja;
    TextView txtEnderecoLoja;
    ImageView pic;
    CardView cardLojas;

    LojaViewHolder(View v, final LojaAdapter.OnLojaListener listener){
        super(v);

        this.pic = v.findViewById(R.id.picLoja);
        this.txtNomeLoja = v.findViewById(R.id.nomeLoja);
        this.txtDescLoja = v.findViewById(R.id.descricaoLoja);
        this.txtContatoLoja = v.findViewById(R.id.contatoLoja);
        this.txtEnderecoLoja = v.findViewById(R.id.enderecoLoja);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null){
                    int position = getAdapterPosition();
                    if(position!= RecyclerView.NO_POSITION){
                        listener.onLojaClick(position);
                    }
                }
            }
        });
    }
}
