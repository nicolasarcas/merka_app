package com.example.merka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

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

    private FirebaseUser user;
    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase
    private ValueEventListener userListener;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_loja);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

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

        btnCancelarCriarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CriarLoja.this, Tela_Inicial.class));
            }
        });
    }

    private void criarLoja(View view){
        final String nome = txtNomeLoja.getEditableText().toString();
        final String contato = txtContatoLoja.getEditableText().toString();
        final String endereco = txtEnderecoLoja.getEditableText().toString();
        final String descricao = txtDescricaoLoja.getEditableText().toString();

        if(validateFields(nome,contato,endereco,descricao)){
            FirebaseUser user = firebaseAuth.getCurrentUser();
            String userId = user.getUid();

            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("users").child(userId).child("store").setValue(true);

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

    public boolean validateFields(String nome, String contato, String endereco, String desc){
        if(nome.isEmpty() || contato.isEmpty() || endereco.isEmpty() || desc.isEmpty()){
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
            uploadPic();
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

    private void uploadPic(){

        final String randomKey = UUID.randomUUID().toString();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        StorageReference riversRef = storageReference.child(userId).child("images/"+randomKey);

        riversRef.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(findViewById(android.R.id.content),"Dados gravados", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Falha ao gravar dados", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}