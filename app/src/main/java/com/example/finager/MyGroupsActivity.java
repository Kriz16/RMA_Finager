package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyGroupsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String userID;
    private Group group;
    private String TAG = "MyGroupsActivity";
    private String username;
    private static ArrayList<Group> groupList;
    private static ArrayList<String> groupID;
    private MyGroupsRecyclerViewAdapter myGroupsRecyclerViewAdapter;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyGroups;
    private DatabaseReference reffMembers;
    private DatabaseReference reffUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseItemsInitialization();

        activityLayoutItemsInitialization();

        getUsername();

        getGroups();

    }


    private void getGroups() {
        group = new Group();

        reffMyGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupList = new ArrayList<Group>();
                groupID = new ArrayList<String>();
                String username2;
                String[] members; /*= {"ana", "ana1", "leo", "leo1"};*/
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    group = ds.getValue(Group.class);
                    username2 = group.getMembers().toString();
                    Log.d(TAG, "onDataChange: " + username2);
                    members = username2.split("\\s*,\\s*");
                    for (int i = 0; i < members.length; i++) {
                        if (username.equals(members[i])) {
                            Log.d(TAG, "onDataChange: MEMBER[i] " + members[i]);
                            groupList.add(group);
                            groupID.add(ds.getKey());
                        }
                    }
                }
                myGroupsRecyclerViewAdapter = new MyGroupsRecyclerViewAdapter(MyGroupsActivity.this, groupList);
                recyclerView.setAdapter(myGroupsRecyclerViewAdapter);

                myGroupsRecyclerViewAdapter.setOnItemClickListener(new MyGroupsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        Toast.makeText(MyGroupsActivity.this, String.valueOf(position) + " clicked", Toast.LENGTH_SHORT).show();
                        sendUserToSelectedGroup(position);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getGroups:onCancelled", databaseError.toException());
            }
        });

        /*reffMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> member = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.getKey();
                    if (uid.equals(username)) {
                        member.add(uid);
                        Log.d(TAG, "MEMBERS " + member);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getGroups:onCancelled", databaseError.toException());
            }
        });*/
    }

    private void sendUserToSelectedGroup(int position) {
        Intent intent = new Intent(MyGroupsActivity.this, GroupActivity.class);
        intent.putExtra("group_id" , groupID.get(position));
        startActivity(intent);
    }

    private void activityLayoutItemsInitialization() {
        recyclerView = findViewById(R.id.recyclerViewGroup);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        reffMyGroups = database.getReference().child("myGroups");
        reffMembers = database.getReference().child("members");
        reffUsers = database.getReference().child("users");
    }

    private void getUsername(){
        reffUsers.child(userID+"/name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) username = (String) dataSnapshot.getValue();
                Log.d("USERNAME value:", username);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent intent = new Intent(MyGroupsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
    }
}
