package com.example.merka.Utils;

import android.content.Context;

import com.example.merka.Models.Loja;
import com.example.merka.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class FirebaseMethods {

    private FirebaseAuth mAuth;
    private Context mContext;
    private String userID;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public static boolean checkCPFExists(String cpf, DataSnapshot datasnapshot){

        Loja loja = new Loja();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setCpf(ds.getValue(Loja.class).getCpf());

            if(loja.getCpf().equals(cpf)) return true;

        }
        return false;
    }

    public static boolean checkCPFExists(String cpf, DataSnapshot datasnapshot, String userId){

        Loja loja = new Loja();

        if(cpf.equals(datasnapshot.child(userId).child("cpf").getValue().toString())) return false;

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setCpf(ds.getValue(Loja.class).getCpf());

            if(loja.getCpf().equals(cpf)) return true;
        }
        return false;
    }

    public static boolean checkContatoExists(String contato, DataSnapshot datasnapshot, String userId){

        Loja loja = new Loja();

        if(contato.equals(datasnapshot.child(userId).child("contato").getValue().toString())) return false;

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setContato(ds.getValue(Loja.class).getContato());

            if(loja.getContato().equals(contato)) return true;
        }
        return false;
    }

    public static boolean checkContatoExists(String contato, DataSnapshot datasnapshot){

        Loja loja = new Loja();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setContato(ds.getValue(Loja.class).getContato());

            if(loja.getContato().equals(contato)) return true;
        }
        return false;
    }

    public static boolean checkEmailExists(String email, DataSnapshot datasnapshot, String userId){

        User user = new User();

        if(email.equals(datasnapshot.child(userId).child("email").getValue().toString())) return false;

        for (DataSnapshot ds: datasnapshot.getChildren()){

            user.setEmail(ds.getValue(User.class).getEmail());

            if(user.getEmail().equals(email)) return true;
        }
        return false;
    }
    public static boolean checkEmailExists(String email, DataSnapshot datasnapshot, Boolean idf){

        if(idf) return false;

        User user = new User();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            user.setEmail(ds.getValue(User.class).getEmail());

            if(user.getEmail().equals(email)) return true;
        }
        return false;
    }


    public static boolean checkEmailExists(String email, DataSnapshot datasnapshot){

        User user = new User();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            user.setEmail(ds.getValue(User.class).getEmail());

            if(user.getEmail().equals(email)) return true;
        }
        return false;
    }

}