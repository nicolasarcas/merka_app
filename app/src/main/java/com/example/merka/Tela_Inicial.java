package com.example.merka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Tela_Inicial extends AppCompatActivity {

    private TextView btnPerfil;
    private TextView btnLoja;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela__inicial);

        btnPerfil = findViewById(R.id.textViewPerfilPerfil);

        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Tela_Inicial.this, Perfil.class));
            }
        });

        btnLoja = findViewById(R.id.textViewLojaPerfil);
        btnLoja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent (Tela_Inicial.this, CriarLoja.class));
            }
        });

    }
}