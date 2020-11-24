package com.example.merka.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.merka.models.Produto;
import com.example.merka.R;
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
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static com.example.merka.utils.PicMethods.getExtension;
import static com.example.merka.utils.PicMethods.getImageCompressed;
import static com.example.merka.utils.PicMethods.resize;
import static com.example.merka.utils.TextMethods.returnFormatedText;
import static com.example.merka.utils.TextMethods.returnFormatedValue;
import static com.example.merka.utils.TextMethods.validProductFields;

public class AdicionaProduto extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    private ImageView pic;
    private EditText txtAdicionaNomeProduto;
    private EditText txtAdicionaValorProduto;
    private EditText txtAdicionaDescricaoProduto;

    final Produto prod = new Produto();
    private String picName;
    private Uri picUri;

    private boolean hasPicture = false;

    private final int STORAGE_REQUEST_CODE = 1;
    private final int IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adiciona_produto);

        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogAtualizandoDados));

        txtAdicionaNomeProduto = findViewById(R.id.txtAdicionaNomeProduto);
        txtAdicionaValorProduto = findViewById(R.id.txtAdicionaValorProduto);
        txtAdicionaDescricaoProduto = findViewById(R.id.txtAdicionaDescricaoProduto);

        pic = findViewById(R.id.picProduto);
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });
        pic.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if(hasPicture){
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(AdicionaProduto.this);
                    msgBox.setTitle(getString(R.string.msgBoxTitleExcluirImagem));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaRetirarImagem));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier(getString(R.string.DefaultProductImage), null, null));
                            hasPicture = false;
                        }
                    });
                    msgBox.setNegativeButton(getString(R.string.nao), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    msgBox.show();
                }
                return true;
            }
        });

        Button btnAdicionaConfirma = findViewById(R.id.btnConfirmarAdicionarProduto);
        btnAdicionaConfirma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prod.setFields(txtAdicionaNomeProduto, txtAdicionaValorProduto, txtAdicionaDescricaoProduto);

                if(validProductFields(AdicionaProduto.this, prod)){

                    progressDialog.show();
                    if(hasPicture) Fileuploader();
                    else writeNewProduto();
                }
            }
        });
        Button btnAdicionaCancela = findViewById(R.id.btnCancelarAdicionarProduto);
        btnAdicionaCancela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdicionaProduto.this, ProdutosLoja.class));
            }
        });
    }

    private void choosePic(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, IMAGE_REQUEST_CODE);
        }
        else requestStoragePermition();
    }

    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(this, picUri));

        Ref.putFile(picUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()){}

                        if (urlTask.isSuccessful()){
                            picName = taskSnapshot.getStorage().getName();
                            writeNewProduto();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(AdicionaProduto.this, getString(R.string.ToastUploadImagemNaoFoiPossivel), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void writeNewProduto() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = Objects.requireNonNull(user).getUid();

        //usando o mesmo UID do Firebase Authentication: userId
        Random rand = new Random();
        int id = rand.nextInt(10000)+1;

        prod.setId(String.valueOf(id));
        prod.setNome(returnFormatedText(txtAdicionaNomeProduto.getEditableText().toString()));
        prod.setValor(returnFormatedValue(txtAdicionaValorProduto.getEditableText().toString()));
        prod.setDescricao(returnFormatedText(txtAdicionaDescricaoProduto.getEditableText().toString()));
        prod.setPic((hasPicture) ? picName : "");

        try {//tentando cadastrar no banco
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("produtos").child(userId).child(prod.getId()).setValue(prod);
            goToProdutos();

        } catch (DatabaseException e) {
            Toast.makeText(AdicionaProduto.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== IMAGE_REQUEST_CODE && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            try {
                Bitmap foto = resize(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                picUri = getImageCompressed(this, foto);
                pic.setImageURI(picUri);
                hasPicture = true;

            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void requestStoragePermition(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.AlertDialogTitlePermissaoNecessariaImagem))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(AdicionaProduto.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        }
                    })
                    .create().show();
        }
        else ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == STORAGE_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, getString(R.string.permissaoAceita), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, getString(R.string.permissaoNegada), Toast.LENGTH_SHORT).show();
            }
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