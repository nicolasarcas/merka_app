package com.example.merka.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.models.Loja;
import com.example.merka.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class LojaAdapter extends RecyclerView.Adapter<LojaViewHolder> {
    private final List<Loja> lojas;
    private final Context context;
    private OnLojaListener lojaListener;

    public LojaAdapter(List<Loja> lojas, Context context){
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
    public void onBindViewHolder(@NonNull final LojaViewHolder holder, int position) {
        Loja l = lojas.get(position);
        holder.txtNomeLoja.setText(l.nome);
        holder.txtEnderecoLoja.setText(l.endereco);
        holder.txtDescLoja.setText(l.descricao);
        holder.txtContatoLoja.setText(l.contato);

        if(l.pic.length() > 0) {

            Log.d("teste", l.pic);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference()
                    .child("Images").child("Lojas").child(l.pic);

            imageRef.getBytes(1024*1024)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                   public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.pic.setImageBitmap(bitmap);
                    }
                });
        }
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
