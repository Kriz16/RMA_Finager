package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class EditMyBillActivity extends AppCompatActivity {
    private String TAG = "EditMyBillActivity";
    private String getIncIntent;
    private String userID;
    private int expense_or_income;
    private String billID;
    private EditText amountET;
    private AutoCompleteTextView categoryET;
    private AutoCompleteTextView subcategoryET;
    private EditText dateET;
    private Button cancelBTN;
    private Button saveBTN;
    private Bill bill;
    private Category cat;
    private Calendar dateFromDatabase;
    private String categoryOld;
    private float amountOld;
    private long maxcategoryID = 0;

    private boolean flag2;
    private String categoryID;
    private String categoryIDOld;

    private FirebaseDatabase database;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference reffMyFinances;
    private DatabaseReference reffMyCategories;
    private DatabaseReference reffMyFinancesCategoryID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //dohvacanje podataka poslanih putem intenta
        billID = getIncomingIntent("billID");
        getIncIntent = getIncomingIntent("expense_or_income");
        if (getIncIntent.equals("income")) {
            expense_or_income = 0;
        } else {
            expense_or_income = 1;
        }
        categoryOld = getIncomingIntent("categoryOld");
        getIncIntent = getIncomingIntent("amountOld");
        amountOld = Float.parseFloat(getIncIntent);
        Log.d(TAG, "CATEGORY_OLD_ID" + categoryOld);
        if (expense_or_income == 0) {
            setContentView(R.layout.activity_edit_my_bill_income);
        } else {
            setContentView(R.layout.activity_edit_my_bill_expense);
        }

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseItemsInitialization();

        activityLayoutItemInitialization();

        reffMyFinancesCategoryID =  database.getReference().child("myFinancesBillID/id_category");

        getDataFromDatabaseToEditText();

        setDatePickerDialog();

        setButtons();

    }

    private void  setButtons() {
        saveBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*1. treba provjerit dal je koje od polja ostalo prazno ako je ispisat upozorenje X
                  2. provjerit dali postoji kategorija ako ne postoji onda stvorit novu kategoriju i uzet njen id za povratak nazad
                    - ako stvaram novu kategoriju onda treba stvorit novu kategoriju i updateat vrijednosti racuna
                    - ako stvaram novu kategoriju treba prijasnju kategoriju di mi je bija racun smanjit za tu vrijednost
                    - ako stvaramo novu kategoriju a u staroj kategoriji nema vise racuna, treba obrisat staru kategoriju
                    - ako stvaramo novu kategoriju a u staroj kategoriji ima jos racuna, treba umanjit total amount kategorije za amount racuna
                  3. ako kategorija postoji treba updateat tu kategoriju odnosno povecat amount
                  spremit stari naziv kategorije prije izmjene i novi naziv, onda njih usporedit prilikom spremanja racuna pa ako su jednaki razmislit na ca sve utjece*/
                boolean flag = true;
                String amount = amountET.getText().toString().trim();
                String category = categoryET.getText().toString().trim();
                String subcategory = subcategoryET.getText().toString().trim();
                String date = dateET.getText().toString().trim();
                float amountFloat = 0;

                if ("".equals(amount)){
                    amountET.setError("Amount is required!");
                    flag = false;
                } else {
                    amountFloat = Float.parseFloat(amount);
                }
                if ("".equals(category)) {
                    categoryET.setError("Category is required!");
                    flag = false;

                }
                if ("".equals(subcategory)) {
                    subcategoryET.setError("Subcategory is required!");
                    flag = false;
                }

                if (flag) {
                    makeAllChangesInDatabase(amountFloat, category, subcategory, date);
                    Log.d(TAG, amount + " " + category + " " + subcategory + " " + date);
                }
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMyCategoryBillsActivity();
            }
        });
    }

    private void makeAllChangesInDatabase(final float amount, final String category, final String subcategory, final String date) {
        flag2 = false;
        reffMyCategories = database.getReference().child("myCategories");
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean wrongCategory = false;
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    cat = ds.getValue(Category.class);
                    flag2 = checkIfCategoryExist(category);
                    if (userID.equals(cat.getUserID()) && category.equals(cat.getCategory())) {
                        categoryID = ds.getKey();
                    }
                    if (userID.equals(cat.getUserID()) && categoryOld.equals(cat.getCategory())) {
                        categoryIDOld = ds.getKey();
                    }
                    if (userID.equals(cat.getUserID()) && category.equals(cat.getCategory()) && expense_or_income != cat.getExpense_or_income()) {
                        if (expense_or_income == 0) {
                            categoryET.setError("Chosen category is't income category");
                        } else {
                            categoryET.setError("Chosen category is't expense category");
                        }
                        wrongCategory = true;
                    }
                }

                if (!wrongCategory) {
                    updateBill(amount, category, subcategory, date);
                    if (flag2) {
                        //kategorija postoji -> updateaj racun i kategoriju
                        flag2 = checkIfNewCategorySameAsOld(category, categoryOld );
                        if (flag2) { //ako je kategorija ostala ista pa updateam samo amount categorije ako je promijenjen amount racuna
                            updateCategory(category);
                            sendUserToMyCategoryBillsActivity();
                        } else { //ako je kategorija promijenjena a vec postoji u bazi
                            //smanji total_amount stare kategorije za staru vrijednost računa i povecaj vrijednost nove kat za novi amount
                            updateOldCategory();
                            increaseCategoryAmountWhereBillIsAdded(categoryID, amount);
                            sendUserToMyCategoryBillsActivityIfCategoryChanged();
                        }
                    } else {
                        //kategorija ne postoji
                        //treba updateat vrijednost stare kategorije, ako je iznos = 0 onda je treba obrisat
                        //treba stvorit novu kategoriju

                        //makeNewCategory(amount, category, expense_or_income);
                        //updateOldCategory();

                        updateOldCategory2(amount, category, expense_or_income);

                        sendUserToMyCategoryBillsActivityIfCategoryChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "makeAllChangesInDatabase:onCancelled", databaseError.toException());
            }
        });
    }

    private void updateOldCategory2(final float amount, final String category, final int expense_or_income) {
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    cat = ds.getValue(Category.class);
                    float category_amount;
                    Log.d("cat.getCategory()", cat.getCategory());
                    if (categoryOld.equals(cat.getCategory())) {

                        category_amount = cat.getTotal_amount() - amountOld;
                        Log.d("amountOld", String.valueOf(cat.getTotal_amount()) + "  " + String.valueOf(amountOld));
                        Log.d("category_amount = 0", String.valueOf(category_amount));
                        if (category_amount == 0 || category_amount == 0.0) {
                            Category cat2 = new Category();
                            cat2.setTotal_amount(amount);
                            cat2.setCategory(category);
                            cat2.setExpense_or_income(expense_or_income);
                            cat2.setUserID(userID);
                            Log.d("KATEGORIJA CAT2", cat2.getCategory() + " " + String.valueOf(cat2.getTotal_amount()) + " " + cat2.getUserID());
                            Log.d("ID STARE KATEGORIJE", categoryIDOld);
                            reffMyCategories.child(categoryIDOld).setValue(cat2);
                            Log.d("category_amount = 0", String.valueOf(category_amount));
                            Log.d(TAG, "KATEGORIJA JE OBRISANA");
                        } else {
                            cat.setTotal_amount(category_amount);
                            reffMyCategories.child(categoryIDOld).setValue(cat);
                            //Log.d("category_amount", String.valueOf(cat.getTotal_amount()));
                            makeNewCategory(amount, category, expense_or_income);
                        }

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "updateOldCategory2:onCancelled", databaseError.toException());
            }
        });
    }

    private void makeNewCategory(final float amount, final String category, final int expense_or_income) {

        reffMyFinancesCategoryID.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxcategoryID = (long) dataSnapshot.getValue();
                reffMyCategories = database.getReference().child("myCategories");
                cat = new Category();
                cat.setTotal_amount(amount);
                cat.setCategory(category);
                cat.setExpense_or_income(expense_or_income);
                cat.setUserID(userID);
                reffMyCategories.child(String.valueOf(maxcategoryID+1)).setValue(cat);
                reffMyFinancesCategoryID.setValue(maxcategoryID + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "makeNewCategory:onCancelled", databaseError.toException());
            }
        });
    }

    private void increaseCategoryAmountWhereBillIsAdded(final String categoryID, final float amount) {
        reffMyCategories.child(categoryID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float category_amount;
                cat = dataSnapshot.getValue(Category.class);
                category_amount = cat.getTotal_amount() + amount;
                cat.setTotal_amount(category_amount);
                reffMyCategories.child(categoryID).setValue(cat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "increaseCategoryAmountWhereBillIsAdded:onCancelled", databaseError.toException());
            }
        });
    }

    private void updateOldCategory() {
        reffMyCategories.child(categoryIDOld).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cat = dataSnapshot.getValue(Category.class);
                float category_amount;
                category_amount = cat.getTotal_amount() - amountOld;
                Log.d("amountOld", String.valueOf(cat.getTotal_amount()) + "  " + String.valueOf(amountOld));
                if (category_amount == 0 || category_amount == 0.0) {
                    reffMyCategories.child(categoryIDOld).removeValue();
                    Log.d("category_amount = 0", String.valueOf(category_amount));
                } else {
                    cat.setTotal_amount(category_amount);
                    reffMyCategories.child(categoryIDOld).setValue(cat);
                    Log.d("category_amount", String.valueOf(cat.getTotal_amount()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "updateOldCategory:onCancelled", databaseError.toException());
            }
        });
    }

    private boolean checkIfNewCategorySameAsOld(String category, String categoryOld) {
        boolean flag2 = false;
        if ((category.trim()).equals(categoryOld.trim())) {
            flag2 =  true;
        }
        return flag2;
    }

     private void updateCategory(final String category) {
        final Float[] total_amount = new Float[1];
        reffMyFinances = database.getReference().child("myFinances");
        reffMyFinances.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                total_amount[0] = 0.0f;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (category.equals(bill.getCategory()) && userID.equals(bill.getUserID())) {
                        total_amount[0] += bill.getAmount();
                        //Log.d("TOTAL AMOUNT", String.valueOf(total_amount[0]) + "  " + categoryID);
                    }
                }
                //Log.d("TAG", category + " " + categoryID + " " + total_amount[0]);
                cat = new Category(userID, category, expense_or_income, total_amount[0]);
                reffMyCategories.child(categoryID).setValue(cat);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "updateCategory:onCancelled", databaseError.toException());
            }
        });
    }

    private void updateBill(float amount, String category, String subcategory, String date) {
        reffMyFinances = database.getReference().child("myFinances").child(billID);
        bill = new Bill();
        bill.setAmount(amount);
        bill.setCategory(category);
        bill.setSubcategory(subcategory);
        bill.setDate(date);
        bill.setUserID(userID);
        bill.setExpense_or_income(expense_or_income);
        reffMyFinances.setValue(bill);
        Log.d("BILL_DATABASE", String.valueOf(bill.getAmount()) + " " + bill.getCategory() + "  " + bill.getSubcategory());
    }

    private boolean checkIfCategoryExist(String category) {
        if (userID.equals(cat.getUserID()) && category.equals(cat.getCategory())) {
            flag2 = true;
        }

        return flag2;
    }

    private void setDatePickerDialog() {
        dateFromDatabase = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                dateFromDatabase.set(Calendar.YEAR, year);
                dateFromDatabase.set(Calendar.MONTH, monthOfYear);
                dateFromDatabase.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        dateET.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(EditMyBillActivity.this, date, dateFromDatabase.get(Calendar.YEAR), dateFromDatabase.get(Calendar.MONTH),
                        dateFromDatabase.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        dateET.setText(sdf.format(dateFromDatabase.getTime()));
    }

    private void getDataFromDatabaseToEditText() {
        reffMyFinances = database.getReference().child("myFinances").child(billID);
        reffMyFinances.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bill = dataSnapshot.getValue(Bill.class);
                amountET.setText( String.valueOf(bill.getAmount()));
                categoryET.setText(bill.getCategory());
                subcategoryET.setText(bill.getSubcategory());
                dateET.setText(bill.getDate());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "EditMyBillActivity:getDataFromDatabaseToEditText", databaseError.toException());
            }
        });
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();
    }

    private void activityLayoutItemInitialization() {
        amountET = (EditText) findViewById(R.id.amountEditBillET);
        categoryET = (AutoCompleteTextView) findViewById(R.id.categoryEditBillACTV);
        subcategoryET = (AutoCompleteTextView) findViewById(R.id.subcategoryEditBillACTV);
        dateET = (EditText) findViewById(R.id.dateEditBillTV);
        saveBTN = (Button) findViewById(R.id.saveEditBillBTN);
        cancelBTN = (Button) findViewById(R.id.cancelEditBillBTN);

        setSuggestionsForCategories();

        setSuggestionsForSubcategories();
    }

    private void setSuggestionsForCategories() {
        reffMyCategories = database.getReference().child("myCategories");
        reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> katLista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    cat = ds.getValue(Category.class);
                    if (userID.equals(cat.getUserID()) && expense_or_income == cat.getExpense_or_income()) {
                        katLista.add(cat.getCategory());
                    }
                }
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(EditMyBillActivity.this, android.R.layout.simple_expandable_list_item_1, katLista);
                categoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "activityLayoutItemInitialization:onCancelled", databaseError.toException());
            }
        });
    }

    private void setSuggestionsForSubcategories() {
        reffMyFinances = database.getReference().child("myFinances");
        reffMyFinances.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> subkatLista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (userID.equals(bill.getUserID()) && expense_or_income == bill.getExpense_or_income()) {
                        subkatLista.add(bill.getSubcategory());
                    }
                }
                subkatLista = removeDuplicates(subkatLista);
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(EditMyBillActivity.this, android.R.layout.simple_expandable_list_item_1, subkatLista);
                subcategoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setSuggestionsForSubcategory:onCancelled", databaseError.toException());
            }
        });
    }

    private static <T> ArrayList<T> removeDuplicates(ArrayList<T> list) {
        Set<T> set = new LinkedHashSet<>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    private void sendUserToMyCategoryBillsActivityIfCategoryChanged(){
        Intent intent = new Intent(EditMyBillActivity.this, MyCategoryBillsActivity.class);
        intent.putExtra("category", categoryET.getText().toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToMyCategoryBillsActivity() {
        Intent intent = new Intent(EditMyBillActivity.this, MyCategoryBillsActivity.class);
        intent.putExtra("category", categoryOld);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getIncomingIntent(String key){
        String value = "";
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        if (getIntent().hasExtra(key)) {
            value = getIntent().getStringExtra(key);
        }
        return value;
    }

    /*@Override //kad pritisnem botun za poc nazad na toolbaru vraca me u kategoriju i salje parametre koji su potrebni
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getParentActivityIntent() == null) {
                    Log.i(TAG, "You have forgotten to specify the parentActivityName in the AndroidManifest!");
                    onBackPressed();
                } else {
                    Intent intent = new Intent(EditMyBillActivity.this, MyCategoryBillsActivity.class);
                    intent.putExtra("category", categoryET.getText().toString());
                    NavUtils.navigateUpTo(this, intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToMyCategoryBillsActivity();
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
