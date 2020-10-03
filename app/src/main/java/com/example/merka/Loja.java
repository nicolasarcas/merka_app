package com.example.merka;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Loja {


    public String nome;
    public String contato;
    public String endereco;
    public String descricao;
    public String delivery;
    public String cpf;

    public Loja(){
        // Default constructor required for calls to DataSnapshot.getValue(Loja.class)
    }
    public Loja(String nome, String contato, String endereco, String descricao,String delivery, String cpf){
        this.nome = nome;
        this.descricao=descricao;
        this.contato = contato;
        this.endereco= endereco;
        this.delivery=delivery;
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
