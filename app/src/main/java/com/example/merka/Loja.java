package com.example.merka;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Loja {

    public String nome;
    public String contato;
    public String endereco;
    public String descricao;
    public String delivery;

    public Loja(){
        // Default constructor required for calls to DataSnapshot.getValue(Loja.class)
    }
    public Loja(String nome, String contato, String endereco, String descricao,String delivery){
        this.nome = nome;
        this.descricao=descricao;
        this.contato = contato;
        this.endereco= endereco;
        this.delivery=delivery;
    }


}
