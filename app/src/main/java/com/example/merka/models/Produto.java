package com.example.merka.models;

public class Produto {

    public String id;
    public String nome;
    public String valor;
    public String pic;
    public String descricao;

    public Produto(){}

    public Produto(String id,String nome, String valor, String descricao, String pic){
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.pic = pic;
        this.descricao = descricao;
    }
}

