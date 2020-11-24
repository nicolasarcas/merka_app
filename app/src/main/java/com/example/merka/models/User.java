package com.example.merka.models;

import android.text.BoringLayout;
import android.widget.EditText;

import com.example.merka.utils.TextMethods;
import com.google.firebase.database.IgnoreExtraProperties;

import static com.example.merka.utils.TextMethods.returnFormatedText;

@IgnoreExtraProperties
public class User {

    protected String nome;
    protected String email;
    protected String password;
    protected boolean loja;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String nome, String email, String password, boolean loja) {
        this.nome = nome;
        this.email = email;
        this.password = password;
        this.loja = loja;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setLoja(boolean loja) {
        this.loja = loja;
    }

    public Boolean getLoja() {
        return loja;
    }

    public void setFields(EditText nome, EditText email, EditText pass, Boolean loja){

        this.nome = returnFormatedText(nome.getEditableText().toString());
        this.email = email.getEditableText().toString().trim().toLowerCase();
        this.password = pass.getEditableText().toString();
        this.loja = loja;
    }

}
