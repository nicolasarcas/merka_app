package com.example.merka.models;

import android.widget.EditText;

import static com.example.merka.utils.TextMethods.returnFormatedText;
import static com.example.merka.utils.TextMethods.returnFormatedValue;

public class Produto {

    protected String id;
    protected String nome;
    protected String valor;
    protected String pic;
    protected String descricao;

    public Produto(){}

    public Produto(String id,String nome, String valor, String descricao, String pic){
        this.id = id;
        this.nome = nome;
        this.valor = valor;
        this.pic = pic;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setFields(EditText nome, EditText valor, EditText descricao){

        this.nome = returnFormatedText(nome.getEditableText().toString());
        this.valor = returnFormatedValue(valor.getEditableText().toString());
        this.descricao = returnFormatedText(descricao.getEditableText().toString());
    }
}

