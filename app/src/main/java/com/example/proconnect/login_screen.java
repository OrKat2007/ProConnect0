package com.example.proconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ButtonBarLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class login_screen extends AppCompatActivity implements View.OnClickListener {

    EditText etEmail, etpassword;
    Button login, signup;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
       mAuth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etpassword = findViewById(R.id.etPassWord);
        login = findViewById(R.id.btnLogIn);
        signup = findViewById(R.id.btnSignUp);
        login.setOnClickListener(this);
        signup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == login){
            String email = etEmail.getText().toString().trim();
            String password = etpassword.getText().toString().trim();

            try {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(login_screen.this, "Authentication Success", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(login_screen.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String errorMessage;
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthInvalidUserException e) {
                                        errorMessage = "This email is not registered.";
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        errorMessage = "Incorrect email or password.";
                                    } catch (Exception e) {
                                        errorMessage = "Authentication failed. Please try again.";
                                    }
                                    Toast.makeText(login_screen.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(login_screen.this, "An error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (v == signup){
            Intent intent = new Intent(login_screen.this, sign_up.class);
            startActivity(intent);
        }
    }
}