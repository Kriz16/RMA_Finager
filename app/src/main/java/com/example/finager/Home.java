package com.example.finager;

import android.app.Application;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
/*
Pošto klasa Home extenda Application, ova klasa nam provjerava trenutni session firebase korisnika.
Ako korisnik postoji (nije se logouta) pokrenuti ćemo novu aktivnost odnosno preusmjeriti ga u MainActivity.
 */

public class Home extends Application {
    @Override
    public void onCreate(){
        super.onCreate();

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
            Intent intent = new Intent(Home.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

}
