package com.example.merka.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.merka.models.Produto;
import com.example.merka.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter <ProdutoViewHolder> {
    private final List<Produto> produtos;
    private final Context context;

    public ProdutoAdapter(List<Produto> produtos, Context context){
        this.produtos = produtos;
        this.context = context;
    }
    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        LayoutInflater inflater = LayoutInflater.from(context);
        View v =inflater.inflate(R.layout.produtos_item, parent, false);
        return new ProdutoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProdutoViewHolder holder, int position){
        Produto p = produtos.get(position);
        final String valor = "R$ " + p.valor;

        holder.txtNomeProduto.setText(p.nome);
        holder.txtValorProduto.setText(valor);
        holder.txtDescricaoProduto.setText(p.descricao);

        if(p.pic.length() > 0) {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference imageRef = storage.getReference()
                    .child("Images").child("Produtos").child(p.pic);

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
    public int getItemCount(){
        return produtos.size();
    }

}

