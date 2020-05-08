package com.example.finager;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddBillDialog extends AppCompatDialogFragment {
    private EditText amountET;
    private EditText categoryET;
    private EditText subcategoriyET;
    private TextView dateTV;
    private int expense_or_income;
    private String userID;
    private DatabaseReference reff;
    private long maxid=0;
    private Calendar currentDate;
    private int day, month, year;

    public AddBillDialog(int expense_or_income, String userID) { //konstruktor
        this.expense_or_income = expense_or_income;
        this.userID = userID;
    }

    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.add_bill_dialog, null);

        reff = FirebaseDatabase.getInstance().getReference().child("myFinances"); //referenca na myFinances gdje se upisuju racuni korisnika
        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    maxid = (dataSnapshot.getChildrenCount());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        builder.setView(view);

        if (expense_or_income == 0) {
            builder.setTitle("ADD INCOME");
        } else {
            builder.setTitle("ADD EXPENSE");
        }

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Canceled...", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String replaced_amount = amountET.getText().toString().replace(',', '.');
                float amount = Float.parseFloat(replaced_amount);
                String category = categoryET.getText().toString();
                String subcategory = subcategoriyET.getText().toString();
                String date = dateTV.getText().toString();

                Bill bill = new Bill(amount, category, subcategory, date, userID, expense_or_income);
                reff.child(String.valueOf(maxid+1)).setValue(bill);
                Toast.makeText(getContext(), "Data inserted successfully!", Toast.LENGTH_SHORT).show();
                //String proba = amount + " " + category + " " + subcategory;
                //Log.d("vjezba", proba);
            }
        });

        amountET = view.findViewById(R.id.amountET);
        categoryET = view.findViewById(R.id.categoryET);
        subcategoriyET = view.findViewById(R.id.subcategoryET);
        dateTV = view.findViewById(R.id.dateTV);

        //postavljanje trenutnog datuma u textView
        currentDate = Calendar.getInstance();
        day = currentDate.get(Calendar.DAY_OF_MONTH);
        month = currentDate.get(Calendar.MONTH);
        year = currentDate.get(Calendar.YEAR);
        month = month + 1;
        dateTV.setText(day+"/"+month+"/"+year);

        dateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int y, int m, int d) {
                        m = m + 1;
                        dateTV.setText(d+"/"+m+"/"+y);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
        return builder.create();
    }



    public int getExpense_or_income() {
        return expense_or_income;
    }

    public void setExpense_or_income(int expense_or_income) {
        this.expense_or_income = expense_or_income;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
