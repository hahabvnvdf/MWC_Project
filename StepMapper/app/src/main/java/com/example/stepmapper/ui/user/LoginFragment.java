package com.example.stepmapper.ui.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.stepmapper.MainActivity;
import com.example.stepmapper.R;
import com.example.stepmapper.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class LoginFragment extends AppCompatActivity {
    private EditText emailSI, passwordSI;
    private Button signInButton;
    private TextView signUpTv;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);
        firebaseAuth = FirebaseAuth.getInstance();
        emailSI = findViewById(R.id.emailLogin);
        passwordSI = findViewById(R.id.passwordLogin);
        progressDialog = new ProgressDialog(this);
        signInButton = findViewById(R.id.loginButton);
        signUpTv = findViewById(R.id.signUpTv);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login();
            }
        });
        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginFragment.this, SignUpFragment.class);
                startActivity(intent);
                finish();
            }
        });
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    private void Login() {
        String email = emailSI.getText().toString();
        String password = passwordSI.getText().toString();

        if (TextUtils.isEmpty(email)){
            emailSI.setError("Enter your email");
            return;
        }
        else if (TextUtils.isEmpty(password)){
            passwordSI.setError("Enter your password");
            return;
        }
        else if (password.length()<6){
            passwordSI.setError("Password length require >=6");
            return;
        }
        else if (!isValidEmail(email)){
            emailSI.setError("Invalid email");
            return;
        }

        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        progressDialog.setCanceledOnTouchOutside(false);
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LoginFragment.this, "Successfull Login", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(LoginFragment.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Exception errorCode = task.getException();
                    Log.e("Signin Error", String.valueOf(errorCode));
                }
                progressDialog.dismiss();
            }
        });
    }
    private Boolean isValidEmail(CharSequence target){
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
}
