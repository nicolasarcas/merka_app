package com.example.merka.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

public class EditProdutoLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;

    private EditText txtEditNomePrduto;
    private EditText txtEditValorPrduto;
    private EditText txtEditDescricaoPrduto;

    private StorageReference mStorageRef;

    private boolean hasPicture = false;
    private boolean picChanged = false;

    private String idProd;
    private String oldName;

    private ImageView pic;
    private Uri picUri;
    private String picName;

    private final int STORAGE_PERMISSION_CODE = 1;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produto_loja);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogAtualizandoDados));

        pic = findViewById(R.id.picProduto);
        pic.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if(hasPicture){
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(EditProdutoLoja.this);
                    msgBox.setTitle(getString(R.string.msgBoxTitleExcluir));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaRetirarImagem));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier("com.example.merka:drawable/store_icon", null, null));
                            picChanged = true;
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

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos");

        txtEditNomePrduto = findViewById(R.id.txtEditNomeProduto);
        txtEditValorPrduto = findViewById(R.id.txtEditValorProduto);
        txtEditDescricaoPrduto = findViewById(R.id.txtEditDescricaoProduto);

        Button btnConfirmaEditProduto = findViewById(R.id.btnConfirmarEditProduto);
        btnConfirmaEditProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validar_e_confirmarAlteracao();
            }
        });

        Button btnCancelaEditProduto = findViewById(R.id.btnCancelarEditProduto);
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
                    .setTitle(getString(R.string.AlertDialogTitlePermissaoNecessariaImagem))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
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

    private String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
    private void fileDeleteFromFirebase(){

        if(oldName.length() > 0){
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Images").child("Produtos").child(oldName);

            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    oldName = "";
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            });
        }
    }

    private void Fileuploader(){
        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(picUri));

        fileDeleteFromFirebase();

        Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful()){}

                        if (urlTask.isSuccessful()) {

                            Toast.makeText(EditProdutoLoja.this, getString(R.string.ToastDadosDoProdutoAlterados), Toast.LENGTH_LONG).show();
                            picName = taskSnapshot.getStorage().getName();
                            atualizarProduto();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(EditProdutoLoja.this, getString(R.string.ToastUploadImagemNaoFoiPossivel), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void validar_e_confirmarAlteracao(){

        final String nome = TextMethods.formatText(txtEditNomePrduto.getEditableText().toString());
        final String valor = TextMethods.retonarValorFormatado(txtEditValorPrduto.getEditableText().toString());
        final String desc = TextMethods.formatText(txtEditDescricaoPrduto.getEditableText().toString());

        if(validateFields(nome,valor,desc)){
            AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
            msgBox.setTitle(getString(R.string.msgBoxTitleAlteraçãoDeDados));
            msgBox.setIcon(android.R.drawable.ic_menu_info_details);
            msgBox.setMessage(getString(R.string.msgBoxMessageDesejaAlterarDadosDoProduto));
            msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
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
            msgBox.setNegativeButton(getString(R.string.nao), new DialogInterface.OnClickListener() {
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
            if(oldName.length() > 0) url = oldName;
            else url = picName;
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
        String userId = Objects.requireNonNull(fbuser).getUid();

        Produto produto = new Produto(id,nome,valor,desc,url);

        refUser.child("produtos").child(userId).child(id).setValue(produto);

        progressDialog.dismiss();

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

        Intent i = getIntent();
        idProd = i.getStringExtra("id");

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(Objects.requireNonNull(firebaseAuth.getUid())).child(idProd);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (txtEditNomePrduto.getText().length() == 0) {

                    Produto produto = snapshot.getValue(Produto.class);

                    txtEditNomePrduto.setText(Objects.requireNonNull(produto).nome);
                    txtEditValorPrduto.setText(produto.valor.replace(',', '.'));
                    txtEditDescricaoPrduto.setText(produto.descricao);
                    oldName = produto.pic;

                    if (oldName.length() > 0) {

                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference imageRef = storage.getReference()
                                .child("Images").child("Produtos").child(oldName);

                        imageRef.getBytes(1024*1024)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        pic.setImageBitmap(bitmap);
                                    }
                                });
                        hasPicture = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProdutoLoja.this, getString(R.string.ToastErroAoCarregarDadosProduto), Toast.LENGTH_SHORT).show();
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