package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MyCategoriesActivity extends AppCompatActivity {

    private String userID;
    private RecyclerView recyclerView;
    private Category category;
    private ArrayList<Category> categoryList;
    private MyCategoriesRecyclerViewAdapter myCategoryAdapter;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyCategories;

    private TextView probaCategorijeTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_categories);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        category = new Category();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getUid();

        database = FirebaseDatabase.getInstance();

        //Text View koristen za ispis vrijednosti za debug
        probaCategorijeTV = (TextView) findViewById(R.id.probaCategorijeTV);

        reffMyCategories = database.getReference().child("myCategories");
        reffMyCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                categoryList = new ArrayList<Category>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (userID.equals(category.getUserID())) {
                        categoryList.add(category);
                    }
                }
                myCategoryAdapter = new MyCategoriesRecyclerViewAdapter(MyCategoriesActivity.this, categoryList);
                recyclerView.setAdapter(myCategoryAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



}
