package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyCategoryBillsActivity extends AppCompatActivity {
    private TextView myCategoryBillsTV;
    private static final String TAG = "MyCategoryBillsActivity";
    private static String category_name;
    private static String userID;
    private static Bill bill;
    private static RecyclerView recyclerView;
    private static ArrayList<Bill> billList;
    private MyCategoryBillsRecyclerViewAdapter myCategoryBillsAdapter;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyFinances;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_category_bills);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();

        category_name = getIcomingIntent();

        bill = new Bill();

        /*myCategoryBillsTV = (TextView) findViewById(R.id.myCategoryBillsTV);
        myCategoryBillsTV.setText(category_name);*/

        reffMyFinances = database.getReference().child("myFinances");
        reffMyFinances.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                billList = new ArrayList<Bill>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (userID.equals(bill.getUserID()) && category_name.equals(bill.getCategory())) {
                        billList.add(bill);
                    }
                }
                myCategoryBillsAdapter = new MyCategoryBillsRecyclerViewAdapter(MyCategoryBillsActivity.this, billList);
                recyclerView.setAdapter(myCategoryBillsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public String getIcomingIntent(){
        String ime_kat = "";
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        if (getIntent().hasExtra("category")) {
            ime_kat = getIntent().getStringExtra("category");
        }
        return ime_kat;
    }
}
