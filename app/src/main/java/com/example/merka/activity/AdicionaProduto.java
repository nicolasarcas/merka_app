package com.example.merka.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.merka.models.Produto;
import com.example.merka.R;
import com.example.merka.utils.PicMethods;
import com.example.merka.utils.TextMethods;
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
import java.util.Random;

public class AdicionaProduto extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText txtAdicionaNomeProduto;
    private EditText txtAdicionaValorProduto;
    private EditText txtAdicionaDescricaoProduto;

    private StorageReference mStorageRef;

    private boolean hasPicture = false;

    private final int STORAGE_PERMISSION_CODE = 1;

    private ProgressDialog progressDialog;

    private ImageView pic;
    private Uri picUri;
    private String picName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adiciona_produto);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogAtualizandoDados));

        firebaseAuth = FirebaseAuth.getInstance();
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
                    msgBox.setTitle(getString(R.string.msgBoxTitleExcluirImagem));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaRetirarImagem));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier("com.example.merka:drawable/store_icon", null, null));
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
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePic();
            }
        });

        Button btnAdicionaCancela = findViewById(R.id.btnCancelarAdicionarProduto);
        btnAdicionaCancela.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdicionaProduto.this, ProdutosLoja.class));
            }
        });

        Button btnAdicionaConfirma = findViewById(R.id.btnConfirmarAdicionarProduto);
        btnAdicionaConfirma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                validar_e_confirmarAlteracao();
            }
        });
    }

    private void validar_e_confirmarAlteracao(){

        String nome = TextMethods.formatText(txtAdicionaNomeProduto.getEditableText().toString());
        String valor = TextMethods.retonarValorFormatado(txtAdicionaValorProduto.getEditableText().toString());
        String desc = TextMethods.formatText(txtAdicionaDescricaoProduto.getEditableText().toString());

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

        final String nome = TextMethods.formatText(txtAdicionaNomeProduto.getEditableText().toString());
        final String valor = TextMethods.retonarValorFormatado(txtAdicionaValorProduto.getEditableText().toString());
        final String desc = TextMethods.formatText(txtAdicionaDescricaoProduto.getEditableText().toString());
        final String url = (hasPicture) ? picName : "";

        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();

        writeNewProduto(userId, nome, valor, desc, url);
    }

    private String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }


    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(picUri));

        Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful()){}

                        if (urlTask.isSuccessful()){
                            picName = taskSnapshot.getStorage().getName();
                            adicionaProduto();
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

    private void choosePic(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 2);
        }else{
            requestStoragePermition();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            try {
                Bitmap fotoBuscada = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                picUri = redimensionar_e_compressao(fotoBuscada);
                pic.setImageURI(picUri);
                hasPicture = true;

            } catch (IOException e) {
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
                            ActivityCompat.requestPermissions(AdicionaProduto.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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
                Toast.makeText(this, getString(R.string.permissaoAceita), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, getString(R.string.permissaoNegada), Toast.LENGTH_SHORT).show();
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