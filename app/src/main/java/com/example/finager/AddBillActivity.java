package com.example.finager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class AddBillActivity extends AppCompatActivity {
    private EditText amountET;
    private AutoCompleteTextView categoryET;
    private AutoCompleteTextView subcategoryET;
    private TextView dateTV;
    private int expense_or_income;
    private String userID;
    private long maxbillID = 0;
    private long maxcategoryID = 0;
    private long maxbillGroupID = 0;
    private long maxcategoryGroupID = 0;
    private Calendar currentDate;
    private int day, month, year;
    private Button cancelBTN;
    private Button addBTN;
    private String flagCatPostoji = "postoji";
    private String getCategoryID = "";
    private float getCategoryTotalAmount;
    private String getDateFromCategory;
    private Bill racun;
    private float total_amount = 0;
    private String dateToStore;
    private String TAG = "AddBillActivity";
    private Category cat;
    private Bill bill;
    private String group_id;

    private FirebaseDatabase database;
    private DatabaseReference reffMyFinances;
    private DatabaseReference reffMyFinancesBillID;
    private DatabaseReference reffMyFinancesCategoryID;
    private DatabaseReference reffMyCategories;
    private DatabaseReference reffChangeAmountInCategory;
    private DatabaseReference reffMyCategoriesGroup;
    private DatabaseReference reffMyFinancesBillGroupID;
    private DatabaseReference reffMyFinancesGroup;
    private DatabaseReference reffMyFinancesCategoryGroupID;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //hvatanje podataka iz bundla poslanih preko drugog intenta, ako je poslana 0 racun je income ako je poslana 1 racun je expens
        Bundle b = new Bundle();
        int value = -1;
        if(b != null)
            b = getIntent().getExtras();
            value = b.getInt("expense_or_income");
        expense_or_income = value;
        Log.d("EXPENSE_OR_INCOME", String.valueOf(expense_or_income));

        if(b != null)
            b = getIntent().getExtras();
        group_id = b.getString("group_id");
        Log.d("GROUP_ID_BUNDLE", String.valueOf(group_id));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //ako je pritisnut gumb za dodavanje expensa prikazuje se dizajn za expens (crveni dizajn) dok suprotno prikazuje se dizajn za income (zeleni dizajn)
        setCorrectContentView();

        firebaseItemsInitialization();

        activityLayoutItemInitialization();

        //dohvacanje trenutnog najveceg id-a dodijeljenog racunu
        getMaxBillID();

        getMaxBillGroupID();

        //dohvacanje trenutnog najveceg id-a dodijeljenog kategoriji
        getMaxCategoryGroupID();

        getMaxCategoryID();

        //postavljanje trenutnog datuma u textView
        setCurrentDateInTV();
        updateLabel();

        //postavaljanje datepickera
        setDatePickerDialog();

        setButtons();

    }

    private void checkIfCategoryInGroupExist(final float amount, final String category, final String subcategory, final String date) {
        flagCatPostoji = "ne_postoji";
        boolean flag = true;
        Log.d("TUUUU SAN", "TUUU SAN U GRUPI");
        //provjerava dal su sva polja ispunjena
        if ("".equals(category) || "".equals(amount) || "".equals(subcategory)) {
            flag = false;
        }
        if (flag) {
            reffMyCategoriesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean wrongCategory = false;
                    Category cat;
                    for (DataSnapshot dataS : dataSnapshot.getChildren()) {
                        cat = dataS.getValue(Category.class);
                        if (category.equals(cat.getCategory())) {
                            flagCatPostoji = "postoji";
                            getCategoryID = dataS.getKey();
                            getCategoryTotalAmount = cat.getTotal_amount();
                            //getDateFromCategory = cat.getDate();
                            Log.d("Kategorija", cat.getCategory() + " " + cat.getUserID());
                            //break; AKO NEŠTO S RAČUNIMA NE RADI ONDA TO TREBA ODKOMENTIRAT
                        }
                        //provjera dal je izabrana ispravna kategorija (ako je racun prihod ne smijem ubacit novi racun u kategoriju koja je trosak)
                        if (category.equals(cat.getCategory()) && expense_or_income != cat.getExpense_or_income()) {
                            if (expense_or_income == 0) {
                                categoryET.setError("Chosen category is't income category");
                            } else {
                                categoryET.setError("Chosen category is't expense category");
                            }
                            wrongCategory = true;
                        }
                        Log.d("ISPIS",  cat.getCategory() + " " + String.valueOf(expense_or_income) + "  " + String.valueOf(cat.getExpense_or_income()));
                    }
                    Log.d("WRONG CATEGORY", String.valueOf(wrongCategory));
                    if (!wrongCategory) {
                        Bill bill = new Bill(amount, category, subcategory, date, userID, expense_or_income);
                        reffMyFinancesGroup.child(String.valueOf(maxbillGroupID+1)).setValue(bill);
                        reffMyFinancesBillGroupID.setValue(maxbillGroupID+1);
                        if (flagCatPostoji.equals("ne_postoji")) {
                            addCategoryToDatabaseInGroup();
                        } else if (flagCatPostoji.equals("postoji")) {
                            updateCategoryAmountInGroup( category, amount);
                            //updateCategory(category);
                        }
                        sendUserToGroupActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "checkIfCategoryInGroupExist:onCancelled", databaseError.toException());
                }
            });
        }
    }

    private void updateCategoryAmountInGroup( String category, float amount) {
        float amountCat = getCategoryTotalAmount + amount;
        Category cat2 = new Category(userID, category, expense_or_income, amountCat);

        Log.d("CATEGORY_ID", getCategoryID);
        Log.d("IZNOS", String.valueOf(amountCat));

        reffMyCategoriesGroup.child(getCategoryID).setValue(cat2);
    }

    private void addCategoryToDatabaseInGroup() {
        String amount = amountET.getText().toString();
        String category = categoryET.getText().toString().trim();
        String subcategory = subcategoryET.getText().toString().trim();
        //String date = dateTV.getText().toString().trim();
        boolean flag = true;

        //provjerava dal su sva polja ispunjena
        if ("".equals(category) || "".equals(amount) || "".equals(subcategory)) {
            flag = false;
        }
        if (flag) { //ako su sva polja ispunjena onda doda kategoriju u bazu
            //Category kategorija = new Category(userID, category, date, expense_or_income, Float.valueOf(amount));
            Category kategorija = new Category(userID, category, expense_or_income, Float.valueOf(amount));
            //reffMyCategories.child(String.valueOf(categoryAddToDB+""+userID)).setValue(kategorija);
            reffMyCategoriesGroup.child(String.valueOf(maxcategoryGroupID + 1)).setValue(kategorija);
            reffMyFinancesCategoryGroupID.setValue(maxcategoryGroupID + 1);
            Log.d("TAG", "Kategorija uspjesno dodana!");
        }
    }

    private void checkIfCategoryExist(final float amount, final String category, final String subcategory, final String date) {
        /*final String category = categoryET.getText().toString().trim();
        final String amount = amountET.getText().toString().trim();
        final String date = dateTV.getText().toString().trim();
        final String subcategory = subcategoryET.getText().toString().trim();*/
        flagCatPostoji = "ne_postoji";
        boolean flag = true;

        //provjerava dal su sva polja ispunjena
        if ("".equals(category) || "".equals(amount) || "".equals(subcategory)) {
            flag = false;
        }
        if (flag) {
            reffMyCategories.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean wrongCategory = false;
                    Category cat;
                    for (DataSnapshot dataS : dataSnapshot.getChildren()) {
                        cat = dataS.getValue(Category.class);
                        if (userID.equals(cat.getUserID()) && category.equals(cat.getCategory())) {
                            flagCatPostoji = "postoji";
                            getCategoryID = dataS.getKey();
                            getCategoryTotalAmount = cat.getTotal_amount();
                            //getDateFromCategory = cat.getDate();
                            Log.d("Kategorija", cat.getCategory() + " " + cat.getUserID());
                            //break; AKO NEŠTO S RAČUNIMA NE RADI ONDA TO TREBA ODKOMENTIRAT
                        }
                        //provjera dal je izabrana ispravna kategorija (ako je racun prihod ne smijem ubacit novi racun u kategoriju koja je trosak)
                        if (userID.equals(cat.getUserID()) && category.equals(cat.getCategory()) && expense_or_income != cat.getExpense_or_income()) {
                            if (expense_or_income == 0) {
                                categoryET.setError("Chosen category is't income category");
                            } else {
                                categoryET.setError("Chosen category is't expense category");
                            }
                            wrongCategory = true;
                        }
                        Log.d("ISPIS",  cat.getCategory() + " " + String.valueOf(expense_or_income) + "  " + String.valueOf(cat.getExpense_or_income()));
                    }
                    Log.d("WRONG CATEGORY", String.valueOf(wrongCategory));
                    if (!wrongCategory) {
                        Bill bill = new Bill(amount, category, subcategory, date, userID, expense_or_income);
                        reffMyFinances.child(String.valueOf(maxbillID+1)).setValue(bill);
                        reffMyFinancesBillID.setValue(maxbillID+1);
                        if (flagCatPostoji.equals("ne_postoji")) {
                            addCategoryToDatabase();
                        } else if (flagCatPostoji.equals("postoji")) {
                            updateCategoryAmount( category, amount);
                            //updateCategory(category);
                        }
                        sendUserToMainActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "checkIfCategoryExist:onCancelled", databaseError.toException());
                }
            });
        }
    }

    /*private void updateCategory(final String category) {
        racun = new Bill();
        //dateToStore = "00/00/0000";
        reffMyFinances = database.getReference().child("myFinances");
        reffMyFinances.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    racun = ds.getValue(Bill.class);
                    if (userID.equals(racun.getUserID()) && category.equals(racun.getCategory())) {
                        //Log.d("RAČUN", racun.getAmount() + " " + racun.getDate());
                        total_amount += racun.getAmount();
                        //dateToStore = compareTwoDates(dateToStore, racun.getDate());
                        //Log.d("DATUM", dateToStore + "  " + total_amount);
                    }
                }
                //Log.d("CATEGORY_ID", getCategoryID);
                //Log.d("IZNOS", String.valueOf(total_amount));
                //Category cat2 = new Category(userID, category, dateToStore, expense_or_income, total_amount);
                Category cat2 = new Category(userID, category, expense_or_income, total_amount);
                reffMyCategories.child(getCategoryID).setValue(cat2);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "updateCategory:onCancelled", databaseError.toException());
            }
        });
    }*/

    //metoda usporeduje dva datuma i vraca zadnji datum
    /*private String compareTwoDates(String date, String recentDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            //Log.d("DATUM", String.valueOf(sdf.parse(date).before(sdf.parse(getDateFromCategory))));
            if (!sdf.parse(date).before(sdf.parse(recentDate))) {
                recentDate = date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return recentDate;
    }*/

    private void setButtons() {
        addBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBillToDatabase();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (group_id.equals("nije_grupa")) {
                    sendUserToMainActivity();
                } else {
                    sendUserToGroupActivity();
                }
            }
        });
    }

    private void setDatePickerDialog() {
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                currentDate.set(Calendar.YEAR, year);
                currentDate.set(Calendar.MONTH, monthOfYear);
                currentDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        dateTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(AddBillActivity.this, date, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH),
                        currentDate.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateCategoryAmount( String category, float amount) {
        /*SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            //Log.d("DATUM", String.valueOf(sdf.parse(date).before(sdf.parse(getDateFromCategory))));
            if (!sdf.parse(date).before(sdf.parse(getDateFromCategory))) {
                getDateFromCategory = date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        float amountCat = getCategoryTotalAmount + amount;
        Category cat2 = new Category(userID, category, expense_or_income, amountCat);

        Log.d("CATEGORY_ID", getCategoryID);
        Log.d("IZNOS", String.valueOf(amountCat));

        reffChangeAmountInCategory.child(getCategoryID).setValue(cat2);
    }

    private void addCategoryToDatabase() {
        String amount = amountET.getText().toString();
        String category = categoryET.getText().toString().trim();
        String subcategory = subcategoryET.getText().toString().trim();
        //String date = dateTV.getText().toString().trim();
        boolean flag = true;

        //provjerava dal su sva polja ispunjena
        if ("".equals(category) || "".equals(amount) || "".equals(subcategory)) {
            flag = false;
        }
        if (flag) { //ako su sva polja ispunjena onda doda kategoriju u bazu
            //Category kategorija = new Category(userID, category, date, expense_or_income, Float.valueOf(amount));
            Category kategorija = new Category(userID, category, expense_or_income, Float.valueOf(amount));
            //reffMyCategories.child(String.valueOf(categoryAddToDB+""+userID)).setValue(kategorija);
            reffMyCategories.child(String.valueOf(maxcategoryID + 1)).setValue(kategorija);
            reffMyFinancesCategoryID.setValue(maxcategoryID + 1);
            Log.d("TAG", "Kategorija uspjesno dodana!");
        }
    }

    private void addBillToDatabase() {
        boolean flag = true;
        String amount = amountET.getText().toString().trim();
        String category = categoryET.getText().toString().trim();
        String subcategory = subcategoryET.getText().toString().trim();
        String date = dateTV.getText().toString().trim();
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
            if (group_id.equals("nije_grupa")) {
                checkIfCategoryExist(amountFloat, category, subcategory, date);
            } else {
                checkIfCategoryInGroupExist(amountFloat, category, subcategory, date);
            }
           Log.d("TAG", "Račun uspjesno dodan!");
        }

    }

    private void setCorrectContentView() {
        if (expense_or_income == 0) {
            setContentView(R.layout.activity_add_income);
        } else {
            setContentView(R.layout.activity_add_expense);
        }
    }

    private void getMaxBillGroupID() {
        reffMyFinancesBillGroupID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxbillGroupID = (long) dataSnapshot.getValue();
                Log.d("id_bill_group value:", String.valueOf(maxbillGroupID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getMaxBillID:onCancelled", databaseError.toException());
            }
        });
    }

    private void getMaxBillID() {
        reffMyFinancesBillID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxbillID = (long) dataSnapshot.getValue();
                Log.d("id_bill value:", String.valueOf(maxbillID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getMaxBillID:onCancelled", databaseError.toException());
            }
        });
    }

    private void getMaxCategoryID(){
        reffMyFinancesCategoryID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxcategoryID = (long) dataSnapshot.getValue();
                //Log.d("id_category value:", String.valueOf(maxcategoryID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getMaxCategoryID:onCancelled", databaseError.toException());
            }
        });
    }

    private void getMaxCategoryGroupID() {
        reffMyFinancesCategoryGroupID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxcategoryGroupID = (long) dataSnapshot.getValue();
                //Log.d("id_category value:", String.valueOf(maxcategoryID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "getMaxCategoryID:onCancelled", databaseError.toException());
            }
        });
    }

    private void setCurrentDateInTV() {
        currentDate = Calendar.getInstance();
        day = currentDate.get(Calendar.DAY_OF_MONTH);
        month = currentDate.get(Calendar.MONTH);
        year = currentDate.get(Calendar.YEAR);
        month = month + 1;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        dateTV.setText(sdf.format(currentDate.getTime()));
    }

    private void firebaseItemsInitialization() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();

        reffMyFinances = database.getReference().child("myFinances"); //referenca na myFinances gdje se upisuju racuni korisnika
        reffMyFinancesBillID = database.getReference().child("myFinancesBillID/id_bill");//referenca na myFinancesBillI/id_bill gdje se pohranjuje zadnje dodijeljeni id racunu
        reffMyFinancesCategoryID =  database.getReference().child("myFinancesBillID/id_category"); //referenca na myFinancesBillI/ic_category gdje se pohranjuje zadnje dodijeljeni id racunu
        reffMyCategories = database.getReference().child("myCategories");
        reffChangeAmountInCategory = database.getReference().child("myCategories");
        reffMyCategoriesGroup = database.getReference().child("myCategoriesGroup/" + group_id);
        reffMyFinancesBillGroupID = database.getReference().child("myFinancesBillID/id_bill_group");
        reffMyFinancesCategoryGroupID = database.getReference().child("myFinancesBillID/id_category_group");
        reffMyFinancesGroup = database.getReference().child("myFinancesGroup/" + group_id);
    }

    private void activityLayoutItemInitialization() {
        amountET = findViewById(R.id.amountET);
        categoryET = findViewById(R.id.categoryACTV);
        subcategoryET = findViewById(R.id.subcategoryACTV);
        dateTV = findViewById(R.id.dateTV);
        cancelBTN = findViewById(R.id.cancelBTN);
        addBTN = findViewById(R.id.addBTN);


        if (group_id.equals("nije_grupa")) {
            setSuggestionsForCategory();
            setSuggestionsForSubcategory();
        } else {
            setSuggestionsForCategoryGroup();
            setSuggestionsForSubcategoryGroup();
        }
    }

    private void setSuggestionsForCategoryGroup() {
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
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(AddBillActivity.this, android.R.layout.simple_expandable_list_item_1, katLista);
                categoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setSuggestionsForCategory:onCancelled", databaseError.toException());
            }
        });
    }

    private void setSuggestionsForSubcategoryGroup() {
        reffMyFinancesGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> subkatLista = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    bill = ds.getValue(Bill.class);
                    if (expense_or_income == bill.getExpense_or_income()) {
                        subkatLista.add(bill.getSubcategory());
                    }
                }
                subkatLista = removeDuplicates(subkatLista);
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(AddBillActivity.this, android.R.layout.simple_expandable_list_item_1, subkatLista);
                subcategoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setSuggestionsForSubcategory:onCancelled", databaseError.toException());
            }
        });
    }

    private void setSuggestionsForCategory() {
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
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(AddBillActivity.this, android.R.layout.simple_expandable_list_item_1, katLista);
                categoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setSuggestionsForCategory:onCancelled", databaseError.toException());
            }
        });
    }

    private void setSuggestionsForSubcategory() {
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
                ArrayAdapter<String> actvAdapter = new ArrayAdapter<String>(AddBillActivity.this, android.R.layout.simple_expandable_list_item_1, subkatLista);
                subcategoryET.setAdapter(actvAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "setSuggestionsForSubcategory:onCancelled", databaseError.toException());
            }
        });
    }

    private static <T> ArrayList<T> removeDuplicates(ArrayList<T> list){
        // Create a new LinkedHashSet
        Set<T> set = new LinkedHashSet<>();
        // Add the elements to set
        set.addAll(list);
        // Clear the list
        list.clear();
        // add the elements of set
        // with no duplicates to the list
        list.addAll(set);
        // return the list
        return list;
    }

    private void sendUserToGroupActivity() {
        Intent intent = new Intent(AddBillActivity.this, GroupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("group_id" , group_id);
        startActivity(intent);
        finish();
    }

    private void sendUserToMainActivity(){
        Intent intent = new Intent(AddBillActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    /*@Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl() {
        Intent i = null;
        Bundle extras = getIntent().getExtras();
        String group_id = extras.getString("group_id");

        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
        if (!group_id.equals("")) {
            i = new Intent(this, MainActivity.class);
            // set any flags or extras that you need.
            // If you are reusing the previous Activity (i.e. bringing it to the top
            // without re-creating a new instance) set these flags:
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // if you are re-using the parent Activity you may not need to set any extras
            i.putExtra("someExtra", "whateverYouNeed");
        } else {
            i = new Intent(this, MyGroupsActivity.class);
            // same comments as above
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("someExtra", "whateverYouNeed");
        }

        return i;
    }*/

    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToGroupActivity();
    }*/

    @Override //ako korisnik pritisne u Constraint layout miče se tipkovnica iz fokusa
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(AddBillActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
