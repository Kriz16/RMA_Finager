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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GroupCategoriesActivity extends AppCompatActivity {
    private String TAG = "GroupCategoriesActivity";
    private String group_id;
    private String userID;
    private RecyclerView recyclerView;
    private Bill bill;
    private ArrayList<Category> categoryList;
    private MyCategoriesRecyclerViewAdapter myCategoryAdapter;
    private Category category;
    private TextView incomeTV;
    private TextView expenseTV;
    private TextView totalAmountTV;
    private AnyChartView incomePie;
    private AnyChartView expensePie;
    private LinearLayout linearLayoutShowHide;
    private LinearLayout incChartLinLay;
    private LinearLayout expChartLinLay;
    private Switch showHideSwitch;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference reffMyCategoriesGroup;
    private DatabaseReference reffMyFinancesGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_categories);

        group_id = getIcomingIntent();
        Log.d(TAG, "GROUP_ID " + group_id);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseItemsInitialization();

        activityLayoutItemsInitialization();

        showShortReviewData();

        setUpPieChart();

        setUpRecyclerView();

        showHideSwitch();
    }

    private void setUpRecyclerView() {
        reffMyCategoriesGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                categoryList = new ArrayList<Category>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    categoryList.add(category);
                }
                myCategoryAdapter = new MyCategoriesRecyclerViewAdapter(GroupCategoriesActivity.this, categoryList);
                recyclerView.setAdapter(myCategoryAdapter);

                myCategoryAdapter.setOnItemClickListener(new MyCategoriesRecyclerViewAdapter.OnItemClickListenerCat() {
                    @Override
                    public void onItemClick(int position) {
                        showCategoryBills(position);
                    }

                    @Override
                    public void onEditClick(int position) {
                        sendUserToEditMyCategoryActivity(position);
                    }

                    @Override
                    public void onDeleteClick(int position) {
                        showDeleteCategoryAlertDialog(position);
                    }
                });

                if (categoryList.size() == 0) {
                    showNoCategoriesToShowAlertDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "showCategories:onCancelled", databaseError.toException());
            }
        });
    }

    private void showCategoryBills(int position) {
        Intent intent = new Intent(GroupCategoriesActivity.this , GroupCategoryBillsActivity.class);
        intent.putExtra("category", categoryList.get(position).getCategory());
        intent.putExtra("group_id", group_id);
        finish(); //ZADNJE DODANOOOOOOOOOOOOOO!!!!!!!!!!!11
        startActivity(intent);

        //Toast.makeText(MyCategoriesActivity.this, position+" is clicked", Toast.LENGTH_SHORT).show();
    }


    private void  showDeleteCategoryAlertDialog(final int position) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupCategoriesActivity.this);
        alertDialog.setTitle("Delete category");
        alertDialog.setMessage("Are you sure you want to delete this category?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categor_name = categoryList.get(position).getCategory();
                deleteCategoryBillsFromDatabase(categor_name);
                deleteCategoryFromDatabase(categor_name);
                sendUserToGroupCategoriesActivity();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.setCancelable(true);
                Toast.makeText(GroupCategoriesActivity.this, "Canceled...", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.create().show();
    }

    private void sendUserToGroupCategoriesActivity() {
        Intent intent = new Intent(GroupCategoriesActivity.this, GroupCategoriesActivity.class);
        intent.putExtra("group_id", group_id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);//DODANO
        startActivity(intent);
        finish();//DODANO
    }

    private void deleteCategoryBillsFromDatabase(final String category_name) {
        reffMyFinancesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    bill = ds.getValue(Bill.class);
                    if (bill.getCategory().equals(category_name)) {
                        //Log.d("IDE-EVI ZA BRISANJE B", id);
                        reffMyFinancesGroup.child(id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteCategoryBillsFromDatabase:onCancelled", databaseError.toException());
            }
        });
    }

    private void deleteCategoryFromDatabase(final String category_name) {
        reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    category = ds.getValue(Category.class);
                    if (category.getCategory().equals(category_name)) {
                        //Log.d("IDE-EVI ZA BRISANJE C", id);
                        reffMyCategoriesGroup.child(id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteCategoryFromDatabase:onCancelled", databaseError.toException());
            }
        });
    }

    private void sendUserToEditMyCategoryActivity(final int position) {
        Intent intent = new Intent(GroupCategoriesActivity.this, EditGroupCategoryActivity.class);
        if (categoryList.get(position).getExpense_or_income() == 0) {
            intent.putExtra("expense_or_income", "income");
        } else {
            intent.putExtra("expense_or_income", "expense");
        }
        intent.putExtra("category", categoryList.get(position).getCategory());
        intent.putExtra("group_id", group_id);
        startActivity(intent);
    }

    private void showNoCategoriesToShowAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupCategoriesActivity.this);
        alertDialog.setTitle("My categories");
        alertDialog.setMessage("There is no categories to show");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendUserToGroupActivity();
            }
        });
        alertDialog.create().show();
    }

    private void sendUserToGroupActivity() {
        Intent intent = new Intent(GroupCategoriesActivity.this, GroupActivity.class);
        /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
        intent.putExtra("group_id", group_id);
        finish();
        startActivity(intent);

    }

    private void showShortReviewData() {
        final float[] totalIncome = {0f};
        final float[] totalExpense = {0f};
        reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (category.getExpense_or_income() == 0) {
                        totalIncome[0] += category.getTotal_amount();
                    }
                    if (category.getExpense_or_income() == 1) {
                        totalExpense[0] += category.getTotal_amount();
                    }
                    Log.d("inc, expe", String.valueOf(totalIncome[0]) + "  " + String.valueOf(totalExpense[0]));
                }
                incomeTV.setText(String.valueOf(totalIncome[0]));
                expenseTV.setText(String.valueOf("-"+totalExpense[0]));
                totalAmountTV.setText(String.valueOf(totalIncome[0] - totalExpense[0]));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "showShortReviewData:onCancelled", databaseError.toException());
            }
        });
    }

    private void setUpPieChart() {
        reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> catIncome = new ArrayList<>();
                ArrayList<Float> catIncomeAmount = new ArrayList<>();
                ArrayList<String> catExpense = new ArrayList<>();
                ArrayList<Float> catExpenseAmount = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (category.getExpense_or_income() == 0) {
                        catIncome.add(category.getCategory());
                        catIncomeAmount.add(category.getTotal_amount());
                    }
                    if (category.getExpense_or_income() == 1) {
                        catExpense.add(category.getCategory());
                        catExpenseAmount.add(category.getTotal_amount());
                    }

                }

                //postavljanje prvog pie charta
                incomePie = findViewById(R.id.incomePie);
                APIlib.getInstance().setActiveAnyChartView(incomePie);

                Pie pie = AnyChart.pie();

                List<DataEntry> data = new ArrayList<>();
                for (int i = 0; i < catIncome.size(); i++) {
                    data.add(new ValueDataEntry(catIncome.get(i), catIncomeAmount.get(i)));
                }

                if (data.isEmpty()) {
                    incChartLinLay.setVisibility(View.GONE);
                } else {
                    incChartLinLay.setVisibility(View.VISIBLE);
                }

                pie.data(data);
                incomePie.setChart(pie);

                //postavljanje drugog pie charta
                expensePie = findViewById(R.id.expensePie);
                APIlib.getInstance().setActiveAnyChartView(expensePie);

                Pie pie1 = AnyChart.pie();

                List<DataEntry> data1 = new ArrayList<>();
                for (int i = 0; i < catExpense.size(); i++) {
                    data1.add(new ValueDataEntry(catExpense.get(i), catExpenseAmount.get(i)));
                }

                if (data1.isEmpty()) {
                    expChartLinLay.setVisibility(View.INVISIBLE);
                    expChartLinLay.setVisibility(View.GONE);
                } else {
                    expChartLinLay.setVisibility(View.VISIBLE);
                }

                pie1.data(data1);
                expensePie.setChart(pie1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setUpPieChart:onCancelled", databaseError.toException());
            }
        });


    }

    private void  showHideSwitch(){
        showHideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    linearLayoutShowHide.setVisibility(View.VISIBLE);
                } else {
                    linearLayoutShowHide.setVisibility(View.GONE);
                }
            }
        });
    }

    private void activityLayoutItemsInitialization() {
        incomeTV = (TextView) findViewById(R.id.incomeCatTV);
        expenseTV = (TextView) findViewById(R.id.expenseCatTV);
        totalAmountTV = (TextView) findViewById(R.id.totalAmountCatTV);
        recyclerView = findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        showHideSwitch = (Switch) findViewById(R.id.switchButton);
        linearLayoutShowHide = (LinearLayout) findViewById(R.id.showHideLayout);
        linearLayoutShowHide.setVisibility(View.GONE);
        incChartLinLay = (LinearLayout) findViewById(R.id.incChartLinLay);
        incChartLinLay.setVisibility(View.GONE);
        expChartLinLay = (LinearLayout) findViewById(R.id.expChartLinLay);
        expChartLinLay.setVisibility(View.INVISIBLE);
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        reffMyCategoriesGroup = database.getReference().child("myCategoriesGroup/" + group_id);
        reffMyFinancesGroup = database.getReference().child("myFinancesGroup/" + group_id);
    }

    private String getIcomingIntent(){
        String id_grupe = "";
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        if (getIntent().hasExtra("group_id")) {
            id_grupe = getIntent().getStringExtra("group_id");
        }
        return id_grupe;
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
}
