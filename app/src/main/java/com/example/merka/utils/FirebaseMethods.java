package com.example.merka.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.merka.R;
import com.example.merka.models.Loja;
import com.example.merka.models.Produto;
import com.example.merka.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.merka.utils.PicMethods.deleteImageFromFirebaseStorage;

public class FirebaseMethods {

    public static boolean checkCPFExists(final Context context, DataSnapshot dataSnap, Loja loja){

        boolean cpfExists = false;

        for (DataSnapshot ds: dataSnap.getChildren()){

            if (loja.getCpf().equals(ds.getValue(Loja.class).getCpf())){

                if(!loja.getNome().equals((ds.getValue(Loja.class).getNome()))){

                    Toast.makeText(context, context.getString(R.string.ToastCPFJaUtilizado), Toast.LENGTH_SHORT).show();
                    cpfExists = true;
                }
            }
        }

        return !cpfExists;
    }

    public static boolean checkCPFExists(final Context context, DataSnapshot dataSnap, Loja loja, String userId){

        boolean cpfExists = false;
        DataSnapshot atual = dataSnap.child(userId);

        for (DataSnapshot ds: dataSnap.getChildren()){

            if (loja.getCpf().equals(ds.getValue(Loja.class).getCpf())){

                if(!ds.getValue().equals(atual.getValue())) {
                    Toast.makeText(context, context.getString(R.string.ToastCPFJaUtilizado), Toast.LENGTH_SHORT).show();
                    cpfExists = true;
                }
            }
        }

        return !cpfExists;
    }

    public static boolean checkContatoExists(final Context context, DataSnapshot dataSnap, Loja loja){

        boolean contatoExists = false;

        for (DataSnapshot ds: dataSnap.getChildren()){

            if (loja.getContato().equals(ds.getValue(Loja.class).getContato())){

                if(!loja.getNome().equals((ds.getValue(Loja.class).getNome()))){

                    Toast.makeText(context, context.getString(R.string.ToastContatoJaUtilizado), Toast.LENGTH_SHORT).show();
                    contatoExists = true;
                }
            }
        }

        return !contatoExists;
    }

    public static boolean checkContatoExists(final Context context, DataSnapshot dataSnap, Loja loja, String userId){

        boolean contatoExists = false;
        DataSnapshot atual = dataSnap.child(userId);

        for (DataSnapshot ds: dataSnap.getChildren()){

            if (loja.getContato().equals(ds.getValue(Loja.class).getContato())){

                if(!loja.getNome().equals((ds.getValue(Loja.class).getNome()))){

                    if(!ds.getValue().equals(atual.getValue())) {
                        Toast.makeText(context, context.getString(R.string.ToastContatoJaUtilizado), Toast.LENGTH_SHORT).show();
                        contatoExists = true;
                    }
                }
            }
        }

        return !contatoExists;
    }

    public static boolean checkEmailExists(final Context context, DataSnapshot dataSnap, User user){

        boolean emailExists = false;

        for (DataSnapshot ds: dataSnap.getChildren()){

            if (user.getEmail().equals(ds.getValue(User.class).getEmail())){

                if(!user.getNome().equals((ds.getValue(User.class).getNome()))){

                    Toast.makeText(context, context.getString(R.string.ToastEmailJaCadastrado), Toast.LENGTH_SHORT).show();
                    emailExists = true;
                }
            }
        }

        return !emailExists;
    }

    public static boolean checkEmailExists(final Context context, DataSnapshot dataSnap, User user, String userId){

        boolean emailExists = false;
        DataSnapshot atual = dataSnap.child(userId);

        for (DataSnapshot ds: dataSnap.getChildren()){

            if (user.getEmail().equals(ds.getValue(User.class).getEmail())){

                if(!ds.getValue().equals(atual.getValue())){
                    Toast.makeText(context, context.getString(R.string.ToastEmailJaCadastrado), Toast.LENGTH_SHORT).show();
                    emailExists = true;
                }
            }
        }

        return !emailExists;
    }

    public static void deleteUserData(final Context context, String userId){//deletar dados da loja e produtos do usuário

        DatabaseReference refUser;
        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("lojas").child(userId).removeValue();

        DatabaseReference refUserProduto = FirebaseDatabase.getInstance().getReference().child("produtos").child(userId);

        refUserProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    for(DataSnapshot ds : snapshot.getChildren()){

                        Produto prod = ds.getValue(Produto.class);
                        deleteImageFromFirebaseStorage(prod.getPic(), "Produtos");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        refUser.child("produtos").child(userId).removeValue();
        refUser.child("users").child(userId).child("loja").setValue(false);
    }
}