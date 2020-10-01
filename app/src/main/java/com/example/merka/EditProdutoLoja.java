package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProdutoLoja extends AppCompatActivity {

    private EditText txtEditNomePrduto;
    private EditText txtEditValorPrduto;
    private EditText txtEditDescricaoPrduto;

    private Button btnConfirmaEditProduto;
    private Button btnCancelaEditProduto;
    private Button btnExlcuiProduto;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produto_loja);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos");

        txtEditNomePrduto = findViewById(R.id.txtEditNomeProduto);
        txtEditValorPrduto = findViewById(R.id.txtEditValorProduto);
        txtEditDescricaoPrduto = findViewById(R.id.txtEditDescricaoProduto);

        btnConfirmaEditProduto = findViewById(R.id.btnConfirmarEditProduto);
        btnConfirmaEditProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarProduto(view);
            }
        });

        btnCancelaEditProduto = findViewById(R.id.btnCancelarEditProduto);
        btnCancelaEditProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProdutos();
            }
        });

        btnExlcuiProduto = findViewById(R.id.btnExcluirProdutoLoja);
        btnExlcuiProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarExclusao();
            }
        });
    }

    private void atualizarProduto(View view) {
        final String nome = txtEditNomePrduto.getEditableText().toString();
        final String valor = txtEditValorPrduto.getEditableText().toString();
        final String desc = txtEditDescricaoPrduto.getEditableText().toString();

        if(validateFields(nome,valor,desc)){
            AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
            msgBox.setTitle("Alteração de dados");
            msgBox.setIcon(android.R.drawable.ic_menu_info_details);
            msgBox.setMessage("Deseja alterar os dados do produto?");
            msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
                    FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = fbuser.getUid();

                    Produto produto = new Produto();

                    refUser.child("produtos").child(userId).child(txtEditNomePrduto.getEditableText().toString()).setValue(produto);
                    //  uploadPic();

                    goToProdutos();
                }
            });
            msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            msgBox.show();
        }
        else{
            Toast.makeText(EditProdutoLoja.this, getString(R.string.empty_fields_warning),
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
    }

    private void confirmarExclusao(){
        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle("Excluir");
        msgBox.setIcon(android.R.drawable.ic_menu_delete);
        msgBox.setMessage("Deseja mesmo excluir este produto?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteProdutoData();
                goToProdutos();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        msgBox.show();
    }
    public void deleteProdutoData(){//deletar dados do usuário do banco de dados
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("produtos").child(userId).child(txtEditNomePrduto.getEditableText().toString()).removeValue();
    }

    private void goToProdutos() {
        startActivity(new Intent(EditProdutoLoja.this, ProdutosLoja.class));
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(firebaseAuth.getUid());

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Produto produto = snapshot.getValue(Produto.class);

                txtEditNomePrduto.setText(produto.nome);
                txtEditValorPrduto.setText(produto.valor);
                txtEditDescricaoPrduto.setText(produto.descricao);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProdutoLoja.this, "Falha ao carregar dados da loja.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
        //  loadImage();
    }
}