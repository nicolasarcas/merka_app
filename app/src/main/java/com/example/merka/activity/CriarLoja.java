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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.merka.models.Loja;
import com.example.merka.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Objects;

import static com.example.merka.utils.FirebaseMethods.checkCPFExists;
import static com.example.merka.utils.FirebaseMethods.checkContatoExists;
import static com.example.merka.utils.PicMethods.getExtension;
import static com.example.merka.utils.PicMethods.getImageCompressed;
import static com.example.merka.utils.PicMethods.resize;
import static com.example.merka.utils.TextMethods.validStoreFields;

public class CriarLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    private ImageView pic;
    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtCpfLoja;
    private EditText txtResponsavelLoja;
    private RadioGroup radioGroupCadastro;

    final Loja loja = new Loja();
    private Uri picUri;
    private String picName;
    private String lastChar = " ";

    private boolean hasPicture = false;

    private final int STORAGE_REQUEST_CODE = 1;
    private final int IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_loja);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogAtualizandoDados));

        firebaseAuth = FirebaseAuth.getInstance();

        pic = findViewById(R.id.profile_image);
        pic.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                if(hasPicture){
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(CriarLoja.this);
                    msgBox.setTitle(getString(R.string.msgBoxTitleExcluir));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaRetirarImagem));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier(getString(R.string.DefaultLojaImage), null, null));
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
        radioGroupCadastro = findViewById(R.id.radioGroupCadastro);

        txtNomeLoja=findViewById(R.id.txtNomeLoja);
        txtContatoLoja=findViewById(R.id.txtContatoLoja);
        txtContatoLoja.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                int digits = txtContatoLoja.getText().toString().length();
                if (digits > 1)
                    lastChar = txtContatoLoja.getText().toString().substring(digits-1);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int digits = txtContatoLoja.getText().toString().length();

                if (!lastChar.equals("-")) {
                    if(digits == 2){

                        if(!lastChar.equals(" ")) {
                            txtContatoLoja.append(" ");
                        }
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtEnderecoLoja=findViewById(R.id.txtEnderecoLoja);
        txtDescricaoLoja=findViewById(R.id.txtDescricaoLoja);
        txtCpfLoja = findViewById(R.id.txtCpfLoja);
        txtResponsavelLoja=findViewById(R.id.txtResponsavelLoja);

        Button btnCriarLoja = findViewById(R.id.btnConfirmarCriarLoja);
        Button btnCancelarCriarLoja = findViewById(R.id.btnCancelarCriarLoja);

        btnCriarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loja.setFields(txtNomeLoja, txtContatoLoja, txtEnderecoLoja, txtDescricaoLoja, txtResponsavelLoja, txtCpfLoja);

                if(validStoreFields(CriarLoja.this, loja)){
                    FirebaseDatabase.getInstance().getReference().child("lojas").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(checkCPFExists(CriarLoja.this, snapshot, loja)){
                                if(checkContatoExists(CriarLoja.this, snapshot, loja)){

                                    progressDialog.show();
                                    if(hasPicture) Fileuploader();
                                    else writeNewLoja();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }
            }
        });

        btnCancelarCriarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CriarLoja.this, Tela_Inicial.class));
            }
        });
    }

    private void choosePic(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent= new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,IMAGE_REQUEST_CODE);
        }
        else requestStoragePermition();
    }

    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Lojas").child(System.currentTimeMillis()+"."+getExtension(this, picUri));

        Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful()){}

                        if (urlTask.isSuccessful()){
                            picName = taskSnapshot.getStorage().getName();
                            writeNewLoja();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(CriarLoja.this, getString(R.string.ToastUploadImagemNaoFoiPossivel), Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
    }

    private void writeNewLoja(){

        int radioId = radioGroupCadastro.getCheckedRadioButtonId();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        RadioButton radioCadastro = findViewById(radioId);

        loja.setPic((hasPicture) ? picName : "");
        loja.setId(Objects.requireNonNull(user).getUid());
        loja.setDelivery(radioCadastro.getText().toString());

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("users").child(loja.getId()).child("loja").setValue(true);

        try {//tentando cadastrar no banco

            // variável de acesso ao RealTime DataBase
            refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("lojas").child(loja.getId()).setValue(loja);
            goToLoja();

        } catch (DatabaseException e) {
            progressDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void goToLoja(){
        startActivity(new Intent(CriarLoja.this, PerfilLoja.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CriarLoja.this, Tela_Inicial.class));
        finish();
    }

    private void requestStoragePermition(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.AlertDialogTitlePermissaoNecessariaImagem))
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(CriarLoja.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        }
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

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == STORAGE_REQUEST_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) Toast.makeText(this, getString(R.string.permissaoAceita), Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, getString(R.string.permissaoNegada), Toast.LENGTH_SHORT).show();
        }
    }
}