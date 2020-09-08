package com.example.merka;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Loja {

    public String nome;
    public String descricao;
    public String contato;
    public String endereco;

    public Loja(){
        // Default constructor required for calls to DataSnapshot.getValue(Loja.class)
    }
    public Loja(String nome, String descricao, String contato, String endereco){
        this.nome = nome;
        this.descricao=descricao;
        this.contato = contato;
        this.endereco= endereco;
    }


}
