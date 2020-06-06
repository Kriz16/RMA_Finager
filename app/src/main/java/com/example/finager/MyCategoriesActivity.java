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


public class MyCategoriesActivity extends AppCompatActivity {
    private String TAG = "MyCategoriesActivity";
    private String userID;
    private TextView incomeTV;
    private TextView expenseTV;
    private TextView totalAmountTV;
    private RecyclerView recyclerView;
    private Category category;
    private Bill bill;
    private ArrayList<Category> categoryList;
    private MyCategoriesRecyclerViewAdapter myCategoryAdapter;
    private AnyChartView incomePie;
    private AnyChartView expensePie;
    private LinearLayout linearLayoutShowHide;
    private LinearLayout incChartLinLay;
    private LinearLayout expChartLinLay;
    private Switch showHideSwitch;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyCategories;
    private DatabaseReference reffMyFinances;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_categories);

        activityLayoutItemsInitialization();

        firebaseItemsInitialization();

        showShortReviewData();

        setUpPieChart();

        setUpRecyclerView();

        showHideSwitch();

    }

    private void setUpRecyclerView() {
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

    private void setUpPieChart() {
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> catIncome = new ArrayList<>();
                ArrayList<Float> catIncomeAmount = new ArrayList<>();
                ArrayList<String> catExpense = new ArrayList<>();
                ArrayList<Float> catExpenseAmount = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (userID.equals(category.getUserID()) && category.getExpense_or_income() == 0) {
                        catIncome.add(category.getCategory());
                        catIncomeAmount.add(category.getTotal_amount());
                    }
                    if (userID.equals(category.getUserID()) && category.getExpense_or_income() == 1) {
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
                //pie.title("Categories");
                //pie.labels().position("outside");
                //pie.fill("aquastyle");
                //pie.select().explode("5%");
                //pie.radius("30%");
                expensePie.setChart(pie1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setUpPieChart:onCancelled", databaseError.toException());
            }
        });


    }

    private void showShortReviewData() {
        final float[] totalIncome = {0f};
        final float[] totalExpense = {0f};
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (userID.equals(category.getUserID()) && category.getExpense_or_income() == 0) {
                        totalIncome[0] += category.getTotal_amount();
                    }
                    if (userID.equals(category.getUserID()) && category.getExpense_or_income() == 1) {
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

    private void sendUserToEditMyCategoryActivity(final int position) {
        Intent intent = new Intent(MyCategoriesActivity.this, EditMyCategoryActivity.class);
        if (categoryList.get(position).getExpense_or_income() == 0) {
            intent.putExtra("expense_or_income", "income");
        } else {
            intent.putExtra("expense_or_income", "expense");
        }
        intent.putExtra("category", categoryList.get(position).getCategory());
        startActivity(intent);
    }

    private void  showDeleteCategoryAlertDialog(final int position) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyCategoriesActivity.this);
        alertDialog.setTitle("Delete category");
        alertDialog.setMessage("Are you sure you want to delete this category?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categor_name = categoryList.get(position).getCategory();
                deleteCategoryBillsFromDatabase(categor_name);
                deleteCategoryFromDatabase(categor_name);
                sendUserToMyCategoriesActivity();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.setCancelable(true);
                Toast.makeText(MyCategoriesActivity.this, "Canceled...", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.create().show();
    }

    private void sendUserToMyCategoriesActivity() {
        Intent intent = new Intent(MyCategoriesActivity.this, MyCategoriesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void deleteCategoryFromDatabase(final String category_name) {
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    category = ds.getValue(Category.class);
                    if (userID.equals(category.getUserID()) && category.getCategory().equals(category_name)) {
                        //Log.d("IDE-EVI ZA BRISANJE C", id);
                        reffMyCategories.child(id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteCategoryFromDatabase:onCancelled", databaseError.toException());
            }
        });
    }

    private void deleteCategoryBillsFromDatabase(final String category_name) {
        reffMyFinances.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    bill = ds.getValue(Bill.class);
                    if (userID.equals(bill.getUserID()) && bill.getCategory().equals(category_name)) {
                        //Log.d("IDE-EVI ZA BRISANJE B", id);
                        reffMyFinances.child(id).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteCategoryBillsFromDatabase:onCancelled", databaseError.toException());
            }
        });
    }

    private void showCategoryBills(int position) {
        Intent intent = new Intent(MyCategoriesActivity.this , MyCategoryBillsActivity.class);
        intent.putExtra("category", categoryList.get(position).getCategory());
        startActivity(intent);
        //Toast.makeText(MyCategoriesActivity.this, position+" is clicked", Toast.LENGTH_SHORT).show();
    }

    private void showNoCategoriesToShowAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MyCategoriesActivity.this);
        alertDialog.setTitle("My categories");
        alertDialog.setMessage("There is no categories to show");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendUserToMainActivity();
            }
        });
        alertDialog.create().show();
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseAuth.getUid();
        database = FirebaseDatabase.getInstance();
        reffMyCategories = database.getReference().child("myCategories");
        reffMyFinances = database.getReference().child("myFinances");
    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(MyCategoriesActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void activityLayoutItemsInitialization() {
        incomeTV = (TextView) findViewById(R.id.incomeCatTV);
        expenseTV = (TextView) findViewById(R.id.expenseCatTV);
        totalAmountTV = (TextView) findViewById(R.id.totalAmountCatTV);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        showHideSwitch = (Switch) findViewById(R.id.switchButton);
        linearLayoutShowHide = (LinearLayout) findViewById(R.id.showHideLayout);
        linearLayoutShowHide.setVisibility(View.GONE);
        incChartLinLay = (LinearLayout) findViewById(R.id.incChartLinLay);
        incChartLinLay.setVisibility(View.GONE);
        expChartLinLay = (LinearLayout) findViewById(R.id.expChartLinLay);
        expChartLinLay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMainActivity();
    }
}
