package com.example.merka.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.merka.Models.Produto;
import com.example.merka.R;
import com.example.merka.Utils.DownloadImageTask;
import com.example.merka.Utils.PicMethods;
import com.example.merka.Utils.TextMethods;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    private boolean hasPicture = false;
    private boolean picChanged = false;

    private String idProd;
    private String oldUrl;

    private ImageView pic;
    private Uri picUri;
    private Uri picUrl;

    private Intent i;
    private int STORAGE_PERMISSION_CODE = 1;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produto_loja);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Atualizando dados");

        pic = findViewById(R.id.picProduto);
        pic.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if(hasPicture){
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(EditProdutoLoja.this);
                    msgBox.setTitle("Excluir imagem");
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage("Deseja retirar a imagem do produto?");
                    msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier("com.example.merka:drawable/store_icon", null, null));
                            picChanged = true;
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
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent= new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,4);
        }else{
            requestStoragePermition();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==4 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            try {
                Bitmap fotoBuscada = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                picUri = redimensionar_e_compressao(fotoBuscada);
                pic.setImageURI(picUri);
                hasPicture = true;
                picChanged = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void requestStoragePermition(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permissão necessária para inserir uma imagem")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditProdutoLoja.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissão aceita", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri redimensionar_e_compressao(Bitmap fotoBuscada){

        if (fotoBuscada.getWidth() >= fotoBuscada.getHeight()){

            fotoBuscada = Bitmap.createBitmap(
                    fotoBuscada,
                    fotoBuscada.getWidth()/2 - fotoBuscada.getHeight()/2,
                    0,
                    fotoBuscada.getHeight(),
                    fotoBuscada.getHeight()
            );

        }else{

            fotoBuscada = Bitmap.createBitmap(
                    fotoBuscada,
                    0,
                    fotoBuscada.getHeight()/2 - fotoBuscada.getWidth()/2,
                    fotoBuscada.getWidth(),
                    fotoBuscada.getWidth()
            );
        }

        Bitmap fotoRedimensionada = Bitmap.createScaledBitmap(fotoBuscada, 300, 300, true);

        return PicMethods.getImageUri(this, fotoRedimensionada, pic);
    }

    private String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
    private void fileDeleteFromFirebase(){

        if(oldUrl.length() > 0){
            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(oldUrl);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    oldUrl = "";
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    return;
                }
            });
        }
    }

    private boolean Fileuploader(){
        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(picUri));

        fileDeleteFromFirebase();

        uploadTask = Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Toast.makeText(EditProdutoLoja.this , "Dados do produto alterados!",Toast.LENGTH_LONG).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        picUrl = urlTask.getResult();

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

        final String nome = TextMethods.formatText(txtEditNomePrduto.getEditableText().toString());
        final String valor = TextMethods.retonarValorFormatado(txtEditValorPrduto.getEditableText().toString());
        final String desc = TextMethods.formatText(txtEditDescricaoPrduto.getEditableText().toString());

        if(validateFields(nome,valor,desc)){
            AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
            msgBox.setTitle("Alteração de dados");
            msgBox.setIcon(android.R.drawable.ic_menu_info_details);
            msgBox.setMessage("Deseja alterar os dados do produto?");
            msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    progressDialog.show();

                    if(picChanged) {
                        fileDeleteFromFirebase();

                        if(hasPicture) Fileuploader();
                        else atualizarProduto();
                    }
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
            Toast.makeText(EditProdutoLoja.this, getString(R.string.ToastPreenchaTodosCampos),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getFinalPictureUrl(){

        String url = "";

        if(hasPicture){
            if(oldUrl.length() > 0) url = oldUrl;
            else url = String.valueOf(picUrl);
        }
        return url;
    }

    private void atualizarProduto() {

        final String id = idProd;
        final String nome = TextMethods.formatText(txtEditNomePrduto.getEditableText().toString());
        final String valor = TextMethods.retonarValorFormatado(txtEditValorPrduto.getEditableText().toString());
        final String desc = TextMethods.formatText(txtEditDescricaoPrduto.getEditableText().toString());
        final String url = getFinalPictureUrl();

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = fbuser.getUid();

        Produto produto = new Produto(id,nome,valor,desc,url);

        refUser.child("produtos").child(userId).child(id).setValue(produto);

        goToProdutos();
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

                if(txtEditNomePrduto.getText().length() == 0) {

                    Produto produto = snapshot.getValue(Produto.class);

                    txtEditNomePrduto.setText(produto.nome);
                    txtEditValorPrduto.setText(produto.valor.replace(',', '.'));
                    txtEditDescricaoPrduto.setText(produto.descricao);
                    oldUrl = produto.picUrl;

                    if (oldUrl.length() > 0) {
                        new DownloadImageTask((ImageView) pic).execute(produto.picUrl);
                        hasPicture = true;
                    }
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