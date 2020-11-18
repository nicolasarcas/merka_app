package com.example.merka.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.merka.Models.Loja;
import com.example.merka.R;
import com.example.merka.Utils.DownloadImageTask;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class PerfilLoja extends AppCompatActivity {

    private Button btnEditarLoja;
    private Button btnProdutos;

    private TextView txtNomeLoja;
    private TextView txtContatoLoja;
    private TextView txtEnderecoLoja;
    private TextView txtDescricao;
    private TextView txtDeliveryLoja;
    private TextView txtResponsavel;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference refUser;
    private ValueEventListener userListener;
    private StorageReference mStorageReference;

    private TextView btnHome;
    private TextView btnPerfil;
    private TextView btnBusca;

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

        btnEditarLoja=findViewById(R.id.buttonEditarLoja);
        btnEditarLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PerfilLoja.this, EditLojaPerfil.class));
                finish();
            }
        });

        btnProdutos = findViewById(R.id.btnProdutosLoja);
        btnProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PerfilLoja.this, ProdutosLoja.class));
            }
        });

        btnHome = findViewById(R.id.textViewHomePerfil);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (PerfilLoja.this, Tela_Inicial.class));
                finish();
            }
        });

        btnPerfil = findViewById(R.id.textViewPerfilPerfil);
        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (PerfilLoja.this, Perfil.class));
                finish();
            }
        });

        btnBusca = findViewById(R.id.textViewBuscaPerfil);
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
        refUser = refUser.child(firebaseAuth.getUid());

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Loja loja = snapshot.getValue(Loja.class);

                txtNomeLoja.setText(loja.nome);
                txtContatoLoja.setText(loja.contato);
                txtEnderecoLoja.setText(loja.endereco);
                txtDescricao.setText(loja.descricao);
                txtDeliveryLoja.setText(loja.delivery);
                txtResponsavel.setText(loja.responsavel);
                if(loja.PicUrl.length() > 0) new DownloadImageTask((ImageView) pic).execute(loja.PicUrl);
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

