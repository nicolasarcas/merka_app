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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditLojaPerfil extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;

    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtDescricaoLoja;

    private Button btnConfirmar;
    private Button btnCancelar;
    private Button btnExcluir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_loja_perfil);

        firebaseAuth= FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        txtNomeLoja = findViewById(R.id.txtEditNomeLoja);
        txtContatoLoja = findViewById(R.id.txtEditContatoLoja);
        txtEnderecoLoja = findViewById(R.id.txtEditEnderecoLoja);
        txtDescricaoLoja = findViewById(R.id.txtEditDescricaoLoja);

        btnConfirmar = findViewById(R.id.btnConfirmarAlteracaoLoja);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarLoja();
            }
        });

        btnCancelar = findViewById(R.id.btnCancelarAlteracaoLoja);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditLojaPerfil.this, Tela_Inicial.class));
            }
        });

        btnExcluir=findViewById(R.id.btnExcluirLoja);
        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarExclusao();
            }
        });
    }
    private void atualizarLoja(){
        final String nome = txtNomeLoja.getEditableText().toString();
        final String contato = txtContatoLoja.getEditableText().toString();
        final String endereco = txtEnderecoLoja.getEditableText().toString();
        final String descricao = txtDescricaoLoja.getEditableText().toString();

        if(validateFields(nome,contato,endereco)){
            AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
            msgBox.setTitle("Alteração de dados");
            msgBox.setIcon(android.R.drawable.ic_menu_info_details);
            msgBox.setMessage("Deseja alterar os dados da sua loja?");
            msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
                    FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                    String userId = fbuser.getUid();

                    Loja loja = new Loja(nome, contato, endereco, descricao);

                    refUser.child("lojas").child(userId).setValue(loja);

                    goToLoja();
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
            Toast.makeText(EditLojaPerfil.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void goToLoja(){
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();
    }
    public boolean validateFields(String nome, String contato, String endereco){
        if(nome.isEmpty() || contato.isEmpty() || endereco.isEmpty()){
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
        msgBox.setMessage("Deseja mesmo excluir sua loja?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteUserData();
                goToMenu();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        msgBox.show();
    }


    private void goToMenu(){
        startActivity(new Intent(EditLojaPerfil.this,Tela_Inicial.class));
        finish();
    }

    public void deleteUserData(){//deletar dados do usuário do banco de dados
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();
        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("lojas").child(userId).removeValue();
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(firebaseAuth.getUid());

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Loja loja = dataSnapshot.getValue(Loja.class);

                txtNomeLoja.setText(loja.nome);
                txtContatoLoja.setText(loja.contato);
                txtEnderecoLoja.setText(loja.endereco);
                txtDescricaoLoja.setText(loja.descricao);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditLojaPerfil.this, "Falha ao carregar dados da loja.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }
}