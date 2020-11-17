package com.example.merka.Activity;

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

import com.example.merka.Utils.DownloadImageTask;
import com.example.merka.Utils.FirebaseMethods;
import com.example.merka.Models.Loja;
import com.example.merka.Models.Produto;
import com.example.merka.Recyclerview.ProdutoHorizontalAdapter;
import com.example.merka.R;
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
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

public class EditLojaPerfil extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;

    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtCpfLoja;
    private EditText txtResponsavel;

    private Button btnConfirmar;
    private Button btnCancelar;
    private Button btnExcluir;

    private RadioGroup radioGroupAlteracao;
    private RadioButton radioAlteracao;
    private RadioButton radioEditSim;
    private RadioButton radioEditNao;

    private ProgressDialog progressDialog;

    private StorageReference mStorageRef;
    private StorageTask uploadTask;

    private boolean hasPicture = false;
    private boolean picChanged = false;

    private int STORAGE_PERMISSION_CODE = 1;

    private String idLoja;
    private String oldUrl;
    private String lastChar = " ";

    private ImageView pic;
    private Uri picUri;
    private Uri picUrl;
    private Uri picData;

    private ProdutoHorizontalAdapter adapterProduto;
    private DatabaseReference refUserProduto;
    private List<Produto> produtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_loja_perfil);

        produtos = new ArrayList<>();
        adapterProduto = new ProdutoHorizontalAdapter(produtos,this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Atualizando dados");

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
                    msgBox.setTitle("Excluir imagem");
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage("Deseja retirar a imagem da loja?");
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
        btnConfirmar = findViewById(R.id.btnConfirmarAlteracaoLoja);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validar_e_confirmarAlteracao();
            }
        });

        btnCancelar = findViewById(R.id.btnCancelarAlteracaoLoja);
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
            }
        });

        btnExcluir=findViewById(R.id.btnExcluirLoja);
        btnExcluir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarExclusao();
            }
        });
    }
    private void choosePic(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Intent intent= new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,3);
        }
        else{
            requestStoragePermition();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==3 && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            picData = data.getData();

            try {
                Bitmap fotoBuscada = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());


                picUri = redimensionar_e_compressao(fotoBuscada);
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
                    .setTitle("Permissão necessária para inserir uma imagem")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(EditLojaPerfil.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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

    private void validar_e_confirmarAlteracao(){

        String nome = TextMethods.formatText(txtNomeLoja.getEditableText().toString());
        String responsavel = TextMethods.formatText(txtResponsavel.getEditableText().toString());
        String contato = justNumbers(txtContatoLoja.getEditableText().toString());
        String endereco = TextMethods.formatText(txtEnderecoLoja.getEditableText().toString());
        String descricao = TextMethods.formatText(txtDescricaoLoja.getEditableText().toString());
        String cpf = txtCpfLoja.getEditableText().toString();

        if(validateFields(nome,contato,endereco,descricao,responsavel)){
            if(cpfValido(cpf)){
                if(TextMethods.validateMinAndMaxLengthNumber(this, contato, 9, 11)){

                    validarDadosExistentes(cpf, contato);
                }
            }
            else printToast(getString(R.string.ToastDigiteCPFvalido));
        }
        else printToast(getString(R.string.ToastPreenchaTodosCampos));
    }

    private void validarDadosExistentes(final String cpf, final String contato){

        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        final String userId = fbuser.getUid();

        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(!progressDialog.isShowing()){
                    if(!FirebaseMethods.checkCPFExists(cpf, snapshot, userId)){
                        if(!FirebaseMethods.checkContatoExists(contato, snapshot, userId)){

                            AlertDialog.Builder msgBox = new AlertDialog.Builder(EditLojaPerfil.this);
                            msgBox.setTitle("Alteração de dados");
                            msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                            msgBox.setMessage("Deseja alterar os dados da sua loja?");
                            msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    progressDialog.show();

                                    if(picChanged) {
                                        fileDeleteFromFirebase();

                                        if(hasPicture) Fileuploader();
                                        else atualizarLoja();
                                    }
                                    else atualizarLoja();
                                }
                            });
                            msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            msgBox.show();


                        }
                        else printToast("Este contato já está sendo utilizado!");
                    }
                    else printToast("Este CPF já está sendo utilizado!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditLojaPerfil.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void printToast(String value){
        Toast.makeText(EditLojaPerfil.this, value, Toast.LENGTH_SHORT).show();
    }

    private void atualizarLoja(){
        final String nome = TextMethods.formatText(txtNomeLoja.getEditableText().toString());
        final String responsavel = TextMethods.formatText(txtResponsavel.getEditableText().toString());
        final String contato = justNumbers(txtContatoLoja.getEditableText().toString());
        final String endereco = TextMethods.formatText(txtEnderecoLoja.getEditableText().toString());
        final String descricao = TextMethods.formatText(txtDescricaoLoja.getEditableText().toString());
        final String cpf = txtCpfLoja.getEditableText().toString();
        final String url = getFinalPictureUrl();

        int radioIdAlteracao = radioGroupAlteracao.getCheckedRadioButtonId();
        radioAlteracao = findViewById(radioIdAlteracao);
        final String delivery = radioAlteracao.getText().toString();

        DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = fbuser.getUid();

        Loja loja = new Loja(idLoja, nome, contato, endereco, descricao, delivery, cpf, url, responsavel);

        refUser.child("lojas").child(userId).setValue(loja);

        printToast(getString(R.string.ToastDadosAtualizados));
        goToLoja();
    }

    private String justNumbers(String contato) {

        if(contato.length() > 0) return contato.replaceAll("\\s+","");
        return contato;
    }

    private String maskAplication(String contato) {

        return contato.substring(0,2) + " " + contato.substring(2);
    }

    private String getFinalPictureUrl(){

        String url = "";

        if(hasPicture){
            if(oldUrl.length() > 0) url = oldUrl;
            else url = String.valueOf(picUrl);
        }
        return url;
    }

    public void goToLoja(){
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();
    }

    public boolean validateFields(String nome, String contato, String endereco,String desc,String responsavel){
        return !nome.isEmpty() && !contato.isEmpty() && !endereco.isEmpty() && !desc.isEmpty() && !responsavel.isEmpty();
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

    private void confirmarExclusao(){
        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle("Excluir");
        msgBox.setIcon(android.R.drawable.ic_menu_delete);
        msgBox.setMessage("Deseja mesmo excluir sua loja?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(oldUrl.length() > 0){
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

                deleteUserData();
                goToMenu();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        msgBox.show();
    }

    private void goToMenu(){
        startActivity(new Intent(EditLojaPerfil.this, Tela_Inicial.class));
        finish();
    }

    public void deleteUserData(){//deletar dados do usuário do banco de dados
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("lojas").child(userId).removeValue();

        DatabaseReference refUserProduto;

        refUserProduto = FirebaseDatabase.getInstance().getReference().child("produtos").child(userId);

        refUserProduto.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    for(DataSnapshot ds : snapshot.getChildren()){

                        Produto prod = ds.getValue(Produto.class);

                        if(prod.picUrl.length() > 0){

                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(prod.picUrl);

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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditLojaPerfil.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        refUser.child("produtos").child(userId).removeValue();
        refUser.child("users").child(userId).child("store").setValue(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(firebaseAuth.getUid());

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(txtNomeLoja.getText().length() == 0) {
                    Loja loja = dataSnapshot.getValue(Loja.class);

                    txtNomeLoja.setText(loja.nome);
                    txtContatoLoja.setText(maskAplication(loja.contato));
                    txtEnderecoLoja.setText(loja.endereco);
                    txtDescricaoLoja.setText(loja.descricao);
                    txtCpfLoja.setText(loja.cpf);
                    txtResponsavel.setText(loja.responsavel);
                    idLoja = loja.id;
                    oldUrl = loja.PicUrl;

                    if (oldUrl.length() > 0) {
                        new DownloadImageTask((ImageView) pic).execute(loja.PicUrl);
                        hasPicture = true;
                    }

                    if (loja.delivery.equals("Sim")) {
                        radioEditSim.setChecked(true);
                    } else {
                        radioEditNao.setChecked(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                printToast(getString(R.string.ToastErrorLoadStoreData));
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
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

    private void Fileuploader(){

        StorageReference Ref=mStorageRef.child("Lojas").child(System.currentTimeMillis()+"."+getExtension(picUri));

        uploadTask = Ref.putFile(picUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                        picUrl = urlTask.getResult();

                        atualizarLoja();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) { printToast(getString(R.string.ToastUploadNaoFoiPossivel));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();
    }
}