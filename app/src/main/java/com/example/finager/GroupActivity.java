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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.core.annotations.Line;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private String userID;
    private Group group;
    private Request request;
    private Category category;
    private float total_income;
    private float total_expense;
    private String TAG = "GroupActivity";
    private String group_id;
    private String username;

    private TextView membersTV;
    private TextView descriptionTV;
    private TextView nameTV;
    private TextView adminTV;
    private TextView incomeTV;
    private TextView expenseTV;
    private TextView totalAmountTV;

    private FloatingActionsMenu addBillFAB;
    private FloatingActionButton incomeFAB;
    private FloatingActionButton expenseFAB;
    private FloatingActionButton groupFAB;

    private Button showCategoriesBTN;
    private Button editGroupBTN;
    private Button deleteGroupBTN;
    private Button leaveGroupBTN;

    private LinearLayout pieChartLinLay;
    private AnyChartView shortReviewPie;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyGroups;
    private DatabaseReference reffMembers;
    private DatabaseReference reffUsers;
    private DatabaseReference reffMyCategoriesGroup;
    private DatabaseReference reffRequests;
    private DatabaseReference reffMyFinancesGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        group_id = getIncomingIntent("group_id");

        firebaseItemsInitialization();

        activityLayoutItemsInitialization();

        getUsername();

        showGroupInfo();

        setUpButtons();

        showShortReviewData();

        setUpFABMenu();

    }

    private void setUpButtons() {
        reffMyGroups.child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    group = dataSnapshot.getValue(Group.class);
                    if (username.equals(group.getAdmin())) {
                        leaveGroupBTN.setVisibility(View.GONE);
                    } else {
                        editGroupBTN.setVisibility(View.GONE);
                        deleteGroupBTN.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setUpButtons:onCancelled", databaseError.toException());
            }
        });

        showCategoriesBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, GroupCategoriesActivity.class);
                intent.putExtra("group_id", group_id);
                startActivity(intent);
            }
        });

        leaveGroupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLeaveGroupAlertDialog();
            }
        });

        deleteGroupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteGroupAlertDialog();
            }
        });

        editGroupBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToEditGroup();
            }
        });
    }

    private void sendUserToEditGroup() {
        reffMyGroups.child(group_id).child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Intent intent = new Intent(GroupActivity.this, EditGroupActivity.class);
                intent.putExtra("group_id", group_id);
                intent.putExtra("username", username);
                if (dataSnapshot.exists()) intent.putExtra("currentMembers", (String) dataSnapshot.getValue());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                finish();
                startActivity(intent);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "sendUserToEditGroup:onCancelled", databaseError.toException());
            }
        });
    }

    private void  showDeleteGroupAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupActivity.this);
        alertDialog.setTitle("Delete Group");
        alertDialog.setMessage("Are you sure you want to delete the group?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteGroup();
                sendUserToMyGroupsActivity();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.setCancelable(true);
            }
        });
        alertDialog.create().show();
    }

    private void deleteGroup() {
        reffMyCategoriesGroup.removeValue();
        reffMyFinancesGroup.removeValue();
        reffMembers.child(group_id).removeValue();
        reffMyGroups.child(group_id).removeValue();
        reffRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        request = ds.getValue(Request.class);
                        if (request.getGroup_id().equals(group_id)){
                            reffRequests.child(ds.getKey()).removeValue();
                            Log.d("REQUEST KEY", ds.getKey());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteGroup:onCancelled", databaseError.toException());
            }
        });
    }

    private void deleteUserFromGroup(final String username) {
        //brise korisnika iz member
        reffMembers.child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String member;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    member = ds.getValue(String.class);
                    if (username.equals(member)) {
                        Log.d("MEMBER ZA BRISANJE", member);
                        reffMembers.child(group_id).child(ds.getKey()).removeValue();
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
        reffMyGroups.child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
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
                reffMyGroups.child(group_id).setValue(group);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void  showLeaveGroupAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupActivity.this);
        alertDialog.setTitle("Leave Group");
        alertDialog.setMessage("Are you sure you want to leave the group?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUserFromGroup(username);
                sendUserToMyGroupsActivity();
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.setCancelable(true);
            }
        });
        alertDialog.create().show();
    }

    private void setUpFABMenu() {
        incomeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, AddBillActivity.class);
                Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                bundle.putInt("expense_or_income", 0);
                bundle.putString("group_id", group_id);
                intent.putExtras(bundle);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });


        expenseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, AddBillActivity.class);
                Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                bundle.putInt("expense_or_income", 1);
                bundle.putString("group_id", group_id);
                intent.putExtras(bundle);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });

        groupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupActivity.this, AddGroupActivity.class);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });
    }

    private void showGroupInfo() {
        reffMyGroups.child(group_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) group = dataSnapshot.getValue(Group.class);
                nameTV.setText(group.getName());
                if (group.getDescription().equals("")) {
                    descriptionTV.setText("No description");
                } else {
                    descriptionTV.setText(group.getDescription());
                }
                membersTV.setText(group.getMembers());
                adminTV.setText(group.getAdmin());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "showGroupInfo:onCancelled", databaseError.toException());
            }
        });
    }

    private void showShortReviewData() {

        reffMyCategoriesGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> inc_exp = new ArrayList<>();
                ArrayList<Float> categoryAmount = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (userID.equals(category.getUserID()) && category.getExpense_or_income() == 0) {
                        total_income += category.getTotal_amount();
                    }
                    if (userID.equals(category.getUserID()) && category.getExpense_or_income() == 1) {
                        total_expense += category.getTotal_amount();
                    }
                    Log.d("inc, expe", String.valueOf(total_income) + "  " + String.valueOf(total_expense));
                }
                incomeTV.setText(String.valueOf(total_income));
                expenseTV.setText(String.valueOf("-"+total_expense));
                totalAmountTV.setText(String.valueOf(total_income - total_expense));

                categoryAmount.add(total_income);
                categoryAmount.add(total_expense);
                inc_exp.add("income");
                inc_exp.add("expense");

                Pie pie = AnyChart.pie();
                List<DataEntry> dataEntries = new ArrayList<>();

                for (int i = 0; i < categoryAmount.size(); i++) {
                    dataEntries.add(new ValueDataEntry(inc_exp.get(i), categoryAmount.get(i)));
                }


                if (categoryAmount.size() == 0) {
                    //dataEntries.add(new ValueDataEntry("No categories to show", 1));
                    pieChartLinLay.setVisibility(View.INVISIBLE);
                    pieChartLinLay.setVisibility(View.GONE);
                } else {
                    pieChartLinLay.setVisibility(View.VISIBLE);
                }

                /*ProgressBar pbar = findViewById(R.id.progressBar);
                shortReviewPie.setProgressBar(pbar);*/

                pie.data(dataEntries);
                //pie.title("Categories");
                //pie.labels().position("outside");
                //pie.fill("aquastyle");
                //pie.select().explode("5%");
                //pie.radius("120%");
                shortReviewPie.setChart(pie);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "showShortReviewData:onCancelled", databaseError.toException());
            }
        });
    }

    private void getUsername() {
        reffUsers.child(userID+"/name").addListenerForSingleValueEvent(new ValueEventListener() {
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
        reffMyCategoriesGroup = database.getReference().child("myCategoriesGroup/" + group_id);
        reffMyFinancesGroup = database.getReference().child("myFinancesGroup/" + group_id);
        reffRequests = database.getReference().child("requests");
    }

    private void activityLayoutItemsInitialization() {
        nameTV = (TextView) findViewById(R.id.showNameTV);
        descriptionTV = (TextView) findViewById(R.id.showDescriptionTV);
        membersTV = (TextView) findViewById(R.id.showMembersTV);
        adminTV = (TextView) findViewById(R.id.showAdminTV);
        incomeTV = (TextView) findViewById(R.id.incomeCatTV);
        expenseTV = (TextView) findViewById(R.id.expenseCatTV);
        totalAmountTV = (TextView) findViewById(R.id.totalAmountCatTV);
        addBillFAB = findViewById(R.id.addBillFAB);
        incomeFAB = findViewById(R.id.incomeFAB);
        expenseFAB = findViewById(R.id.expenseFAB);
        groupFAB = findViewById(R.id.groupFAB);
        showCategoriesBTN = (Button) findViewById(R.id.showCategoriesBTN);
        editGroupBTN = (Button) findViewById(R.id.editGroupBTN);
        deleteGroupBTN = (Button) findViewById(R.id.deleteGroupBTN);
        leaveGroupBTN = (Button) findViewById(R.id.leaveGroupBTN);
        shortReviewPie = (AnyChartView) findViewById(R.id.shortReviewPie);
        pieChartLinLay = (LinearLayout) findViewById(R.id.incExChartLinLay);
        pieChartLinLay.setVisibility(View.INVISIBLE);

        /*editGroupLayout = (LinearLayout) findViewById(R.id.editGroupLayout);
        editGroupBTN.setVisibility(View.GONE);*/
    }

    private String getIncomingIntent(String key){
        String value = "";
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        if (getIntent().hasExtra(key)) {
            value = getIntent().getStringExtra(key);
        }
        return value;
    }

    private void sendUserToMyGroupsActivity() {
        Intent intent = new Intent(GroupActivity.this, MyGroupsActivity.class);
        /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
        finish();
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
        sendUserToMyGroupsActivity();
    }
}
