package com.example.merka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class Perfil extends AppCompatActivity {

    private Button btnEditPerfil;
    private  Button btnLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        btnEditPerfil = findViewById(R.id.buttonEditPerfil);
        btnLogOut = findViewById(R.id.buttonSignOut);

        btnEditPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Perfil.this, EditPerfil.class);
                startActivity(intent);
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                goToLogin();
            }
        });


    }

    public void goToLogin(){
        startActivity (new Intent(this, Login.class));
        finish();
    }
}