package com.example.merka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Tela_Inicial extends AppCompatActivity {

    private Button btnPerfil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela__inicial);

        btnPerfil = findViewById(R.id.buttonPerfil);

        btnPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Tela_Inicial.this, Perfil.class);
                startActivity(intent);
            }
        });
    }
}