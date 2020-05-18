package com.example.finager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.util.Calendar;
import java.util.Locale;

public class AddBillActivity extends AppCompatActivity {
    private EditText amountET;
    private EditText categoryET;
    private EditText subcategoryET;
    private TextView dateTV;
    private TextView probaTV;
    private int expense_or_income;
    private String userID;
    private long maxbillID = 0;
    private long maxcategoryID = 0;
    private Calendar currentDate;
    private int day, month, year;
    private Button cancelBTN;
    private Button addBTN;
    private String flagCatPostoji = "postoji";
    private String getCategoryID = "";
    private float getCategoryTotalAmount;
    private String getDateFromCategory;

    private FirebaseDatabase database;
    private DatabaseReference reffMyFinances;
    private DatabaseReference reffMyFinancesBillID;
    private DatabaseReference reffMyFinancesCategoryID;
    private DatabaseReference reffMyCategories;
    private DatabaseReference reffChangeAmountInCategory;
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

        //ako je pritisnut gumb za dodavanje expensa prikazuje se dizajn za expens (crveni dizajn) dok suprotno prikazuje se dizajn za income (zeleni dizajn)
        if (expense_or_income == 0) {
            setContentView(R.layout.activity_add_income);
        } else {
            setContentView(R.layout.activity_add_expense);
        }


        amountET = findViewById(R.id.amountET);
        categoryET = findViewById(R.id.categoryET);
        subcategoryET = findViewById(R.id.subcategoryET);
        dateTV = findViewById(R.id.dateTV);
        cancelBTN = findViewById(R.id.cancelBTN);
        addBTN = findViewById(R.id.addBTN);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userID = firebaseUser.getUid();
        database = FirebaseDatabase.getInstance();

        reffMyFinances = database.getReference().child("myFinances"); //referenca na myFinances gdje se upisuju racuni korisnika
        reffMyFinancesBillID = database.getReference().child("myFinancesBillID/id_bill");//referenca na myFinancesBillI/id_bill gdje se pohranjuje zadnje dodijeljeni id racunu
        reffMyFinancesCategoryID =  database.getReference().child("myFinancesBillID/id_category"); //referenca na myFinancesBillI/ic_category gdje se pohranjuje zadnje dodijeljeni id racunu
        reffMyCategories = database.getReference().child("myCategories");
        reffChangeAmountInCategory = database.getReference().child("myCategories");


        //dohvacanje trenutnog najveceg id-a dodijeljenog racunu
        reffMyFinancesBillID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxbillID = (long) dataSnapshot.getValue();
                Log.d("id_bill value:", String.valueOf(maxbillID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //dohvacanje trenutnog najveceg id-a dodijeljenog kategoriji
        reffMyFinancesCategoryID.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) maxcategoryID = (long) dataSnapshot.getValue();
                Log.d("id_category value:", String.valueOf(maxcategoryID));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //nepotreban dio koristen za debug
        probaTV = findViewById(R.id.probaTV);
        if (value == 0){
            probaTV.setText("poslana je 0");
        } else if (value == 1) {
            probaTV.setText("poslana je 1");
        }

        //postavljanje trenutnog datuma u textView
        currentDate = Calendar.getInstance();
        day = currentDate.get(Calendar.DAY_OF_MONTH);
        month = currentDate.get(Calendar.MONTH);
        year = currentDate.get(Calendar.YEAR);
        month = month + 1;
        updateLabel();

        //postavaljanje datepickera
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

        addBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkIfCategoryExist();
                addBillToDatabase();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToMainActivity();
            }
        });

    }

    public void checkIfCategoryExist() {
        final String category = categoryET.getText().toString().trim();
        final String amount = amountET.getText().toString().trim();
        final String date = dateTV.getText().toString().trim();
        String subcategory = subcategoryET.getText().toString().trim();
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
                    for (DataSnapshot dataS : dataSnapshot.getChildren()) {
                        Category cat = dataS.getValue(Category.class);
                        if (userID.equals(cat.getUserID()) && category.equals(cat.getCategory())) {
                            flagCatPostoji = "postoji";
                            getCategoryID = dataS.getKey();
                            getCategoryTotalAmount = cat.getTotal_amount();
                            getDateFromCategory = cat.getDate();
                            Log.d("Kategorija", cat.getCategory() + " " + cat.getUserID());
                            break;
                        }
                    }
                    if (flagCatPostoji == "ne_postoji") {
                        addCategoryToDatabase();
                    } else if (flagCatPostoji == "postoji") {
                        updateCategoryAmountAndDate(date, category, amount);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }




    public void updateCategoryAmountAndDate(String date, String category, String amount) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            //Log.d("DATUM", String.valueOf(sdf.parse(date).before(sdf.parse(getDateFromCategory))));
            if (!sdf.parse(date).before(sdf.parse(getDateFromCategory))) {
                getDateFromCategory = date;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        float amountCat = getCategoryTotalAmount + Float.parseFloat(amount);
        Category cat2 = new Category(userID, category, getDateFromCategory, expense_or_income, amountCat);

        Log.d("CATEGORY_ID", getCategoryID);
        Log.d("IZNOS", String.valueOf(amountCat));

        reffChangeAmountInCategory.child(getCategoryID).setValue(cat2);
    }

    public void addCategoryToDatabase() {
        String amount = amountET.getText().toString();
        String category = categoryET.getText().toString().trim();
        String subcategory = subcategoryET.getText().toString().trim();
        String date = dateTV.getText().toString().trim();
        boolean flag = true;

        //provjerava dal su sva polja ispunjena
        if ("".equals(category) || "".equals(amount) || "".equals(subcategory)) {
            flag = false;
        }
        if (flag) { //ako su sva polja ispunjena onda doda kategoriju u bazu
            Category kategorija = new Category(userID, category, date, expense_or_income, Float.valueOf(amount));
            //reffMyCategories.child(String.valueOf(categoryAddToDB+""+userID)).setValue(kategorija);
            reffMyCategories.child(String.valueOf(maxcategoryID + 1)).setValue(kategorija);
            reffMyFinancesCategoryID.setValue(maxcategoryID + 1);
            Log.d("TAG", "Kategorija uspjesno dodana!");
        }
    }

    public void addBillToDatabase() {
        boolean flag = true;
        String amount = amountET.getText().toString();
        String category = categoryET.getText().toString().trim();
        String subcategory = subcategoryET.getText().toString().trim();
        String date = dateTV.getText().toString().trim();
        float amountFloat = 0;

        if ("".equals(amountET.getText().toString().trim())){
            amountET.setError("Amount is required!");
            flag = false;
        } else {
            amountFloat = Float.parseFloat(amount);
        }
        if ("".equals(categoryET.getText().toString().trim())) {
            categoryET.setError("Category is required!");
            flag = false;

        }

        if ("".equals(subcategoryET.getText().toString().trim())) {
            subcategoryET.setError("Subcategory is required!");
            flag = false;
        }

        if (flag) {
            Bill bill = new Bill(amountFloat, category, subcategory, date, userID, expense_or_income);
            reffMyFinances.child(String.valueOf(maxbillID+1)).setValue(bill);
            reffMyFinancesBillID.setValue(maxbillID+1);
            sendUserToMainActivity();
           Log.d("TAG", "Račun uspjesno dodan!");
        }

    }


    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

        dateTV.setText(sdf.format(currentDate.getTime()));
    }

    public void sendUserToMainActivity(){
        Intent intent = new Intent(AddBillActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override //ako korisnik pritisne u Constraint layout miče se tipkovnica iz fokusa
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(AddBillActivity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
