package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
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

public class VitrineLoja extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {

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
        vitrineEnderecoLoja =findViewById(R.id.vitrineEnderecoLoja);
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

    public void decisaoMaps(View v){
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.endereco_menu);
        popupMenu.show();
    }
    public void decisaoContato(View v){
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.contato_menu);
        popup.show();
    }

    private void rotas(){

        String destino = vitrineEnderecoLoja.getText().toString().trim();

        try{
            //When Maps is installed
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir//"+destino);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            //When Maps is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
    private void localizacao(){

        String destino = vitrineEnderecoLoja.getText().toString().trim();

        try{
            //When maps is installed
            //Initialize Uri
            Uri uri = Uri.parse("https://www.google.co.in/maps/place/"+destino);
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
    private void openCall() {
        String uri = "tel:" + vitrineContatoLoja.getText().toString() ;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }
    private void openWhatsapp(){
        String numero = vitrineContatoLoja.getText().toString().trim();
        try{
            //When WhatsApp is installed
            Uri uri = Uri.parse("https://api.whatsapp.com/send?phone=55"+numero);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            //When WhatsApp maps is not installed
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.maps1:
                rotas();
                return true;
            case R.id.maps2:
                localizacao();
                return true;
            case R.id.contato1:
                openCall();
                return true;
            case R.id.contato2:
                openWhatsapp();
                return true;
            default:
                return false;
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        decisaoVolta();
    }
}