package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class VitrineLoja extends AppCompatActivity {

    private ImageView vitrineLojaImage;
    private TextView vitrineNomeLoja;
    private TextView vitrineContatoLoja;
    private TextView vitrineEnderecoLoja;
    private TextView vitrineDeliveryLoja;
    private TextView vitrineDescricaoLoja;

    private RecyclerView produtosRecyclerView;
    private ProdutoVitrineAdapter adapter;
    private List<Produto> produtos;

    private FirebaseUser fireUser;
    private DatabaseReference refUser;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener userListener;

    private Intent i;
    private String idLoja;
    private String comingFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitrine_loja);

        firebaseAuth = FirebaseAuth.getInstance();
        refUser = FirebaseDatabase.getInstance().getReference().child("lojas");

        vitrineLojaImage = findViewById(R.id.vitrineLojaImage);
        vitrineNomeLoja = findViewById(R.id.vitrineNomeLoja);
        vitrineContatoLoja=findViewById(R.id.vitrineContatoLoja);
        vitrineContatoLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCall();
            }
        });
        vitrineEnderecoLoja =findViewById(R.id.vitrineEnderecoLoja);
        vitrineEnderecoLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirMapa();
            }
        });
        vitrineDeliveryLoja=findViewById(R.id.vitrineDeliveryLoja);
        vitrineDescricaoLoja=findViewById(R.id.vitrineDescricaoLoja);

        produtosRecyclerView = findViewById(R.id.recyclerViewVitrineProdutos);
        produtos = new ArrayList<>();
        adapter = new ProdutoVitrineAdapter(produtos,this);
        produtosRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        produtosRecyclerView.setLayoutManager(linearLayoutManager);

        configVitrine();
    }
    private void openCall() {
        String uri = "tel:" + vitrineContatoLoja.getText().toString() ;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        decisaoVolta();
    }

    private void decisaoVolta(){
        if(comingFrom.equals("busca"))
            startActivity(new Intent(VitrineLoja.this,TelaBusca.class));
        else
            startActivity(new Intent(VitrineLoja.this,Tela_Inicial.class));
        finish();
    }

    private void setupFirebase() {

        refUser = FirebaseDatabase.getInstance().getReference().child("produtos").child(idLoja);

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    produtos.clear();
                    for(DataSnapshot ds : snapshot.getChildren()){
                        produtos.add(ds.getValue(Produto.class));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VitrineLoja.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configVitrine(){
        i = getIntent();
        idLoja = i.getStringExtra("idLoja");
        comingFrom = i.getStringExtra("comingFrom");

        refUser = refUser.child(idLoja);

        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Loja loja = snapshot.getValue(Loja.class);

                vitrineNomeLoja.setText(loja.nome);
                vitrineContatoLoja.setText(loja.contato);
                vitrineEnderecoLoja.setText((loja.endereco));
                vitrineDescricaoLoja.setText(loja.descricao);
                vitrineDeliveryLoja.setText(loja.delivery);
                if(loja.PicUrl.length() > 0) new EditLojaPerfil.DownloadImageTask((ImageView) vitrineLojaImage).execute(loja.PicUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VitrineLoja.this, "Falha ao carregar dados da loja.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        refUser.addListenerForSingleValueEvent(userListener);
        setupFirebase();
    }

    private void abrirMapa(){

        String destino = vitrineEnderecoLoja.getText().toString().trim();

        try{
            //When maps is installed
            //Initialize Uri
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir//"+destino);
            //Initialize intent with action view
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            //Set package
            intent.setPackage("com.google.android.apps.maps");
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Start activity
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            //When google maps is not installed
            //Initialize uri
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            //Initialize intent with action view
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            //Set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //Start activity
            startActivity(intent);
        }
    }
}