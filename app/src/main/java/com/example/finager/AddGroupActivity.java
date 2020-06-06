package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.print.PrinterId;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChartView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.ChipInterface;

import java.util.ArrayList;
import java.util.List;

public class AddGroupActivity extends AppCompatActivity {
    private String TAG = "AddGroupActivity";
    private String userID;
    private EditText groupNameET;
    private EditText descriptionET;
    private MultiAutoCompleteTextView membersMACTV;
    private Button cancelBTN;
    private Button addBTN;
    private Group group;
    private User user;
    private long maxgroupID = 0;
    private long maxrequestID = 0;
    private String username = "";
    private boolean flag;
    private Request request;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyGroupID;
    private DatabaseReference reffMyGroups;
    private DatabaseReference reffUsers;
    private DatabaseReference reffMembers;
    private DatabaseReference reffMyRequestID;
    private DatabaseReference reffRequests;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        firebaseItemsInitialization();

        activityLayoutItemsInitialization();

        getMaxGroupID();

        getMaxRequestID();

        getUsername();

        //addMemeberBTN();

        setButtons();

        /*
        TREBA STORIT: nacin dodavanja clanova u bazu (kako spremit u bazu),
        dodavanje korisnika kao chipseve, napravit tablicu zahtjvi i implementirat dodavanje zahtjeva prilikom stvaranja grupe
        - provjerit dal korisnici koji su odabrani postoje u bazi
         */
    }

    /*private void addMemeberBTN() {
        addMemeberBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] tags = membersMACTV.getText().toString().split(" ");
                LayoutInflater layoutInflater = LayoutInflater.from(AddGroupActivity.this);
                for (String s : tags){
                    Chip chip = (Chip) layoutInflater.inflate(R.layout.chip_item, null, false);
                    chip.setOnCloseIconClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            membersChip.removeView(v);
                        }
                    });
                    membersChip.addView(chip);
                }
                StringBuilder result = new StringBuilder("");
                for (int i = 0; i < membersChip.getChildCount(); i++) {
                    Chip chip = (Chip) membersChip.getChildAt(i);
                    if (chip.isChecked()) {
                        if (i < membersChip.getChildCount() - 1) {
                            result.append(chip.getText()).append(",");
                        } else {
                            result.append(chip.getText());
                        }
                    }
                }
                Toast.makeText(AddGroupActivity.this, ""+result.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void showInput() {
        String input = membersMACTV.getText().toString().trim();
        String[] singleInputs = input.split("\\s*,\\s*");

        singleInputs = removeDuplicateMembers(singleInputs);
        String toastText = "";

        /*for (int i = 0; i < singleInputs.length; i++) {
            toastText += "Item " + i + ": " + singleInputs[i] + "\n";
        }
        Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();*/

        //AKO SE KORISNIK NE POSTOJI U BAZI PODATAKA ONDA GA NE DODAJE U BAZU
        final String[] finalSingleInputs = singleInputs;
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
                    if (flag) {
                        membersArr.add(member);
                    }
                }
                for (int i = 0; i < membersArr.size(); i++) {
                    toastMessage += "Item " + i + ": " + membersArr.get(i) + "\n";
                }
                Toast.makeText(AddGroupActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addGroupToDatabase() {
        String name = groupNameET.getText().toString().trim();
        String description = descriptionET.getText().toString().trim();
        String members_string = membersMACTV.getText().toString().trim();
        flag = true;

        //final String[] members = members_string.split(" ");
        String[] memberArray = members_string.split("\\s*,\\s*");

        memberArray = removeDuplicateMembers(memberArray);

        if ("".equals(name)) {
            groupNameET.setError("Group name is required!");
            flag = false;

        }

        if ("".equals(memberArray)) {
            membersMACTV.setError("Group members are required!");
            flag = false;
        }

        if (flag) {
            Log.d("ISPIS VRIJEDNOSTI", name + " " + description + " " + memberArray);
            //final String[] members2 = {"ana1", "leo1", "leo"};
            //Log.d("ISPIS VRIJEDNOSTI", name + " " + description + " " + members + " " + members2);

            //members_string += " "+username;
            Group grupa = new Group(name, description, username, username);
            reffMyGroups.child(String.valueOf(maxgroupID + 1)).setValue(grupa);

            Log.w(TAG, "onDataChange: dodavanje grupe u bazi");
            final long id_grupe = maxgroupID + 1; //var dodana jer se prije izvrsi donja naredba gdje se poveca maxgoupID u bazi pa mi spremi group memebere za jedan id veci
            reffMembers.child(String.valueOf(id_grupe)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    /*for (int i = 0; i < members.length; i++) {
                        reffMembers.child(String.valueOf(id_grupe)).child(String.valueOf(i)).setValue(members[i]);
                    }*/
                    reffMembers.child(String.valueOf(id_grupe)).push().setValue(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "onCancelled: spremanje membera", databaseError.toException());
                }
            });
            reffMyGroupID.setValue(maxgroupID + 1);
            Log.w(TAG, "onDataChange: povecavanje ida u bazi");

            //AKO SE KORISNIK NE POSTOJI U BAZI PODATAKA ONDA GA NE DODAJE U BAZU
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
                        if (flag) {
                            membersArr.add(member);
                        }
                    }

                    for (int i = 0; i < membersArr.size(); i++) {
                        toastMessage += "Item " + i + ": " + membersArr.get(i) + "\n";
                        request = new Request();
                        request.setGroup_id(String.valueOf(maxgroupID));
                        request.setInvited_by(username);
                        request.setName(membersArr.get(i));
                        request.setAnswer(0);
                        reffRequests.child(String.valueOf(maxrequestID)).setValue(request);
                        maxrequestID += 1;
                    }
                    reffMyRequestID.setValue(maxrequestID);
                    Toast.makeText(AddGroupActivity.this, toastMessage, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            sendUserToMainActivity();
        }
    }

    private void setButtons() {
        addBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addGroupToDatabase();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMainActivity();
            }
        });
    }

    private void activityLayoutItemsInitialization() {
        groupNameET = (EditText) findViewById(R.id.groupNameET);
        descriptionET = (EditText) findViewById(R.id.descriptionET);
        membersMACTV = (MultiAutoCompleteTextView) findViewById(R.id.membersMACTV);
        cancelBTN = (Button) findViewById(R.id.cancelBTN);
        addBTN = (Button) findViewById(R.id.addBTN);
        //membersChip = (ChipGroup) findViewById(R.id.membersChipGroup);
        //membersChip = (ChipsInput) findViewById(R.id.membersChip);
        //addMemeberBTN = (Button) findViewById(R.id.addMemberBTN);
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        reffMyGroups = database.getReference().child("myGroups");
        reffMyGroupID = database.getReference().child("myFinancesBillID").child("id_group");
        reffUsers = database.getReference().child("users");
        reffMembers = database.getReference().child("members");
        reffMyRequestID = database.getReference().child("myFinancesBillID").child("id_request");
        reffRequests = database.getReference().child("requests");

        setSuggestionsForMembers();
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

    private void getMaxGroupID(){
        reffMyGroupID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxgroupID = (long) dataSnapshot.getValue();
                //Log.d("id_category value:", String.valueOf(maxgroupID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getMaxGroupID:onCancelled", databaseError.toException());
            }
        });
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

    private void setSuggestionsForMembers() {
        reffUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> memList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    user = ds.getValue(User.class);
                    if (!userID.equals(ds.getKey())) {
                        memList.add(user.getName());
                    }
                }
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(AddGroupActivity.this, android.R.layout.simple_expandable_list_item_1, memList);
                membersMACTV.setAdapter(actvAdapter);
                membersMACTV.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "activityLayoutItemInitialization:onCancelled", databaseError.toException());
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

    private void sendUserToMainActivity(){
        Intent intent = new Intent(AddGroupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
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
