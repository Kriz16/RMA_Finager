package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
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

public class EditGroupActivity extends AppCompatActivity {
    private String TAG = "EditGroupActivity";
    private String userID;
    private AutoCompleteTextView adminET;
    private EditText groupNameET;
    private EditText descriptionET;
    private MultiAutoCompleteTextView membersMACTV;
    private Button cancelBTN;
    private Button saveBTN;
    private Group group;
    private User user;
    private long maxrequestID = 0;
    private String username = "";
    private String group_id;
    private boolean flag;
    private boolean flag2;
    private Request request;
    private String currentMembers = "";

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyGroups;
    private DatabaseReference reffUsers;
    private DatabaseReference reffMembers;
    private DatabaseReference reffMyRequestID;
    private DatabaseReference reffRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        group_id = getIncomingIntent("group_id");
        username = getIncomingIntent("username");
        currentMembers = getIncomingIntent("currentMembers");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseItemsInitialization();

        activityLayoutItemsInitialization();

        String[] currentMemberArray = currentMembers.split("\\s*,\\s*");

        getMaxRequestID();

        getDataFromDatabaseToEditText();

        setButtons();

    }

    private void setButtons() {
        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesToDatabase();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToGroupActivity();
            }
        });
    }

    private void saveChangesToDatabase() {
        final String admin = adminET.getText().toString().trim();
        final String name = groupNameET.getText().toString().trim();
        final String description = descriptionET.getText().toString().trim();
        String members_string = membersMACTV.getText().toString().trim();
        flag = true;

        String[] memberArray = members_string.split("\\s*,\\s*");

        memberArray = removeDuplicateMembers(memberArray);

        final String[] adminArray = admin.split("\\s*,\\s*");

        if ("".equals(admin)) { //provjera dal je ostavljeno prazno polje
            adminET.setError("Admin is required!");
            flag = false;
        } else if (adminArray.length > 1) { //provjera dal je unesen veci broj korisnika
            adminET.setError("Enter only one admin!");
            flag = false;
        } /*else { //provjera dal uneseni korisnik nije clan grupe
            reffMembers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        flag2 = false;
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String is_member = ds.getValue(String.class);
                            if (adminArray[0].equals(is_member)) {
                                flag2 = true;
                            }
                        }
                        if (!flag2) {
                            adminET.setError("Admin should be a member of the group!");
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }*/

        if ("".equals(name)) {
            groupNameET.setError("Group name is required!");
            flag = false;
        }

        if ("".equals(members_string)) {
            membersMACTV.setError("Group members are required!");
            flag = false;
        }


        if (flag) {
            //treba provjerit dal je admin samo jedan, dal postoji medju trenutnim clanovima ako nije ok onda treba ispisat upozorenje DONE
            //treba provjerit dal je izbacen koj korisnik iz grupe
            //sve korisnike koji su dodani treba splitat, onda za svakeg korisnika provjerit dal vec postoji u bazi ako ne postoji onda ga dodat (zahtjev)
            // ..treba provjerit isto dal je koj korisnik izbacen (prilokom otvaranja aktivitia uzmem members i splitan ih u neko polje i onda to napravin
            // funkciju posebnu koja ce provjerit dal su mi ti korisnici medju ovima koji su sad dodani)
            final String[] currentMemberArray = currentMembers.split("\\s*,\\s*");
            final String[] finalSingleInputs = memberArray;
            reffUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String toastMessage = "";
                    ArrayList<String> membersArr = new ArrayList<>();
                    for (int i = 0; i < finalSingleInputs.length; i++) {
                        flag = false;
                        String member = finalSingleInputs[i];
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            user = ds.getValue(User.class);
                            if (member.equals(user.getName())) {
                                flag = true;
                            }
                        }
                        if (flag) { //ako korisnik postoji onda ga doda u array
                            membersArr.add(member);
                            //toastMessage += member + " ";
                        }
                    }

                    flag2 = false;
                    ArrayList<String> za_brisanje = new ArrayList<>();
                    for (int j = 0; j < currentMemberArray.length; j++) {
                        //toastMessage += currentMemberArray[j] + " ";
                        flag2 = false;
                        for (int i = 0; i < membersArr.size(); i++) {
                            if (membersArr.get(i).equals(currentMemberArray[j])) {
                               //toastMessage += membersArr.get(i) + " " + currentMemberArray[j] + "\n";
                                flag2 = true;
                            }
                        }
                        if (!flag2) {
                            if (!username.equals(currentMemberArray[j]) && !admin.equals(currentMemberArray[j])) {
                                za_brisanje.add(currentMemberArray[j]);
                                Log.d("ZA BRISANJE", currentMemberArray[j]);
                            }
                        }
                    }

                    for (int i = 0; i < za_brisanje.size(); i++) {
                        //toastMessage += za_brisanje.get(i) + "\n";
                        deleteUserFromGroup(za_brisanje.get(i));
                    }


                    flag = false;
                    ArrayList<String> za_request = new ArrayList<>();
                    for (int i = 0; i < membersArr.size(); i++) {
                        flag = false;
                        for (int j = 0; j < currentMemberArray.length; j++) {
                            if (membersArr.get(i).equals(currentMemberArray[j])) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            za_request.add(membersArr.get(i));
                        }
                    }

                    for (int i = 0; i < za_request.size(); i++) {
                        //toastMessage += za_request.get(i) + "\n";
                        Log.d("ZA REQUEST", za_request.get(i));
                        final String member = za_request.get(i);
                        reffRequests.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    flag = false;
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        request = ds.getValue(Request.class);
                                        if (member.equals(request.getName()) && group_id.equals(request.getGroup_id())) {
                                            flag = true;
                                        }
                                    }
                                    if (!flag) {
                                        Log.d("KEMU SALJEM REQUEST", member);
                                        request = new Request();
                                        request.setGroup_id(group_id);
                                        request.setInvited_by(username);
                                        request.setName(member);
                                        request.setAnswer(0);
                                        reffRequests.child(String.valueOf(maxrequestID)).setValue(request);
                                        maxrequestID += 1;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                    }
                    reffMyRequestID.setValue(maxrequestID);
                    Toast.makeText(EditGroupActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            reffMyGroups.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        group = dataSnapshot.getValue(Group.class);
                        String novi_admin = username;
                        String members = group.getMembers();
                        String[] memberArray = members.split("\\s*,\\s*");
                        for (int i = 0; i < memberArray.length; i++) {
                            if (admin.equals(memberArray[i])) {
                                novi_admin = memberArray[i];
                            }
                        }
                        /*ArrayList<String> adminMembersArr = new ArrayList<>();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String member = ds.getValue(String.class);
                            adminMembersArr.add(member);
                            if (admin.equals(member)) {
                                novi_admin = member;
                            }
                        }*/
                        group.setAdmin(novi_admin);
                        group.setDescription(description);
                        group.setName(name);
                        reffMyGroups.setValue(group);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            sendUserToGroupActivity();
        }
    }

    private void deleteUserFromGroup(final String username) {
        //brise korisnika iz member
        Log.d("TUUU SAN", "TU SAN");
        reffMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String member;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    member = ds.getValue(String.class);
                    if (username.equals(member)) {
                        Log.d("MEMBER ZA BRISANJE", member);
                        reffMembers.child(ds.getKey()).removeValue();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteUserFromGroup:onCancelled", databaseError.toException());
            }
        });

        //brise korisnikov request iz baze
        reffRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    request = ds.getValue(Request.class);
                    if (username.equals(request.getName()) && group_id.equals(request.getGroup_id())) {
                        Log.d("REQUEST ZA BRISANJE", ds.getKey() + " " + request.getName() + " " + request.getGroup_id());
                        reffRequests.child(ds.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteUserFromGroup:onCancelled", databaseError.toException());
            }
        });

        //brise korisnika iz myGroups/members
        reffMyGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) group = dataSnapshot.getValue(Group.class);
                String members = group.getMembers();
                Log.d("MEMBERS BEFORE REMOVE", members);
                String[] memberArray = members.split("\\s*,\\s*");
                List<String> removeMemberArray = new ArrayList<String>();
                for (int i = 0; i < memberArray.length; i++) {
                    if (memberArray[i].equals(username)) {}
                    else {
                        removeMemberArray.add(memberArray[i]);
                    }
                }
                members = "";
                for (int i = 0; i < removeMemberArray.size(); i++) {
                    if (i == (removeMemberArray.size()-1)) {
                        members += removeMemberArray.get(i);
                        //flag = true;
                    } else {
                        members += removeMemberArray.get(i) + ", ";
                    }
                }
                Log.d("MEMBERS AFTER REMOVE", members);
                group.setMembers(members);
                Log.d("GRUPA", group.getAdmin() + " " + group.getMembers() + " " + group.getName() + " " + group.getDescription());
                reffMyGroups.setValue(group);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String[] removeDuplicateMembers(String[] members) {
        ArrayList<String> listMembers = new ArrayList<String>();

        for(int i=0; i < members.length; i++){

            if( !listMembers.contains(members[i]) ){
                listMembers.add(members[i]);
            }
        }

        members = listMembers.toArray( new String[listMembers.size()] );
        return members;
    }

    private void getDataFromDatabaseToEditText() {
        reffMyGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) group = dataSnapshot.getValue(Group.class);
                adminET.setText(group.getAdmin());
                groupNameET.setText(group.getName());
                descriptionET.setText(group.getDescription());
                String members = group.getMembers();
                members += ", ";
                membersMACTV.setText(members);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getDataFromDatabaseToEditText:onCancelled", databaseError.toException());
            }
        });
    }

    private void activityLayoutItemsInitialization() {
        adminET = (AutoCompleteTextView) findViewById(R.id.adminET);
        groupNameET = (EditText) findViewById(R.id.groupNameET);
        descriptionET = (EditText) findViewById(R.id.descriptionET);
        membersMACTV = (MultiAutoCompleteTextView) findViewById(R.id.membersMACTV);
        cancelBTN = (Button) findViewById(R.id.cancelBTN);
        saveBTN = (Button) findViewById(R.id.saveBTN);
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        reffMyGroups = database.getReference().child("myGroups").child(group_id);
        reffUsers = database.getReference().child("users");
        reffMembers = database.getReference().child("members").child(group_id);
        reffMyRequestID = database.getReference().child("myFinancesBillID").child("id_request");
        reffRequests = database.getReference().child("requests");

        setSuggestionsForAdmin();

        setSuggestionsForMembers();
    }

    private void setSuggestionsForAdmin() {
        reffMembers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String member;
                ArrayList<String> memList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    member = ds.getValue(String.class);
                    memList.add(member);
                }
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(EditGroupActivity.this, android.R.layout.simple_expandable_list_item_1, memList);
                adminET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "activityLayoutItemInitialization:onCancelled", databaseError.toException());
            }
        });
    }

    private void setSuggestionsForMembers() {
        reffUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> memList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    user = ds.getValue(User.class);
                    memList.add(user.getName());
                }
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(EditGroupActivity.this, android.R.layout.simple_expandable_list_item_1, memList);
                membersMACTV.setAdapter(actvAdapter);
                membersMACTV.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "activityLayoutItemInitialization:onCancelled", databaseError.toException());
            }
        });
    }

    private void sendUserToGroupActivity() {
        Intent intent = new Intent(EditGroupActivity.this, GroupActivity.class);
        intent.putExtra("group_id", group_id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);

    }

    private void getMaxRequestID(){
        reffMyRequestID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxrequestID = (long) dataSnapshot.getValue();
                Log.d("id_request value:", String.valueOf(maxrequestID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getMaxGroupID:onCancelled", databaseError.toException());
            }
        });
    }

    private String getIncomingIntent(String key){
        String value = "";
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        if (getIntent().hasExtra(key)) {
            value = getIntent().getStringExtra(key);
        }
        return value;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToGroupActivity();
    }

    @Override //ako korisnik pritisne u Constraint layout miče se tipkovnica iz fokusa
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(EditMyBillActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
