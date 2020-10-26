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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class EditProdutoLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;

    private EditText txtEditNomePrduto;
    private EditText txtEditValorPrduto;
    private EditText txtEditDescricaoPrduto;

    private Button btnConfirmaEditProduto;
    private Button btnCancelaEditProduto;

    private StorageReference mStorageRef;
    private StorageTask uploadTask;

    private boolean picChanged = false;

    private String idProd;
    private String oldUrl;

    private ImageView pic;
    private Uri picUri;
    private Uri picUrl;

    private Intent i;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produto_loja);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Atualizando dados");

        pic = findViewById(R.id.picProduto);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos");

        txtEditNomePrduto = findViewById(R.id.txtEditNomeProduto);
        txtEditValorPrduto = findViewById(R.id.txtEditValorProduto);
        txtEditDescricaoPrduto = findViewById(R.id.txtEditDescricaoProduto);

        btnConfirmaEditProduto = findViewById(R.id.btnConfirmarEditProduto);
        btnConfirmaEditProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validar_e_confirmarAlteracao();
            }
        });

        btnCancelaEditProduto = findViewById(R.id.btnCancelarEditProduto);
        btnCancelaEditProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToProdutos();
            }
        });
    }

    private void choosePic(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,4);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            picUri = data.getData();
            pic.setImageURI(picUri);
            picChanged = true;
        }
    }

    private String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private boolean Fileuploader(){
        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(picUri));

        if (oldUrl.length() > 0) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(oldUrl);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // File not deleted
                }
            });
        }


        uploadTask = Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(EditProdutoLoja.this , "Dados do produto alterados!",Toast.LENGTH_LONG).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        picUrl = urlTask.getResult();

                        picChanged = false;
                        atualizarProduto();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(EditProdutoLoja.this , "Não foi possível fazer o upload da imagem",Toast.LENGTH_LONG).show();
                    }
                });
        return true;
    }

    private void validar_e_confirmarAlteracao(){

        final String nome = txtEditNomePrduto.getEditableText().toString();
        final String valor = retonaValorFormatado(txtEditValorPrduto.getEditableText().toString());
        final String desc = txtEditDescricaoPrduto.getEditableText().toString();

        if(validateFields(nome,valor,desc)){
            AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
            msgBox.setTitle("Alteração de dados");
            msgBox.setIcon(android.R.drawable.ic_menu_info_details);
            msgBox.setMessage("Deseja alterar os dados do produto?");
            msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    progressDialog.show();
                    if(picChanged) Fileuploader();
                    else atualizarProduto();
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

    private void atualizarProduto() {

        final String id = idProd;
        final String nome = retornaNomeFormatado(txtEditNomePrduto.getEditableText().toString());
        final String valor = retonaValorFormatado(txtEditValorPrduto.getEditableText().toString());
        final String desc = txtEditDescricaoPrduto.getEditableText().toString();
        final String url = (String.valueOf(picUrl).equals("")) ? oldUrl : String.valueOf(picUrl);

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = fbuser.getUid();

        Produto produto = new Produto(id,nome,valor,desc,url);

        refUser.child("produtos").child(userId).child(id).setValue(produto);

        progressDialog.dismiss();
        goToProdutos();
    }

    public String retornaNomeFormatado(String nome){
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }


    public String retonaValorFormatado(String valor){

        if(valor.indexOf('.') == -1){
            return valor + ",00";
        }
        return valor.substring(0, (valor.indexOf('.')+3)).replace('.',',');
    }

    public boolean validateFields(String nome, String valor,  String desc){
        return !nome.isEmpty() && !valor.isEmpty() && !desc.isEmpty();
    }


    private void goToProdutos() {
        startActivity(new Intent(EditProdutoLoja.this, ProdutosLoja.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        i = getIntent();
        idProd = i.getStringExtra("id");

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(firebaseAuth.getUid()).child(idProd);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(picUri==null){

                    Produto produto = snapshot.getValue(Produto.class);

                    txtEditNomePrduto.setText(produto.nome);
                    txtEditValorPrduto.setText(produto.valor.replace(',', '.'));
                    txtEditDescricaoPrduto.setText(produto.descricao);
                    oldUrl = produto.picUrl;
                    if(produto.picUrl.length() > 0) new EditLojaPerfil.DownloadImageTask((ImageView) pic).execute(produto.picUrl);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProdutoLoja.this, "Falha ao carregar dados do produto.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
        //  loadImage();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditProdutoLoja.this, ProdutosLoja.class));
        finish();
    }
}