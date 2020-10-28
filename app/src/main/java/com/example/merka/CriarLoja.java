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

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.InputMismatchException;
import java.util.Random;
import java.util.UUID;

public class CriarLoja extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtCpfLoja;
    private EditText txtResponsavelLoja;

    private Button btnCriarLoja;
    private Button btnCancelarCriarLoja;

    private RadioGroup radioGroupCadastro;
    private RadioButton radioCadastro;

    private ImageView pic;

    private boolean picChanged = false;

    private FirebaseUser user;
    private FirebaseAuth mAuth; //variável de acesso ao Firebase autenticatiton
    private DatabaseReference refUser; // variável de acesso ao RealTime DataBase
    private ValueEventListener userListener;

    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private StorageTask uploadTask;

    private Uri picUri;
    private Uri picUrl = null;
    private String lastChar = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_loja);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("users");
        storage = FirebaseStorage.getInstance();

        pic = findViewById(R.id.profile_image);
        pic.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder msgBox = new AlertDialog.Builder(CriarLoja.this);
                msgBox.setTitle("Excluir imagem");
                msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                msgBox.setMessage("Deseja retirar a imagem da loja?");
                msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        picChanged = false;
                        pic.setImageResource(0);
                    }
                });

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
                    else if (digits == 8) {
                        txtContatoLoja.append("-");
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

        btnCriarLoja=findViewById(R.id.btnConfirmarCriarLoja);
        btnCancelarCriarLoja=findViewById(R.id.btnCancelarCriarLoja);

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

    private boolean Fileuploader(){
        StorageReference Ref=mStorageRef.child("Lojas").child(System.currentTimeMillis()+"."+getExtension(picUri));

        uploadTask = Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        picUrl = urlTask.getResult();

                        criarLoja();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(CriarLoja.this , "Não foi possível fazer o upload da imagem",Toast.LENGTH_LONG).show();
                    }
                });
        return true;
    }

    private void validar(){
        final String nome = txtNomeLoja.getEditableText().toString();
        final String contato = justNumbers(txtContatoLoja.getEditableText().toString());
        final String endereco = txtEnderecoLoja.getEditableText().toString();
        final String descricao = txtDescricaoLoja.getEditableText().toString();
        final String cpf = txtCpfLoja.getEditableText().toString();
        final String responsavel=txtResponsavelLoja.getEditableText().toString();

        if(validateFields(nome,contato,endereco,descricao,responsavel)){
            if(cpfValido(cpf)){
                if(validateMinLengthNumber(contato)){
                    if(picChanged) Fileuploader();
                    else criarLoja();
                }
                else{
                    Toast.makeText(CriarLoja.this, getString(R.string.min_length_number_warning),
                            Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(CriarLoja.this, "Digite um CPF válido",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(CriarLoja.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void criarLoja(){
        final String nome = retornaNomeFormatado(txtNomeLoja.getEditableText().toString());
        final String contato = justNumbers(txtContatoLoja.getEditableText().toString());
        final String endereco = txtEnderecoLoja.getEditableText().toString();
        final String descricao = txtDescricaoLoja.getEditableText().toString();
        final String cpf = txtCpfLoja.getEditableText().toString();
        final String responsavel=txtResponsavelLoja.getEditableText().toString();
        int radioId = radioGroupCadastro.getCheckedRadioButtonId();
        radioCadastro = findViewById(radioId);
        final String delivery = radioCadastro.getText().toString();

        final String url = (String.valueOf(picUrl).length() > 0) ? String.valueOf(picUrl) : "";

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("users").child(userId).child("store").setValue(true);

        writeNewLoja(userId, nome, contato, endereco, descricao,delivery,cpf, url, responsavel);
    }

    public String retornaNomeFormatado(String nome){
        return nome.substring(0, 1).toUpperCase() + nome.substring(1);
    }

    public void goToLoja(){
        startActivity(new Intent(CriarLoja.this, PerfilLoja.class));
        finish();
    }

    public boolean validateFields(String nome, String contato, String endereco, String desc, String responsavel){
        return !nome.isEmpty() && !contato.isEmpty() && !endereco.isEmpty() && !desc.isEmpty() && !responsavel.isEmpty();
    }

    public boolean validateMinLengthNumber(String num){
        return num.length() > 9;
    }
    public static boolean cpfValido(String CPF) {
        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000") ||
                CPF.equals("11111111111") ||
                CPF.equals("22222222222") || CPF.equals("33333333333") ||
                CPF.equals("44444444444") || CPF.equals("55555555555") ||
                CPF.equals("66666666666") || CPF.equals("77777777777") ||
                CPF.equals("88888888888") || CPF.equals("99999999999") ||
                (CPF.length() != 11))
            return(false);

        char dig10, dig11;
        int sm, i, r, num, peso;

        // "try" - protege o codigo para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i=0; i<9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig10 = '0';
            else dig10 = (char)(r + 48); // converte no respectivo caractere numerico

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for(i=0; i<10; i++) {
                num = (int)(CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11))
                dig11 = '0';
            else dig11 = (char)(r + 48);

            // Verifica se os digitos calculados conferem com os digitos informados.
            return (dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10));
        } catch (InputMismatchException erro) {
            return(false);
        }
    }

    private void writeNewLoja(String userId, String nome, String contato, String endereco, String descricao,String delivery, String cpf, String ImageUrl,String responsavel) {
        //usando o mesmo UID do Firebase Authentication: userId

        try {//tentando cadastrar no banco
            Loja loja = new Loja(userId,nome, contato, endereco, descricao,delivery,cpf, ImageUrl,responsavel);

            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("lojas").child(userId).setValue(loja);
           // uploadPic();
            goToLoja();


        } catch (DatabaseException e) {
                      Toast.makeText(CriarLoja.this, e.getMessage(),
                             Toast.LENGTH_SHORT).show();
        }
    }
    private void choosePic(){
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            picUri = data.getData();
            pic.setImageURI(picUri);
            picChanged = true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(CriarLoja.this, Tela_Inicial.class));
        finish();
    }

    private String justNumbers(String contato) {
        String result = contato.substring(0,2);
        result += contato.substring(3, 8);
        return result += contato.substring(9);
    }
}