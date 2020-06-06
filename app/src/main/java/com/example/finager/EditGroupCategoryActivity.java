package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditGroupCategoryActivity extends AppCompatActivity {
    private String TAG = "EditMyCategoryActivity";
    private int expense_or_income;
    private String category;
    private String userID;
    private Button cancelBTN;
    private Button saveBTN;
    private AutoCompleteTextView categoryET;
    private Bill bill;
    private Category cat;
    private long maxcategoryID = 0;
    private boolean catPostojiUBazi = false;
    private String categoryIDOld;
    private String categoryID;
    private String group_id;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyFinancesGroup;
    private DatabaseReference reffMyCategoriesGroup;
    private DatabaseReference reffMyFinancesCategoryGroupID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_edit_group_category);
        /* PROVJERIT DAL SVE FUNKCIONIRA AKO KORISTIM LAYOUT DRUGE AKTIVNOSTI POŠTO U TOM LAYOUTU PISE KONTEKSTOM DRUGE AKTIVNOSTI*/

        group_id = getIncomingIntent("group_id");
        category = getIncomingIntent("category");
        String getIncIntent = getIncomingIntent("expense_or_income");
        if (getIncIntent.equals("income")) {
            expense_or_income = 0;
        } else {
            expense_or_income = 1;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setCorrectContentView();

        firebaseItemsInitialization();

        activityLayoutItemInitialization();

        categoryET.setText(category);

        setButtons();
    }

    private void setButtons() {
        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String categoryNew = categoryET.getText().toString().trim();
                if ("".equals(categoryNew)) {
                    categoryET.setError("Category is required!");
                } else {
                    reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            boolean wrongCategory = false;
                            final String categoryNew = categoryET.getText().toString();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                cat = ds.getValue(Category.class);
                                if (categoryNew.equals(cat.getCategory())) {
                                    catPostojiUBazi = true;
                                    categoryID = ds.getKey();
                                }
                                if (category.equals(cat.getCategory())) {
                                    categoryIDOld = ds.getKey();
                                }
                                if (categoryNew.equals(cat.getCategory()) && expense_or_income != cat.getExpense_or_income()) {
                                    if (expense_or_income == 0) {
                                        categoryET.setError("Chosen category is't income category");
                                    } else {
                                        categoryET.setError("Chosen category is't expense category");
                                    }
                                    wrongCategory = true;
                                }
                            }

                            if (!wrongCategory) {
                                //tu updateaj racun jer uvjek cu mjenjat samo naziv kategorije racunu
                                if (categoryNew.equals(category)) {
                                    sendUserToGroupCategoriesActivity();
                                } else {
                                    updateBill();

                                    if (catPostojiUBazi) {
                                        //kategoirija postoji u bazi
                                        reffMyCategoriesGroup.child(categoryIDOld).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                cat = dataSnapshot.getValue(Category.class);
                                                float total_amount_old = 0;
                                                if (dataSnapshot.exists()) {
                                                    total_amount_old = cat.getTotal_amount();
                                                }

                                                final float finalTotal_amount_old = total_amount_old;
                                                reffMyCategoriesGroup.child(categoryID).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        //dohvacam total_amount nove kategorije, uvecavam ga za amount stare kategorije, spreman u bazu i brišem staru kategoriju
                                                        cat = dataSnapshot.getValue(Category.class);
                                                        float new_total_amount = 0;
                                                        new_total_amount = cat.getTotal_amount() + finalTotal_amount_old;
                                                        cat.setTotal_amount(new_total_amount);
                                                        reffMyCategoriesGroup.child(categoryID).setValue(cat);
                                                        reffMyCategoriesGroup.child(categoryIDOld).removeValue();
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                            }
                                        });
                                    } else {
                                        //kategorija ne postoji - samo izmjeni ime kategorije u myCategories
                                        reffMyCategoriesGroup.child(categoryIDOld).child("category").setValue(categoryNew);
                                    }
                                }

                                sendUserToGroupCategoriesActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w(TAG, "setButtons:onCancelled", databaseError.toException());
                        }
                    });
                }

            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToGroupCategoriesActivity();
            }
        });
    }


    private void updateBill() {
        reffMyFinancesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String new_category = categoryET.getText().toString();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (category.equals(bill.getCategory())) {
                        bill.setCategory(new_category);
                        reffMyFinancesGroup.child(ds.getKey()).setValue(bill);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "updateBill:onCancelled", databaseError.toException());
            }
        });
    }

    private void sendUserToGroupCategoriesActivity() {
        Intent intent = new Intent(EditGroupCategoryActivity.this, GroupCategoriesActivity.class);
        intent.putExtra("group_id", group_id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();
        startActivity(intent);

    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();
        reffMyCategoriesGroup = database.getReference().child("myCategoriesGroup/" + group_id);
        reffMyFinancesGroup = database.getReference().child("myFinancesGroup/" + group_id);
        reffMyFinancesCategoryGroupID = database.getReference().child("myFinancesBillID/id_category_group");
    }

    private void activityLayoutItemInitialization() {
        cancelBTN = findViewById(R.id.cancelEditCategoryBTN);
        saveBTN = findViewById(R.id.saveEditCategoryBTN);
        categoryET = findViewById(R.id.categoryEditACTV);
        reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> katLista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    cat = ds.getValue(Category.class);
                    if (expense_or_income == cat.getExpense_or_income()) {
                        katLista.add(cat.getCategory());
                    }
                }
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(EditGroupCategoryActivity.this, android.R.layout.simple_expandable_list_item_1, katLista);
                categoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "activityLayoutItemInitialization:onCancelled", databaseError.toException());
            }
        });
    }

    private void setCorrectContentView() {
        if (expense_or_income == 0)
            setContentView(R.layout.activity_edit_my_category_income);
        else
            setContentView(R.layout.activity_edit_my_category_expense);
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

    @Override //ako korisnik pritisne u Constraint layout miče se tipkovnica iz fokusa
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(EditMyBillActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}
