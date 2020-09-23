package com.example.merka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnCadastrar;

    private EditText editTextNome;
    private EditText editTextEmail;
    private EditText editTextSenha;
    private  EditText editTextConfirmaSenha;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmailAddress);
        editTextSenha = findViewById(R.id.editNovaTextSenha);
        editTextConfirmaSenha = findViewById(R.id.editTextConfirmaSenha);
        firebaseAuth = FirebaseAuth.getInstance();

        btnLogin = findViewById(R.id.buttonIrParaLogin);
        btnCadastrar = findViewById(R.id.buttonCriarConta);

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criarNovoUsuario(view);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (MainActivity.this, Login.class);
                startActivity(intent);
            }
        });

    }

    private void criarNovoUsuario(View view){
        final String login = editTextEmail.getEditableText().toString();
        final String pass = editTextSenha.getEditableText().toString();
        String pass2 = editTextConfirmaSenha.getEditableText().toString();
        final String nome = editTextNome.getEditableText().toString();

        if(validateFields(login,pass,pass2,nome)){
            if(Patterns.EMAIL_ADDRESS.matcher(login).matches()){
                if(validateMinLengthPassword(pass,pass2)){
                    if(validateEqualPasswords(pass,pass2)){
                        firebaseAuth.createUserWithEmailAndPassword(login,pass2).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(MainActivity.this, getString(R.string.registration_successful),
                                            Toast.LENGTH_SHORT).show();

                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    String userId = user.getUid();
                                    writeNewUser(userId, nome, login, pass);
                                    FirebaseAuth.getInstance().signOut();
                                    goToLogin();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(MainActivity.this, getString(R.string.registration_failure),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(MainActivity.this, getString(R.string.different_password_warning),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, getString(R.string.min_length_password_warning),
                            Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this, getString(R.string.email_invalido),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(MainActivity.this, getString(R.string.empty_fields_warning),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void goToMenu(){
        startActivity (new Intent(this, Tela_Inicial.class));
        finish();
    }

    public void goToLogin(){
        startActivity (new Intent(this, Login.class));
    }

    public boolean validateFields(String login, String password1, String password2, String name){
        if(login.isEmpty() || password1.isEmpty() || password2.isEmpty() || name.isEmpty()){
            return false;
        }
        else{
            return true;
        }
    }

    public boolean validateEqualPasswords(String password1, String password2){
        if(password1.equals(password2)){
            return true;
        }else{
            return false;
        }
    }

    public boolean validateMinLengthPassword(String password1, String password2){
        if(password1.length() > 7 || password2.length() > 7){
            return true;
        }else{
            return false;
        }
    }


    private void writeNewUser(String userId, String name, String email, String password){
        //usando o mesmo UID do Firebase Authentication: userId

        try {//tentando cadastrar no banco
            User user = new User(name, email, password, false);
            // variável de acesso ao RealTime DataBase
            DatabaseReference refUser = FirebaseDatabase.getInstance().getReference();
            refUser.child("users").child(userId).setValue(user);

        }
        catch(DatabaseException e){
            Toast.makeText(MainActivity.this, e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


}