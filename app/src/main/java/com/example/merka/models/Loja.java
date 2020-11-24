package com.example.merka.models;

import android.widget.EditText;

import com.google.firebase.database.IgnoreExtraProperties;

import static com.example.merka.utils.TextMethods.justNumbers;
import static com.example.merka.utils.TextMethods.returnFormatedText;

@IgnoreExtraProperties
public class Loja {

    protected String id;
    protected String nome;
    protected String contato;
    protected String endereco;
    protected String descricao;
    protected String delivery;
    protected String cpf;
    protected String pic;
    protected String responsavel;

    public Loja(){
        // Default constructor required for calls to DataSnapshot.getValue(Loja.class)
    }

    public Loja(String id, String nome, String contato, String endereco, String descricao, String delivery, String cpf, String pic, String responsavel){
        this.id=id;
        this.nome = nome;
        this.descricao=descricao;
        this.contato = contato;
        this.endereco= endereco;
        this.delivery=delivery;
        this.pic = pic;
        this.cpf = cpf;
        this.responsavel=responsavel;
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

    public String getContato() {
        return contato;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDelivery() {
        return delivery;
    }

    public void setDelivery(String delivery) {
        this.delivery = delivery;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setFields(EditText nome, EditText contato, EditText endereco, EditText descricao, EditText responsavel, EditText cpf){
        this.nome = returnFormatedText(nome.getEditableText().toString());
        this.contato = justNumbers(contato.getEditableText().toString());
        this.endereco = returnFormatedText(endereco.getEditableText().toString());
        this.descricao = returnFormatedText(descricao.getEditableText().toString());
        this.responsavel = returnFormatedText(responsavel.getEditableText().toString());
        this.cpf = justNumbers(cpf.getEditableText().toString());
    }
}
