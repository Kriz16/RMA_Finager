package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    private EditText mFullName, mEmail, mPassword;
    private Button mRegistrerBtn;
    private TextView mLoginBtn;
    private FirebaseAuth fAuth;
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private String userID;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fAuth = FirebaseAuth.getInstance();//uzimamo instancu baze kako bi mogli izvoditi operacije s bazom
        firebaseUser = fAuth.getCurrentUser();

        if (firebaseUser != null) {
            sendUserToMainActivity();
        }

        setContentView(R.layout.activity_registration);

        mFullName = findViewById(R.id.fullNameET);
        mEmail = findViewById(R.id.emailET);
        mPassword = findViewById(R.id.passwordET);
        mRegistrerBtn = findViewById(R.id.loginBtn);
        mLoginBtn = findViewById(R.id.createTextTV);

        database = FirebaseDatabase.getInstance();//instanciramo firebase Realtime database
        progressBar = findViewById(R.id.progressBar);

        if (fAuth.getCurrentUser() != null) { //ako je korisnik već prijavljen, ne dopuštamo mu da uđe u registraciju već ga preusmjeravamo u MainActivity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mRegistrerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRegistration();
            }
        });

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });


    }

    public void userRegistration() {
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String fullName = mFullName.getText().toString();

        if (TextUtils.isEmpty(email)){ //provjera ako je korisnik ostavio prazan email
            mEmail.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is required.");
            return;
        }

        if (password.length() < 6) { //provjera duljine passworda
            mPassword.setError("Password too short.");
        }

        progressBar.setVisibility(View.VISIBLE);

        //registracija korisnika u firebase
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) { //ako je registracija uspjesno obavljena, korisnika se preusmjerava u MainActivity
                    Toast.makeText(RegistrationActivity.this, "User created.", Toast.LENGTH_SHORT).show();

                    //spremanje korisnika u bazu podataka
                    userID = fAuth.getCurrentUser().getUid(); //uzimam user ID
                    DatabaseReference userRef = database.getReference("users").child(userID);
                    User user = new User(fullName, email);
                    userRef.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: user Profile is created for "+ userID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: " + e.toString());
                        }
                    });

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegistrationActivity.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
    public void sendUserToMainActivity(){
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendUserToLoginActivity(){
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    @Override //ako korisnik pritisne u Constraint layout miče se tipkovnica iz fokusa
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(RegistrationActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}
