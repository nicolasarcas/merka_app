package com.example.merka;

public class Produto {

    public String id;
    public String nome;
    public String valor;
    public String descricao;

    public Produto(){}

    public Produto(String id,String nome, String valor, String descricao){
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.descricao = descricao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

//android:contentDeion="@string/ImageViewContentDeion

