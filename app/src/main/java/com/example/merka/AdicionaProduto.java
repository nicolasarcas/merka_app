package com.example.merka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.Random;

public class AdicionaProduto extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;

    private Button btnAdicionaConfirma;
    private Button btnAdicionaCancela;

    private EditText txtAdicionaNomeProduto;
    private EditText txtAdicionaValorProduto;
    private EditText txtAdicionaDescricaoProduto;

    private StorageReference mStorageRef;
    private StorageTask uploadTask;

    private boolean hasPicture = false;

    private ProgressDialog progressDialog;

    private ImageView pic;
    private Uri picUri;
    private Uri picUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adiciona_produto);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Atualizando dados");

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");

        txtAdicionaNomeProduto = findViewById(R.id.txtAdicionaNomeProduto);
        txtAdicionaValorProduto = findViewById(R.id.txtAdicionaValorProduto);
        txtAdicionaDescricaoProduto = findViewById(R.id.txtAdicionaDescricaoProduto);

        pic = findViewById(R.id.picProduto);
        pic.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if(hasPicture){
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(AdicionaProduto.this);
                    msgBox.setTitle("Excluir imagem");
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage("Deseja retirar a imagem do produto?");
                    msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier("com.example.merka:drawable/store_icon", null, null));
                            hasPicture = false;
                        }
                    });
                    msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    msgBox.show();
                }

                return true;
            }
        });
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });

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

                validar_e_confirmarAlteracao();
            }
        });
    }

    private void validar_e_confirmarAlteracao(){

        String nome = txtAdicionaNomeProduto.getEditableText().toString();
        String valor = txtAdicionaValorProduto.getEditableText().toString();
        String desc = txtAdicionaDescricaoProduto.getEditableText().toString();

        if(validateFields(nome, valor, desc)){
            if(valorValido(valor)){

                progressDialog.show();

                if(hasPicture) Fileuploader();
                else adicionaProduto();
            }
            else Toast.makeText(AdicionaProduto.this, getString(R.string.ToastDigiteValorValido), Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(AdicionaProduto.this, getString(R.string.ToastPreenchaTodosCampos), Toast.LENGTH_SHORT).show();

    }

    public boolean validateFields(String nome, String valor, String desc){
        return !nome.isEmpty() && !valor.isEmpty() && !desc.isEmpty();
    }

    public boolean valorValido(String valor){
        return valor.replace(".", "").length() > 0;
    }

    private void adicionaProduto() {

        final String nome = retornaNomeFormatado(txtAdicionaNomeProduto.getEditableText().toString());
        final String valor = retonarValorFormatado(txtAdicionaValorProduto.getEditableText().toString());
        final String desc = txtAdicionaDescricaoProduto.getEditableText().toString();
        final String url = (hasPicture) ? String.valueOf(picUrl) : "";

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        writeNewProduto(userId, nome, valor, desc, url);
    }

    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(picUri));

        uploadTask = Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        picUrl = urlTask.getResult();

                        adicionaProduto();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(AdicionaProduto.this , "Não foi possível fazer o upload da imagem",Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void choosePic(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            picUri = data.getData();
            pic.setImageURI(picUri);
            hasPicture = true;
        }
    }

    private String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    public String retonarValorFormatado(String valor){

        if(valor.indexOf(".") == 0 || valor.indexOf(".") == valor.length() -1){
            valor = valor.replace(".", "");
        }

        int index = valor.indexOf(".");

        if (index == -1) return valor + ",00";
        else if(index == valor.length()-2) valor += "0";

        return valor.replace('.', ',');
    }

    public String retornaNomeFormatado(String nome){
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    private void writeNewProduto(String userId, String nome, String valor, String descricao, String url) {
        //usando o mesmo UID do Firebase Authentication: userId

        Random rand = new Random();
        int id = rand.nextInt(10000)+1;
        String idProd = String.valueOf(id);

        try {//tentando cadastrar no banco
            Produto produto = new Produto(idProd,nome,valor, descricao, url);

            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("produtos").child(userId).child(idProd).setValue(produto);
            progressDialog.dismiss();
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
        finish();
    }
}