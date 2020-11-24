package com.example.merka.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.merka.models.Loja;
import com.example.merka.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class PerfilLoja extends AppCompatActivity {

    private TextView txtNomeLoja;
    private TextView txtContatoLoja;
    private TextView txtEnderecoLoja;
    private TextView txtDescricao;
    private TextView txtDeliveryLoja;
    private TextView txtResponsavel;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;

    private ImageView pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_loja);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        pic = findViewById(R.id.perfilLojaImage);

        txtNomeLoja=findViewById(R.id.textViewNomeLoja);
        txtContatoLoja=findViewById(R.id.txtPerfilContatoLoja);
        txtEnderecoLoja=findViewById(R.id.txtPerfilEnderecoLoja);
        txtDescricao=findViewById(R.id.txtPerfilDescricaoLoja);
        txtDeliveryLoja=findViewById(R.id.txtPerfilDeliveryLoja);
        txtResponsavel=findViewById(R.id.txtPerfilResponsavelLoja);

        Button btnEditarLoja = findViewById(R.id.buttonEditarLoja);
        btnEditarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PerfilLoja.this, EditLojaPerfil.class));
                finish();
            }
        });

        Button btnProdutos = findViewById(R.id.btnProdutosLoja);
        btnProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PerfilLoja.this, ProdutosLoja.class));
            }
        });

        TextView btnHome = findViewById(R.id.textViewHomePerfil);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (PerfilLoja.this, Tela_Inicial.class));
                finish();
            }
        });

        TextView btnPerfil = findViewById(R.id.textViewPerfilPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (PerfilLoja.this, Perfil.class));
                finish();
            }
        });

        TextView btnBusca = findViewById(R.id.textViewBuscaPerfil);
        btnBusca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PerfilLoja.this, TelaBusca.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.getCurrentUser();
        refUser = refUser.child(Objects.requireNonNull(firebaseAuth.getUid()));

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Loja loja = snapshot.getValue(Loja.class);

                txtNomeLoja.setText(Objects.requireNonNull(loja).getNome());
                txtContatoLoja.setText(loja.getContato());
                txtEnderecoLoja.setText(loja.getEndereco());
                txtDescricao.setText(loja.getDescricao());
                txtDeliveryLoja.setText(loja.getDelivery());
                txtResponsavel.setText(loja.getResponsavel());

                if(loja.getPic().length() > 0) {

                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference imageRef = storage.getReference()
                            .child("Images").child("Lojas").child(loja.getPic());

                    imageRef.getBytes(1024*1024)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    pic.setImageBitmap(bitmap);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PerfilLoja.this, getString(R.string.ToastErroAoCarregarDadosLoja),
                        Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refUser.removeEventListener(userListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(PerfilLoja.this, Tela_Inicial.class));
        finish();
    }
}

