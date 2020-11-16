package com.example.merka.Models;

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
    public String responsavel;

    public Loja(){
        // Default constructor required for calls to DataSnapshot.getValue(Loja.class)
    }

    public Loja(String id, String nome, String contato, String endereco, String descricao, String delivery, String cpf, String PicUrl, String responsavel){
        this.id=id;
        this.nome = nome;
        this.descricao=descricao;
        this.contato = contato;
        this.endereco= endereco;
        this.delivery=delivery;
        this.PicUrl = PicUrl;
        this.cpf = cpf;
        this.responsavel=responsavel;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

}
