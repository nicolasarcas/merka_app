package com.example.merka.utils;

import com.example.merka.models.Loja;
import com.example.merka.models.User;
import com.google.firebase.database.DataSnapshot;

import java.util.Objects;

public class FirebaseMethods {

    public static boolean checkCPFExists(String cpf, DataSnapshot datasnapshot){

        Loja loja = new Loja();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setCpf(Objects.requireNonNull(ds.getValue(Loja.class)).getCpf());

            if(loja.getCpf().equals(cpf)) return true;

        }
        return false;
    }

    public static boolean checkCPFExists(String cpf, DataSnapshot datasnapshot, String userId){

        Loja loja = new Loja();

        if(cpf.equals(Objects.requireNonNull(datasnapshot.child(userId).child("cpf").getValue()).toString())) return false;

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setCpf(Objects.requireNonNull(ds.getValue(Loja.class)).getCpf());

            if(loja.getCpf().equals(cpf)) return true;
        }
        return false;
    }

    public static boolean checkContatoExists(String contato, DataSnapshot datasnapshot, String userId){

        Loja loja = new Loja();

        if(contato.equals(Objects.requireNonNull(datasnapshot.child(userId).child("contato").getValue()).toString())) return false;

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setContato(Objects.requireNonNull(ds.getValue(Loja.class)).getContato());

            if(loja.getContato().equals(contato)) return true;
        }
        return false;
    }

    public static boolean checkContatoExists(String contato, DataSnapshot datasnapshot){

        Loja loja = new Loja();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            loja.setContato(Objects.requireNonNull(ds.getValue(Loja.class)).getContato());

            if(loja.getContato().equals(contato)) return true;
        }
        return false;
    }

    public static boolean checkEmailExists(String email, DataSnapshot datasnapshot, String userId){

        User user = new User();

        if(email.equals(Objects.requireNonNull(datasnapshot.child(userId).child("email").getValue()).toString())) return false;

        for (DataSnapshot ds: datasnapshot.getChildren()){

            user.setEmail(Objects.requireNonNull(ds.getValue(User.class)).getEmail());

            if(user.getEmail().equals(email)) return true;
        }
        return false;
    }
    public static boolean checkEmailExists(String email, DataSnapshot datasnapshot, Boolean idf){

        if(idf) return false;

        User user = new User();

        for (DataSnapshot ds: datasnapshot.getChildren()){

            user.setEmail(Objects.requireNonNull(ds.getValue(User.class)).getEmail());

            if(user.getEmail().equals(email)) return true;
        }
        return false;
    }
}