package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProdutosLoja extends AppCompatActivity {

    private FloatingActionButton btnAddProduto;

    private RecyclerView produtosRecyclerView;
    private ProdutoAdapter adapter;
    private List<Produto> produtos;

    private FirebaseUser fireUser;
    private DatabaseReference refUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produtos_loja);

        firebaseAuth = FirebaseAuth.getInstance();

        btnAddProduto = findViewById(R.id.btnAddProduto);
        btnAddProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProdutosLoja.this, AdicionaProduto.class));
            }
        });

        produtosRecyclerView = findViewById(R.id.recyclerViewProdutos);
        produtos = new ArrayList<>();
        adapter = new ProdutoAdapter(produtos,this);
        produtosRecyclerView.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        produtosRecyclerView.setLayoutManager(linearLayoutManager);
    }
    private void setupFirebase() {
        fireUser = FirebaseAuth.getInstance().getCurrentUser();
        refUser = FirebaseDatabase.getInstance().getReference().child("produtos").child(fireUser.getUid());

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
                Toast.makeText(ProdutosLoja.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case 11:
                Produto p = produtos.get(item.getGroupId());
                Intent i = new Intent(ProdutosLoja.this,EditProdutoLoja.class);
                i.putExtra("id",p.id);
                startActivity(i);
                return true;
            case 22:
                confirmarExclusao(item.getGroupId());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
    private void confirmarExclusao(final int position){
        AlertDialog.Builder msgBox = new AlertDialog.Builder(this);
        msgBox.setTitle("Excluir");
        msgBox.setIcon(android.R.drawable.ic_menu_delete);
        msgBox.setMessage("Deseja mesmo excluir o produto?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Produto p = produtos.get(position);

                if(p.picUrl.length() > 0){
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(p.picUrl);

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

                produtos.remove(p);
                deleteUserData(p.id);
                adapter.notifyDataSetChanged();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        msgBox.show();
    }
    public void deleteUserData(String prodId){//deletar dados do usuário do banco de dados
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        refUser = FirebaseDatabase.getInstance().getReference();
        refUser.child("produtos").child(userId).child(prodId).removeValue();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebase();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ProdutosLoja.this,PerfilLoja.class));
        finish();
    }
}