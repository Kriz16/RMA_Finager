package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private DrawerLayout drawer;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private String userID;
    private TextView headerUsernameTV;
    private TextView headerEmailTV;
    private Category category;
    private float total_income;
    private float total_expense;

    private TextView incomeTV;
    private TextView expenseTV;
    private TextView totalAmountTV;
    private LinearLayout pieChartLinLay;

    private FloatingActionButton groupFAB;
    private FloatingActionButton expenseFAB;
    private FloatingActionButton incomeFAB;
    private FloatingActionsMenu addBillFAB;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference reffMyCategories;
    private FirebaseDatabase database;
    private DatabaseReference reffMyGroups;

    private AnyChartView anyChartView;
    private AnyChartView shortReviewPie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseItemsInitialization();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer);

        navigationView = findViewById(R.id.navigationView);
        /*Menu menu = navigationView.getMenu();
        final Menu submenu = menu.addSubMenu("My groups");
        reffMyGroups = database.getReference().child("myGroups");
        reffMyGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group grupa;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    grupa = ds.getValue(Group.class);
                    submenu.add(grupa.getName());
                    navigationView.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ", databaseError.toException());
            }
        });*/

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        activityLayoutItemsInitialization();

        shortReviewPie = (AnyChartView) findViewById(R.id.shortReviewPie);

        showShortReviewData();

        //setUpPieChart();

        setUserDataInNavigationDrawer();

        setUpFABMenu();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.request:
                        Intent requestActivity = new Intent(MainActivity.this, MyRequestsActivity.class);
                        startActivity(requestActivity);
                        break;

                    case R.id.myGroups:
                        Intent myGroups = new Intent(MainActivity.this, MyGroupsActivity.class);
                        startActivity(myGroups);
                        break;

                    case R.id.myCategories:
                        Intent myCategories = new Intent(MainActivity.this, MyCategoriesActivity.class);
                        startActivity(myCategories);
                        break;

                    case R.id.myFinance:
                        Intent myFinance = new Intent(MainActivity.this, MainActivity.class);
                        finish();
                        startActivity(myFinance);
                        break;

                    case R.id.logout:
                        firebaseAuth.getInstance().signOut();
                        sendUserToLoginActivity();
                        break;
                }
                return true;
            }
        });


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        /*GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();*/
    }

    /*public void openDialog(int expense_or_income, String UserID) {
        AddBillDialog addBillDialog = new AddBillDialog(expense_or_income, UserID);
        addBillDialog.show(getSupportFragmentManager(), "add bill dialog");
    }*/

    private void setUpPieChart() {
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> categoryList = new ArrayList<>();
                ArrayList<Float> categoryAmount = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    category = ds.getValue(Category.class);
                    if (userID.equals(category.getUserID())) {
                        categoryList.add(category.getCategory());
                        categoryAmount.add(category.getTotal_amount());
                    }

                }
                Pie pie = AnyChart.pie();
                List<DataEntry> dataEntries = new ArrayList<>();

                for (int i = 0; i < categoryList.size(); i++) {
                    dataEntries.add(new ValueDataEntry(categoryList.get(i), categoryAmount.get(i)));
                }

                if (dataEntries.isEmpty()) {
                    dataEntries.add(new ValueDataEntry("No categories to show", 1));
                    pieChartLinLay.setVisibility(View.INVISIBLE);
                    pieChartLinLay.setVisibility(View.GONE);
                } else {
                    pieChartLinLay.setVisibility(View.VISIBLE);
                }

                pie.data(dataEntries);
                //pie.title("Categories");
                //pie.labels().position("outside");
                //pie.fill("aquastyle");
                //pie.select().explode("5%");
                //pie.radius("30%");
                anyChartView.setChart(pie);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setUpPieChart:onCancelled", databaseError.toException());
            }
        });


    }

    private void showShortReviewData() {

        reffMyCategories.addValueEventListener(new ValueEventListener() {
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

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void setUserDataInNavigationDrawer(){
        DatabaseReference reference = firebaseDatabase.getReference().child("users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (userID.equals(snapshot.getKey())) {
                        User user = snapshot.getValue(User.class);
                        headerUsernameTV.setText(user.getName());
                        headerEmailTV.setText(user.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Tag", databaseError.getMessage());
            }
        });
    }

    private void activityLayoutItemsInitialization() {
        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        headerUsernameTV = (TextView) navView.findViewById(R.id.headerUsernameTV);
        headerEmailTV = (TextView) navView.findViewById(R.id.headerEmailTV);
        incomeTV = (TextView) findViewById(R.id.incomeCatTV);
        expenseTV = (TextView) findViewById(R.id.expenseCatTV);
        totalAmountTV = (TextView) findViewById(R.id.totalAmountCatTV);
        addBillFAB = findViewById(R.id.addBillFAB);
        incomeFAB = findViewById(R.id.incomeFAB);
        expenseFAB = findViewById(R.id.expenseFAB);
        groupFAB = findViewById(R.id.groupFAB);
        //anyChartView = (AnyChartView) findViewById(R.id.pieChart);
        pieChartLinLay = (LinearLayout) findViewById(R.id.incExChartLinLay);
        pieChartLinLay.setVisibility(View.INVISIBLE);
    }

    private void  firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userID = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();
        reffMyCategories = database.getReference().child("myCategories");
    }

    private void setUpFABMenu() {
        incomeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showToast("add income clicked");
                //openDialog(0, userID);
                Intent intent = new Intent(MainActivity.this, AddBillActivity.class);
                Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                bundle.putInt("expense_or_income", 0);
                bundle.putString("group_id", "nije_grupa");
                intent.putExtras(bundle);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });


        expenseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showToast("add expense clicked");
                //openDialog(1, userID);
                Intent intent = new Intent(MainActivity.this, AddBillActivity.class);
                Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                bundle.putInt("expense_or_income", 1);
                bundle.putString("group_id", "nije_grupa");
                intent.putExtras(bundle);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });

        groupFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddGroupActivity.class);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });
    }

    private void sendUserToLoginActivity(){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        addBillFAB.collapse();
    }

    @Override
    protected void onStart() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        super.onStart();
        addBillFAB.collapse();
    }

    @Override //ako korisnik pritisne u Constraint layout miče se tipkovnica iz fokusa
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(MainActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
