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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.MimeTypeMap;
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
import com.example.merka.utils.FirebaseMethods;
import com.example.merka.utils.PicMethods;
import com.example.merka.utils.TextMethods;
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

public class CriarLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtCpfLoja;
    private EditText txtResponsavelLoja;

    private RadioGroup radioGroupCadastro;
    private RadioButton radioCadastro;

    private ImageView pic;

    private boolean hasPicture = false;

    private ProgressDialog progressDialog;

    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase

    private final int STORAGE_PERMISSION_CODE = 2;

    private StorageReference mStorageRef;

    private Uri picUri;
    private String picName;
    private String lastChar = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_loja);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Atualizando dados");

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");

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
                validar();
            }
        });

        btnCancelarCriarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CriarLoja.this, Tela_Inicial.class));
            }
        });
    }

    private String getExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }

    private void Fileuploader(){
        StorageReference Ref=mStorageRef.child("Lojas").child(System.currentTimeMillis()+"."+getExtension(picUri));

        Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful()){}

                        if (urlTask.isSuccessful()){
                            picName = taskSnapshot.getStorage().getName();
                            criarLoja();
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

    private void validar(){
        final String nome = TextMethods.formatText(txtNomeLoja.getEditableText().toString());
        final String contato = TextMethods.justNumbers(txtContatoLoja.getEditableText().toString());
        final String endereco = TextMethods.formatText(txtEnderecoLoja.getEditableText().toString());
        final String descricao = TextMethods.formatText(txtDescricaoLoja.getEditableText().toString());
        final String responsavel= TextMethods.formatText(txtResponsavelLoja.getEditableText().toString());
        final String cpf = txtCpfLoja.getEditableText().toString();

        if(TextMethods.validateLojaFields(nome,contato,endereco,descricao,responsavel)){
            if(TextMethods.cpfValido(cpf)){
                if(TextMethods.validateMinLengthNumber(contato, 9)){

                    progressDialog.show();

                    if(hasPicture) Fileuploader();
                    else criarLoja();
                }
                else printToast(getString(R.string.ToastContatoComNoMinimoDezDigitos));
            }
            else printToast(getString(R.string.ToastDigiteCPFvalido));
        }
        else printToast(getString(R.string.ToastPreenchaTodosCampos));
    }

    private void criarLoja(){
        final String nome = TextMethods.formatText(txtNomeLoja.getEditableText().toString());
        final String contato = TextMethods.justNumbers(txtContatoLoja.getEditableText().toString());
        final String endereco = TextMethods.formatText(txtEnderecoLoja.getEditableText().toString());
        final String descricao = TextMethods.formatText(txtDescricaoLoja.getEditableText().toString());
        final String cpf = txtCpfLoja.getEditableText().toString();
        final String responsavel=TextMethods.formatText(txtResponsavelLoja.getEditableText().toString());
        final String url = (hasPicture) ? picName : "";

        int radioId = radioGroupCadastro.getCheckedRadioButtonId();
        radioCadastro = findViewById(radioId);
        final String delivery = radioCadastro.getText().toString();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = Objects.requireNonNull(user).getUid();

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("users").child(userId).child("store").setValue(true);

        writeNewLoja(userId, nome, contato, endereco, descricao,delivery,cpf, url, responsavel);
    }

    public void goToLoja(){
        startActivity(new Intent(CriarLoja.this, PerfilLoja.class));
        finish();
    }

    private void writeNewLoja(String userId, String nome, String contato, String endereco, String descricao,String delivery, String cpf, String ImageUrl,String responsavel) {
        //usando o mesmo UID do Firebase Authentication: userId

        try {//tentando cadastrar no banco
            Loja loja = new Loja(userId, nome, contato, endereco, descricao, delivery, cpf, ImageUrl, responsavel);

            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("lojas").child(userId).setValue(loja);
            goToLoja();


        } catch (DatabaseException e) {
            progressDialog.dismiss();
            printToast(e.getMessage());
        }
    }
    private void choosePic(){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent= new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,1);
        }
        else requestStoragePermition();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

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
                            ActivityCompat.requestPermissions(CriarLoja.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) printToast(getString(R.string.permissaoAceita));
            else printToast(getString(R.string.permissaoNegada));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CriarLoja.this, Tela_Inicial.class));
        finish();
    }

    private void printToast(String value) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
    }
}