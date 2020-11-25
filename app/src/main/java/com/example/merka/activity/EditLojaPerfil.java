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

import com.example.merka.R;
import com.example.merka.models.Loja;
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

import static com.example.merka.utils.FirebaseMethods.checkCPFExists;
import static com.example.merka.utils.FirebaseMethods.checkContatoExists;
import static com.example.merka.utils.FirebaseMethods.deleteUserData;
import static com.example.merka.utils.PicMethods.deleteImageFromFirebaseStorage;
import static com.example.merka.utils.PicMethods.getExtension;
import static com.example.merka.utils.PicMethods.getImageCompressed;
import static com.example.merka.utils.PicMethods.loadPic;
import static com.example.merka.utils.PicMethods.resize;
import static com.example.merka.utils.TextMethods.maskAplication;
import static com.example.merka.utils.TextMethods.validStoreFields;

public class EditLojaPerfil extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    private ImageView pic;
    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtCpfLoja;
    private EditText txtResponsavel;
    private RadioGroup radioGroupAlteracao;
    private RadioButton radioEditSim;
    private RadioButton radioEditNao;

    private boolean hasPicture = false;
    private boolean picChanged = false;

    final Loja loja = new Loja();
    private Uri picUri;
    private String oldName;
    private String picName;
    private String lastChar = " ";

    private final int STORAGE_REQUEST_CODE = 1;
    private final int IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_loja_perfil);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.progressDialogAtualizandoDados));

        firebaseAuth= FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        mStorageRef = FirebaseStorage.getInstance().getReference("Images");

        radioGroupAlteracao = findViewById(R.id.radioGroupAlteracao);
        radioEditNao=findViewById(R.id.radioAlteracaoNao);
        radioEditSim=findViewById(R.id.radioAlteracaoSim);

        pic = findViewById(R.id.editPerfilLojaImage);
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
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(EditLojaPerfil.this);
                    msgBox.setTitle(getString(R.string.msgBoxTitleExcluirImagem));
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage(getString(R.string.msgBoxMessageDesejaRetirarImagem));
                    msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            pic.setImageResource(getResources().getIdentifier(getString(R.string.DefaultLojaImage), null, null));
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

        txtNomeLoja = findViewById(R.id.txtEditNomeLoja);
        txtContatoLoja = findViewById(R.id.txtEditContatoLoja);
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
            public void afterTextChanged(Editable s){}
        });

        txtEnderecoLoja = findViewById(R.id.txtEditEnderecoLoja);
        txtDescricaoLoja = findViewById(R.id.txtEditDescricaoLoja);
        txtCpfLoja = findViewById(R.id.txtEditCpfLoja);
        txtResponsavel = findViewById(R.id.txtEditResponsavelLoja);
        Button btnConfirmar = findViewById(R.id.btnConfirmarAlteracaoLoja);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loja.setFields(txtNomeLoja, txtContatoLoja, txtEnderecoLoja, txtDescricaoLoja, txtResponsavel, txtCpfLoja);

                FirebaseUser user = firebaseAuth.getCurrentUser();
                final String userId = Objects.requireNonNull(user).getUid();

                if(validStoreFields(EditLojaPerfil.this, loja)) {
                    FirebaseDatabase.getInstance().getReference().child("lojas").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (checkCPFExists(EditLojaPerfil.this, snapshot, loja, userId)) {
                                if (checkContatoExists(EditLojaPerfil.this, snapshot, loja, userId)) {

                                    if(!progressDialog.isShowing()){
                                        AlertDialog.Builder msgBox = new AlertDialog.Builder(EditLojaPerfil.this);
                                        msgBox.setTitle(getString(R.string.msgBoxTitleAlteraçãoDeDados));
                                        msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                                        msgBox.setMessage(R.string.msgBoxMessageDesejaAlterarDadosDaLoja);
                                        msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                progressDialog.show();

                                                if (picChanged) {
                                                    deleteImageFromFirebaseStorage(oldName, "Lojas");

                                                    if (hasPicture) Fileuploader();
                                                    else writeNewLoja();
                                                }
                                                else writeNewLoja();
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
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }
            }
        });

        Button btnCancelar = findViewById(R.id.btnCancelarAlteracaoLoja);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
            }
        });

        Button btnExcluir = findViewById(R.id.btnExcluirLoja);
        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarExclusao();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(Objects.requireNonNull(firebaseAuth.getUid()));

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (txtNomeLoja.getText().length() == 0) {
                    Loja loja = dataSnapshot.getValue(Loja.class);

                    txtNomeLoja.setText(Objects.requireNonNull(loja).getNome());
                    txtContatoLoja.setText(maskAplication(loja.getContato()));
                    txtEnderecoLoja.setText(loja.getEndereco());
                    txtDescricaoLoja.setText(loja.getDescricao());
                    txtCpfLoja.setText(loja.getCpf());
                    txtResponsavel.setText(loja.getResponsavel());
                    oldName = loja.getPic();

                    if (oldName.length() > 0) {

                        loadPic(pic, oldName, "Lojas");
                        hasPicture = true;
                    }

                    if (loja.getDelivery().equals("Sim")) {
                        radioEditSim.setChecked(true);
                    } else {
                        radioEditNao.setChecked(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditLojaPerfil.this, getString(R.string.ToastErroAoCarregarDadosLoja), Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }

    private void choosePic(){

        if(ContextCompat.checkSelfPermission(EditLojaPerfil.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent= new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,IMAGE_REQUEST_CODE);
        }
        else requestStoragePermition();
    }

    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Lojas").child(System.currentTimeMillis()+"."+getExtension(EditLojaPerfil.this, picUri));

        Ref.putFile(picUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                        Toast.makeText(EditLojaPerfil.this, getString(R.string.ToastUploadImagemNaoFoiPossivel), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void writeNewLoja(){

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        int radioIdAlteracao = radioGroupAlteracao.getCheckedRadioButtonId();
        RadioButton radioAlteracao = findViewById(radioIdAlteracao);

        loja.setPic(getFinalPicture());
        loja.setDelivery(radioAlteracao.getText().toString());
        loja.setId(Objects.requireNonNull(fbuser).getUid());

        refUser.child("lojas").child(loja.getId()).setValue(loja);

        Toast.makeText(EditLojaPerfil.this, getString(R.string.ToastDadosAtualizados), Toast.LENGTH_SHORT).show();
        goToLoja();
    }

    private void confirmarExclusao(){
        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle(getString(R.string.msgBoxTitleExcluir));
        msgBox.setIcon(android.R.drawable.ic_menu_delete);
        msgBox.setMessage(getString(R.string.msgBoxMessageDesejaExcluirLoja));
        msgBox.setPositiveButton(getString(R.string.sim), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(oldName.length() > 0) deleteImageFromFirebaseStorage(oldName, "Lojas");

                FirebaseUser user = firebaseAuth.getCurrentUser();
                String userId = Objects.requireNonNull(user).getUid();
                deleteUserData(EditLojaPerfil.this, userId);

                goToMenu();
            }
        });
        msgBox.setNegativeButton(getString(R.string.nao), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        msgBox.show();
    }

    public void goToLoja(){
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();
    }

    private void goToMenu(){
        startActivity(new Intent(EditLojaPerfil.this, Tela_Inicial.class));
        finish();
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
                            ActivityCompat.requestPermissions(EditLojaPerfil.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
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

    private String getFinalPicture(){

        String url = "";

        if(hasPicture){
            if(oldName.length() > 0) url = oldName;
            else url = picName;
        }
        return url;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();
    }
}