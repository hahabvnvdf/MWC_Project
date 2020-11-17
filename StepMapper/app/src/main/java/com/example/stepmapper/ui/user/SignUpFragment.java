package com.example.stepmapper.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.stepmapper.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class SignUpFragment extends AppCompatActivity {
    private EditText emailSU, passwordSU, passwordSU2;
    private Button signUpButton;
    private TextView signInTv;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);
        firebaseAuth = FirebaseAuth.getInstance();
        emailSU = findViewById(R.id.emailLogin);
        passwordSU = findViewById(R.id.passwordLogin);
        passwordSU2 = findViewById(R.id.passwordLogin2);
        signUpButton = findViewById(R.id.signUpButton);
        progressDialog = new ProgressDialog(this);
        signInTv = findViewById(R.id.signInTv);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register();
            }
        });
        signInTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpFragment.this, LoginFragment.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void Register() {
        String email = emailSU.getText().toString();
        String password = passwordSU.getText().toString();
        String password2 = passwordSU2.getText().toString();

        if (TextUtils.isEmpty(email)){
            emailSU.setError("Enter your email");
            return;
        }
        else if (TextUtils.isEmpty(password)){
            passwordSU.setError("Enter your password");
            return;
        }
        else if (TextUtils.isEmpty(password2)){
            passwordSU2.setError("Enter your confirm password");
            return;
        }
        else if (!password.equals(password2)){
            passwordSU2.setError("Password does not match");
            return;
        }
        else if (password.length()<6){
            passwordSU.setError("Password length require >=6");
            return;
        }
        else if (!isValidEmail(email)){
            emailSU.setError("Invalid email");
            return;
        }
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUpFragment.this, "Successfull Register", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignUpFragment.this, LoginFragment.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(SignUpFragment.this, "Failed to Register", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }
    private Boolean isValidEmail(CharSequence target){
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
