package com.example.merka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class AdicionaProduto extends AppCompatActivity {

    private Button btnAdicionaConfirma;
    private Button btnAdicionaCancela;

    private EditText txtAdicionaNomeProduto;
    private EditText txtAdicionaValorProduto;
    private EditText txtAdicionaDescricaoProduto;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adiciona_produto);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        btnAdicionaCancela = findViewById(R.id.btnCancelarAdicionarProduto);
        btnAdicionaCancela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdicionaProduto.this,ProdutosLoja.class));
            }
        });

        btnAdicionaConfirma = findViewById(R.id.btnConfirmarAdicionarProduto);
        btnAdicionaConfirma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adicionaProduto(view);
            }
        });

        txtAdicionaNomeProduto = findViewById(R.id.txtAdicionaNomeProduto);
        txtAdicionaValorProduto = findViewById(R.id.txtAdicionaValorProduto);
        txtAdicionaDescricaoProduto = findViewById(R.id.txtAdicionaDescricaoProduto);
    }

    private void adicionaProduto(View view) {
        final String nome = txtAdicionaNomeProduto.getEditableText().toString();
        final String valor = txtAdicionaValorProduto.getEditableText().toString();
        final String desc = txtAdicionaDescricaoProduto.getEditableText().toString();

        if(validateFields(nome,valor, desc)){
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String userId = user.getUid();

            writeNewProduto(userId, nome, valor, desc);
        }
        else{
            Toast.makeText(AdicionaProduto.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validateFields(String nome, String valor,  String desc){
        if(nome.isEmpty() || valor.isEmpty() || desc.isEmpty()){
            return false;
        }
        else{
            return true;
        }
        //return !nome.isEmpty() && !valor.isEmpty() && !desc.isEmpty();
    }
    private void writeNewProduto(String userId, String nome, String valor, String descricao) {
        //usando o mesmo UID do Firebase Authentication: userId

        Random rand = new Random();
        int id = rand.nextInt(10000)+1;
        String idProd = String.valueOf(id);

        try {//tentando cadastrar no banco
            Produto produto = new Produto(idProd,nome,valor, descricao);

            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("produtos").child(userId).child(idProd).setValue(produto);
            // uploadPic();
            goToProdutos();


        } catch (DatabaseException e) {
            Toast.makeText(AdicionaProduto.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void goToProdutos(){
        startActivity(new Intent(AdicionaProduto.this, ProdutosLoja.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AdicionaProduto.this, ProdutosLoja.class));
        finish();;
    }
}