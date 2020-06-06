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

public class GroupCategoryBillsActivity extends AppCompatActivity {
    private static final String TAG = "GroupCategoryBillsAct";
    private static String category_name;
    private static String group_id;
    private static String userID;
    private static Bill bill;
    private static Category cat;
    private static RecyclerView recyclerView;
    private static ArrayList<Bill> billList;
    private static ArrayList<String> billLID;
    private MyCategoryBillsRecyclerViewAdapter myCategoryBillsAdapter;
    private float total_amount = 0;
    private String dateToStore;
    private String categoryID;
    private boolean flag;
    private TextView chartTitle;
    private Switch showHideSwitch;
    private LinearLayout linearLayoutShowHide;
    private AnyChartView subcategoryPie;
    private TextView totalAmountTV;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyFinancesGroup;
    private DatabaseReference reffMyCategoriesGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_category_bills);

        group_id = getIncomingIntent("group_id");
        category_name = getIncomingIntent("category");
        Log.d("CATEGORY_NAME", category_name);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activityLayoutItemsInitialization();

        firebaseItemsInitialization();

        showHideSwitch();

        setUpPieChart();

        getCategoryBills();

    }

    private void getCategoryBills() {
        bill = new Bill();

        reffMyFinancesGroup.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                billList = new ArrayList<Bill>();
                billLID = new ArrayList<String>();
                int inc_or_exp = 2;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (category_name.equals(bill.getCategory())) {
                        billList.add(bill);
                        billLID.add(ds.getKey());
                        inc_or_exp = bill.getExpense_or_income();
                    }
                }
                myCategoryBillsAdapter = new MyCategoryBillsRecyclerViewAdapter(GroupCategoryBillsActivity.this, billList, inc_or_exp);
                recyclerView.setAdapter(myCategoryBillsAdapter);

                //implementiranje metoda iz interfacea koji se nalazi u adapteru
                myCategoryBillsAdapter.setOnItemClickListener(new MyCategoryBillsRecyclerViewAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                    }

                    @Override
                    public void onEditClick(int position) {
                        sendUserToEditGroupBillActivity(position);
                    }

                    @Override
                    public void onDeleteClick(final int position) {
                        setDeleteAlertDialog(position);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getCategoryBills:onCancelled", databaseError.toException());
            }
        });
    }

    private void setDeleteAlertDialog(final int position) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(GroupCategoryBillsActivity.this);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Are you sure you want to delete this bill?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float amount = billList.get(position).getAmount();
                deleteBillFromDatabase(position);
                flag = checkIfThereAreOtherBills();
                Log.d("TAG", String.valueOf(flag));
                if (flag) {
                    updateCategory(position, amount);
                    reloadActivity();
                } else {
                    deleteCategory();
                    sendUserToGroupCategoriesActivity();
                }

            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.setCancelable(true);
                Toast.makeText(GroupCategoryBillsActivity.this, "Canceled...", Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.create().show();
    }

    private void  sendUserToEditGroupBillActivity(int position) {
        Intent intent = new Intent(GroupCategoryBillsActivity.this, EditGroupBillActivity.class);
        intent.putExtra("billID", billLID.get(position));
        if (billList.get(position).getExpense_or_income() == 0) {
            intent.putExtra("expense_or_income", "income");
        } else {
            intent.putExtra("expense_or_income", "expense");
        }
        intent.putExtra("categoryOld", billList.get(position).getCategory());
        intent.putExtra("amountOld", String.valueOf(billList.get(position).getAmount()));
        intent.putExtra("group_id", group_id);
        /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //DODANO
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); //DODANO*/
        finish(); //DODANO
        startActivity(intent);

    }

    private void reloadActivity() {
        Intent intent = new Intent(GroupCategoryBillsActivity.this, GroupCategoryBillsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("group_id", group_id);
        intent.putExtra("category", category_name);
        finish();
        startActivity(intent);

    }

    private void sendUserToGroupCategoriesActivity(){
        Intent intent = new Intent(GroupCategoryBillsActivity.this, GroupCategoriesActivity.class);
        /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
        intent.putExtra("group_id", group_id);
        finish();
        startActivity(intent);

    }

    private void deleteCategory() {
        reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    cat = ds.getValue(Category.class);
                    if (category_name.equals(cat.getCategory())) {
                        reffMyCategoriesGroup.child(ds.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "deleteCategory:onCancelled", databaseError.toException());
            }
        });

    }

    private void updateCategory(final int position, final float deleteAmount) {
        reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int exp_or_inc = 2;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    cat = ds.getValue(Category.class);
                    if (category_name.equals(cat.getCategory())) {
                        total_amount = cat.getTotal_amount();
                        total_amount = total_amount - deleteAmount;
                        categoryID = ds.getKey();
                        exp_or_inc = cat.getExpense_or_income();
                        //Log.d("TOTAL AMOUNT", String.valueOf(total_amount) + "  " + dateToStore + "  " + categoryID);
                    }
                }
                //Log.d("TAG", category_name + " " + dateToStore + " " + total_amount);
                //cat = new Category(userID, category_name, dateToStore, cat.getExpense_or_income(), total_amount);
                cat = new Category(userID, category_name, exp_or_inc, total_amount);
                reffMyCategoriesGroup.child(categoryID).setValue(cat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "updateCategory:onCancelled", databaseError.toException());
            }
        });

    }

    private boolean checkIfThereAreOtherBills(){
        Log.d("BILLLIST SIZE", String.valueOf(billList.size()));
        if (billList.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private void deleteBillFromDatabase(final int position) {
        //Toast.makeText(MyCategoryBillsActivity.this, String.valueOf(billList.get(position).getAmount()) + "  " + String.valueOf(position) + " " + billLID.get(position), Toast.LENGTH_LONG).show();
        String IDRacunaZaBrisanje = billLID.get(position);
        reffMyFinancesGroup.child(IDRacunaZaBrisanje).removeValue();
        Toast.makeText(GroupCategoryBillsActivity.this, "Successfully deleted...", Toast.LENGTH_SHORT).show();
        billList.remove(position);
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
        chartTitle.setText(category_name );

        reffMyFinancesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> subcatName = new ArrayList<>();
                ArrayList<Float> subcatAmount = new ArrayList<>();
                float totalAmount = 0.f;
                int exp_or_inc = 2;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (category_name.equals(bill.getCategory())) {
                        subcatName.add(bill.getSubcategory());
                        totalAmount += bill.getAmount();
                        exp_or_inc = bill.getExpense_or_income();
                    }
                }

                if (exp_or_inc == 1) {
                    totalAmountTV.setText("-"+String.valueOf(totalAmount));
                } else {
                    totalAmountTV.setText(String.valueOf(totalAmount));
                }

                subcatName = removeDuplicateMembers(subcatName);
                for (int i = 0; i < subcatName.size(); i++) {
                    float amount = 0.f;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        bill = ds.getValue(Bill.class);
                        if (category_name.equals(bill.getCategory()) && subcatName.get(i).equals(bill.getSubcategory())) {
                            amount += bill.getAmount();
                        }
                    }
                    subcatAmount.add(amount);
                }

                subcategoryPie = findViewById(R.id.subcategoryPieChart);
                APIlib.getInstance().setActiveAnyChartView(subcategoryPie);

                Pie pie = AnyChart.pie();

                List<DataEntry> data = new ArrayList<>();
                for (int i = 0; i < subcatName.size(); i++) {
                    data.add(new ValueDataEntry(subcatName.get(i), subcatAmount.get(i)));
                }

                pie.data(data);
                subcategoryPie.setChart(pie);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setUpPieChart:onCancelled", databaseError.toException());
            }
        });
    }

    private ArrayList<String> removeDuplicateMembers(ArrayList<String> members) {
        ArrayList<String> listMembers = new ArrayList<String>();

        for(int i=0; i < members.size(); i++){

            if( !listMembers.contains(members.get(i)) ){
                listMembers.add(members.get(i));
            }
        }

        return listMembers;
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        reffMyFinancesGroup = database.getReference().child("myFinancesGroup/" + group_id);
        reffMyCategoriesGroup = database.getReference().child("myCategoriesGroup/" + group_id);
    }

    private void activityLayoutItemsInitialization() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        chartTitle = (TextView) findViewById(R.id.chartTitleTV);
        showHideSwitch = (Switch) findViewById(R.id.switchButton);
        linearLayoutShowHide = (LinearLayout) findViewById(R.id.showHideLayout);
        linearLayoutShowHide.setVisibility(View.GONE);
        totalAmountTV = (TextView) findViewById(R.id.totalAmountCatTV);
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
        sendUserToGroupCategoriesActivity();
    }
}
