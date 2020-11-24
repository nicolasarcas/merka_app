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

import static com.example.merka.utils.PicMethods.deleteImageFromFirebaseStorage;
import static com.example.merka.utils.PicMethods.getExtension;
import static com.example.merka.utils.PicMethods.getImageCompressed;
import static com.example.merka.utils.PicMethods.loadPic;
import static com.example.merka.utils.PicMethods.resize;
import static com.example.merka.utils.TextMethods.validProductFields;

public class EditProdutoLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    private ImageView pic;
    private EditText txtEditNomeProduto;
    private EditText txtEditValorProduto;
    private EditText txtEditDescricaoProduto;

    final Produto prod = new Produto();
    private String picOldName;
    private String picName;
    private Uri picUri;

    private boolean hasPicture = false;
    private boolean picChanged = false;

    private final int STORAGE_REQUEST_CODE = 1;
    private final int IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_produto_loja);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogAtualizandoDados));

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
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(EditProdutoLoja.this);
                    msgBox.setTitle(getString(R.string.msgBoxTitleExcluir));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaRetirarImagem));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier(getString(R.string.DefaultProductImage), null, null));
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

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos");

        txtEditNomeProduto = findViewById(R.id.txtEditNomeProduto);
        txtEditValorProduto = findViewById(R.id.txtEditValorProduto);
        txtEditDescricaoProduto = findViewById(R.id.txtEditDescricaoProduto);

        Button btnConfirmaEditProduto = findViewById(R.id.btnConfirmarEditProduto);
        btnConfirmaEditProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                prod.setFields(txtEditNomeProduto, txtEditValorProduto, txtEditDescricaoProduto);

                if(validProductFields(EditProdutoLoja.this, prod)){

                    AlertDialog.Builder msgBox = new AlertDialog.Builder(EditProdutoLoja.this);
                    msgBox.setTitle(getString(R.string.msgBoxTitleAlteraçãoDeDados));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaAlterarDadosDoProduto));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            progressDialog.show();

                            if(picChanged) {

                                if(deleteImageFromFirebaseStorage(picOldName, "Produtos")) picOldName = "";
                                if(hasPicture) Fileuploader();
                                else writeNewProduto();
                            }
                            else writeNewProduto();
                        }
                    });
                    msgBox.setNegativeButton(getString(R.string.nao), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    msgBox.show();
                }
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

    @Override
    protected void onStart() {

        super.onStart();
        Intent i = getIntent();
        String idProd = i.getStringExtra("id");
        firebaseAuth.getCurrentUser();
        refUser = refUser.child(Objects.requireNonNull(firebaseAuth.getUid())).child(idProd);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (txtEditNomeProduto.getText().toString().isEmpty()) {

                    Produto produto = snapshot.getValue(Produto.class);

                    txtEditNomeProduto.setText(Objects.requireNonNull(produto).getNome());
                    txtEditValorProduto.setText(produto.getValor().replace(',', '.'));
                    txtEditDescricaoProduto.setText(produto.getDescricao());
                    picOldName = produto.getPic();

                    if(loadPic(pic, picOldName, "Produtos")) hasPicture = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProdutoLoja.this, getString(R.string.ToastErroAoCarregarDadosProduto), Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }

    private void choosePic(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent= new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,IMAGE_REQUEST_CODE);
        }
        else requestStoragePermition();
    }

    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Produtos").child(System.currentTimeMillis()+"."+getExtension(this, picUri));
        if(deleteImageFromFirebaseStorage(picOldName, "Produtos")) picOldName = "";

        Ref.putFile(picUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()){}

                        if (urlTask.isSuccessful()) {
                            picName = taskSnapshot.getStorage().getName();
                            writeNewProduto();
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

    private void writeNewProduto() {

        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = Objects.requireNonNull(fbuser).getUid();

        prod.setPic(getFinalPictureName());

        FirebaseDatabase.getInstance().getReference().child("produtos").child(userId).child(prod.getId()).setValue(prod);
        goToProdutos();
    }

    private void goToProdutos() {
        startActivity(new Intent(EditProdutoLoja.this, ProdutosLoja.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditProdutoLoja.this, ProdutosLoja.class));
        finish();
    }

    private void requestStoragePermition(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.AlertDialogTitlePermissaoNecessariaImagem))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditProdutoLoja.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        }
                    })
                    .create().show();
        }
        else ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST_CODE && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            try {
                Bitmap foto = resize(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
                picUri = getImageCompressed(this, foto);
                pic.setImageURI(picUri);
                hasPicture = true;
                picChanged = true;
            }
            catch (IOException e){
                e.printStackTrace();
            }

        }
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

    private String getFinalPictureName(){

        String url = "";

        if(hasPicture){
            if(picOldName.length() > 0) url = picOldName;
            else url = picName;
        }
        return url;
    }
}