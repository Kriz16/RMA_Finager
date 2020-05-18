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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private String userID;
    private TextView headerFullNameTV;
    private TextView headerEmailTV;


    private FloatingActionButton expenseFAB;
    private FloatingActionButton incomeFAB;
    private FloatingActionsMenu addBillFAB;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //final TextView probaTV = findViewById(R.id.probaTV);
        View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        headerFullNameTV = (TextView) navView.findViewById(R.id.headerFullNameTV);
        headerEmailTV = (TextView) navView.findViewById(R.id.headerEmailTV);
        userID = firebaseUser.getUid();
        setUserDataInNavigationDrawer();

        addBillFAB = findViewById(R.id.addBillFAB);

        incomeFAB = findViewById(R.id.incomeFAB);
        incomeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showToast("add income clicked");
                //openDialog(0, userID);
                Intent intent = new Intent(MainActivity.this, AddBillActivity.class);
                Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                bundle.putInt("expense_or_income", 0);
                intent.putExtras(bundle);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });

        expenseFAB = findViewById(R.id.expenseFAB);
        expenseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showToast("add expense clicked");
                //openDialog(1, userID);
                Intent intent = new Intent(MainActivity.this, AddBillActivity.class);
                Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                bundle.putInt("expense_or_income", 1);
                intent.putExtras(bundle);
                startActivity(intent);
                addBillFAB.collapse();
            }
        });



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    /*case R.id.request:
                        Intent requestActivity = new Intent(MainActivity.this, RequestAcyivity.class);
                        startActivity(requestActivity);
                        break;



                    case R.id.myGroups:
                        Intent myGroups = new Intent(MainActivity.this, GroupFinanceActivity.class);
                        startActivity(myGroups);
                        break;

                    case R.id.settings:
                        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;*/
                    case R.id.myFinance:
                        Intent myFinance = new Intent(MainActivity.this, MyCategoriesActivity.class);
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

    public void showToast(String message) {
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
                        headerFullNameTV.setText(user.getName());
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

    public void sendUserToLoginActivity(){
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
