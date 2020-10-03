package com.example.merka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LojaAdapter extends RecyclerView.Adapter<LojaViewHolder> {
    private List<Loja> lojas;
    private Context context;

    LojaAdapter(List<Loja> lojas, Context context){
        this.context = context;
        this.lojas= lojas;
    }

    @NonNull
    @Override
    public LojaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v =inflater.inflate(R.layout.lojas_item, parent, false);
        return new LojaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LojaViewHolder holder, int position) {
        Loja l = lojas.get(position);

        holder.txtNomeLoja.setText(l.nome);
        holder.txtEnderecoLoja.setText(l.endereco);
        holder.txtDescLoja.setText(l.descricao);
        holder.txtContatoLoja.setText(l.contato);
    }

    @Override
    public int getItemCount() {
        return lojas.size();
    }
}
