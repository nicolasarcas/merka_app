package com.example.merka;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LojaAdapter extends RecyclerView.Adapter<LojaViewHolder> {
    private List<Loja> lojas;
    private Context context;
    private OnLojaListener lojaListener;

    LojaAdapter(List<Loja> lojas, Context context){
        this.context = context;
        this.lojas= lojas;
    }

    @NonNull
    @Override
    public LojaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v =inflater.inflate(R.layout.lojas_item, parent, false);
        return new LojaViewHolder(v, lojaListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LojaViewHolder holder, int position) {
        Loja l = lojas.get(position);

        holder.txtNomeLoja.setText(l.nome);
        holder.txtEnderecoLoja.setText(l.endereco);
        holder.txtDescLoja.setText(l.descricao);
        holder.txtContatoLoja.setText(l.contato);
        if(l.PicUrl!=null) new EditLojaPerfil.DownloadImageTask((ImageView) holder.pic).execute(l.PicUrl);
    }

    @Override
    public int getItemCount() {
        return lojas.size();
    }

    public interface OnLojaListener{
        void onLojaClick(int position);
    }

    public void OnItemClickListener(OnLojaListener listener){
        lojaListener = listener;
    }
}
