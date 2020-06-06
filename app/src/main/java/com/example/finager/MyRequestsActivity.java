package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRequestsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String userID;
    private Request request;
    private String TAG = "MyRequestsActivity";
    private String username;
    private static ArrayList<Request> requestList;
    private static ArrayList<String> requestID;
    private MyRequestsRecyclerViewAdapter myRequestsRecyclerViewAdapter;
    private String members;
    private String members2;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyGroups;
    private DatabaseReference reffMembers;
    private DatabaseReference reffRequests;
    private DatabaseReference reffUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        firebaseItemsInitialization();

        activityLayoutItemsInitialization();

        getUsername();

        getRequests();

    }

    private void getRequests() {
        request = new Request();

        reffRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList = new ArrayList<Request>();
                requestID = new ArrayList<String>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    request = ds.getValue(Request.class);
                        if (username.equals(request.getName()) && request.getAnswer() == 0) {
                            requestList.add(request);
                            requestID.add(ds.getKey());
                        }
                }
                myRequestsRecyclerViewAdapter = new MyRequestsRecyclerViewAdapter(MyRequestsActivity.this, requestList);
                recyclerView.setAdapter(myRequestsRecyclerViewAdapter);

                myRequestsRecyclerViewAdapter.setOnItemClickListener(new MyRequestsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onYesClick(int position) {
                        addUserToGroup(position);
                        sendUserToMyRequestsActivity();
                    }

                    @Override
                    public void onNoClick(int position) {
                        deleteRequest(position);
                        sendUserToMyRequestsActivity();
                    }
                });

                if (requestList.size() == 0) {
                    showNoRequestsToShowAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getGroups:onCancelled", databaseError.toException());
            }
        });
    }

    private void showNoRequestsToShowAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyRequestsActivity.this);
        alertDialog.setTitle("Requests");
        alertDialog.setMessage("There is no requests");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendUserToMainActivity();
            }
        });
        alertDialog.create().show();
    }

    private void addUserToGroup(final int position) {
        reffMembers.child(requestList.get(position).getGroup_id()).push().setValue(username);

        reffMyGroups.child(requestList.get(position).getGroup_id()).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) members = (String) dataSnapshot.getValue();
                Log.d("MEMBERS", members);
                if (members != null) {
                    members += ", " + username;
                    Log.d("MEMBERS_2", members + "  " + requestID.get(position));
                    reffMyGroups.child(requestList.get(position).getGroup_id()).child("members").setValue(members);
                    reffRequests.child(requestID.get(position)).child("answer").setValue(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void deleteRequest(int position) {
        reffRequests.child(requestID.get(position)).removeValue();
    }

    private void sendUserToMyRequestsActivity() {
        Intent intent = new Intent(MyRequestsActivity.this, MyRequestsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(MyRequestsActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void getUsername() {
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

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        reffMyGroups = database.getReference().child("myGroups");
        reffMembers = database.getReference().child("members");
        reffUsers = database.getReference().child("users");
        reffRequests = database.getReference().child("requests");
    }

    private void activityLayoutItemsInitialization() {
        recyclerView = findViewById(R.id.recyclerViewRequest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
    }
}
