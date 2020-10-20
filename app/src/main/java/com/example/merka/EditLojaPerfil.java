package com.example.merka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.InputMismatchException;
import java.util.UUID;

public class EditLojaPerfil extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;
    private StorageReference storageReference;
    private FirebaseStorage storage;

    private EditText txtNomeLoja;
    private EditText txtContatoLoja;
    private EditText txtEnderecoLoja;
    private EditText txtDescricaoLoja;
    private EditText txtCpfLoja;

    private TextView txtEditImageLoja;

    private ImageView imageView;

    private Button btnConfirmar;
    private Button btnCancelar;
    private Button btnExcluir;

    private RadioGroup radioGroupAlteracao;
    private RadioButton radioAlteracao;

    private RadioButton radioEditSim;
    private RadioButton radioEditNao;

    private ImageView pic;
    public Uri picUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_loja_perfil);

        firebaseAuth= FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        imageView = findViewById(R.id.editPerfilLojaImage);

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

        txtNomeLoja = findViewById(R.id.txtEditNomeLoja);
        txtContatoLoja = findViewById(R.id.txtEditContatoLoja);
        txtEnderecoLoja = findViewById(R.id.txtEditEnderecoLoja);
        txtDescricaoLoja = findViewById(R.id.txtEditDescricaoLoja);
        txtCpfLoja = findViewById(R.id.txtEditCpfLoja);

        btnConfirmar = findViewById(R.id.btnConfirmarAlteracaoLoja);
        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarLoja();
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
        Intent intent= new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,2);
    }

    private void atualizarLoja(){
        final String nome = txtNomeLoja.getEditableText().toString();
        final String contato = txtContatoLoja.getEditableText().toString();
        final String endereco = txtEnderecoLoja.getEditableText().toString();
        final String descricao = txtDescricaoLoja.getEditableText().toString();
        final String cpf = txtCpfLoja.getEditableText().toString();
        final String url = String.valueOf(picUrl);
        int radioIdAlteracao = radioGroupAlteracao.getCheckedRadioButtonId();
        radioAlteracao = findViewById(radioIdAlteracao);
        final String delivery = radioAlteracao.getText().toString();

        if(validateFields(nome,contato,endereco,descricao)){
            if(cpfValido(cpf)){
                if(validateMinLengthNumber(contato)){
                    AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
                    msgBox.setTitle("Alteração de dados");
                    msgBox.setIcon(android.R.drawable.ic_menu_info_details);
                    msgBox.setMessage("Deseja alterar os dados da sua loja?");
                    msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
                            FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                            String userId = fbuser.getUid();

                            Loja loja = new Loja(nome, contato, endereco, descricao,delivery,cpf, url);

                            refUser.child("lojas").child(userId).setValue(loja);
                            //  uploadPic();

                            goToLoja();
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
                    Toast.makeText(EditLojaPerfil.this, getString(R.string.min_length_number_warning),
                            Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(EditLojaPerfil.this, "Digite um CPF válido",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(EditLojaPerfil.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }
    public void goToLoja(){
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();
    }
    public boolean validateFields(String nome, String contato, String endereco,String desc){
        if(nome.isEmpty() || contato.isEmpty() || endereco.isEmpty() || desc.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }
    public boolean validateMinLengthNumber(String num){
        if(num.length() > 9){
            return true;
        }else{
            return false;
        }
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
            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10)))
                return(true);
            else return(false);
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
        startActivity(new Intent(EditLojaPerfil.this,Tela_Inicial.class));
        finish();
    }

    public void deleteUserData(){//deletar dados do usuário do banco de dados
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("lojas").child(userId).removeValue();
        refUser.child("produtos").child(userId).removeValue();
        refUser.child("users").child(userId).child("store").setValue(false);
    }

    static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(firebaseAuth.getUid());

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Loja loja = dataSnapshot.getValue(Loja.class);

                txtNomeLoja.setText(loja.nome);
                txtContatoLoja.setText(loja.contato);
                txtEnderecoLoja.setText(loja.endereco);
                txtDescricaoLoja.setText(loja.descricao);
                txtCpfLoja.setText(loja.cpf);
                if(loja.PicUrl!=null) new DownloadImageTask((ImageView) pic).execute(loja.PicUrl);

                if(loja.delivery.equals("Sim")){
                    radioEditSim.setChecked(true);
                }
                else {
                    radioEditNao.setChecked(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditLojaPerfil.this, "Falha ao carregar dados da loja.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
//        loadImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            picUrl = data.getData();
            pic.setImageURI(picUrl);
        }
    }
    private void uploadPic(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        StorageReference riversRef = storageReference.child(userId).child("images/"+txtNomeLoja.getEditableText().toString());

        riversRef.putFile(picUrl)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Snackbar.make(findViewById(android.R.id.content),"Dados gravados", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), "Falha ao gravar dados", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadImage() {

        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();
        storageReference = FirebaseStorage.getInstance().getReference().child(userId).child("images/"+txtNomeLoja.getText().toString()+".jpg");

        try {
            final File localFile = File.createTempFile(txtNomeLoja.getText().toString(),"jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditLojaPerfil.this, "Falha no carregamento da imagem", Toast.LENGTH_SHORT);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(EditLojaPerfil.this, PerfilLoja.class));
        finish();;
    }
}