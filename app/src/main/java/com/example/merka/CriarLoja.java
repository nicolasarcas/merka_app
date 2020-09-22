package com.example.merka;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CriarLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtEnderecoLoja;

    private Button btnCriarLoja;
    private Button btnCancelarCriarLoja;

    private ImageView pic;
    public Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_loja);

        firebaseAuth = FirebaseAuth.getInstance();

        pic = findViewById(R.id.profile_image);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });

        txtNomeLoja=findViewById(R.id.txtNomeLoja);
        txtContatoLoja=findViewById(R.id.txtContatoLoja);
        txtEnderecoLoja=findViewById(R.id.txtEnderecoLoja);
        txtDescricaoLoja=findViewById(R.id.txtDescricaoLoja);

        btnCriarLoja=findViewById(R.id.btnConfirmarCriarLoja);
        btnCancelarCriarLoja=findViewById(R.id.btnCancelarCriarLoja);

        btnCriarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criarLoja(view);
            }
        });

    }

    private void criarLoja(View view){
        final String nome = txtNomeLoja.getEditableText().toString();
        final String contato = txtContatoLoja.getEditableText().toString();
        final String endereco = txtEnderecoLoja.getEditableText().toString();
        final String descricao = txtDescricaoLoja.getEditableText().toString();

        if(validateFields(nome,contato,endereco)){
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String userId = user.getUid();
            writeNewLoja(userId, nome, contato, endereco, descricao);

        }
        else{
            Toast.makeText(CriarLoja.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void goToLoja(){
        startActivity(new Intent(CriarLoja.this, PerfilLoja.class));
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

    private void writeNewLoja(String userId, String nome, String contato, String endereco, String descricao) {
        //usando o mesmo UID do Firebase Authentication: userId

        try {//tentando cadastrar no banco
            Loja loja = new Loja(nome, contato, endereco, descricao);
            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("lojas").child(userId).setValue(loja);
            goToLoja();

        } catch (DatabaseException e) {
                      Toast.makeText(CriarLoja.this, e.getMessage(),
                             Toast.LENGTH_SHORT).show();
        }
    }
    private void choosePic(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            picUri = data.getData();
            pic.setImageURI(picUri);
        }
    }
}