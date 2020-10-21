package com.example.merka;

import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Loja {

    public String id;
    public String nome;
    public String contato;
    public String endereco;
    public String descricao;
    public String delivery;
    public String cpf;
    public String PicUrl;

    public Loja(){
        // Default constructor required for calls to DataSnapshot.getValue(Loja.class)
    }
    public Loja(String id, String nome, String contato, String endereco, String descricao,String delivery, String cpf, String PicUrl){
        this.id=id;
        this.nome = nome;
        this.descricao=descricao;
        this.contato = contato;
        this.endereco= endereco;
        this.delivery=delivery;
        this.PicUrl = PicUrl;
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getContato() {
        return contato;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getDescricao() {
        return descricao;
    }

}
