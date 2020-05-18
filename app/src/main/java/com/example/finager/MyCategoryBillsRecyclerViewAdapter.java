package com.example.finager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyCategoryBillsRecyclerViewAdapter  extends RecyclerView.Adapter<MyCategoryBillsRecyclerViewAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<Bill> bills;

    public MyCategoryBillsRecyclerViewAdapter(Context c, ArrayList<Bill> b) {
        context = c;
        bills = b;
    }

    @NonNull
    @Override
    public MyCategoryBillsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyCategoryBillsRecyclerViewAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view_my_category_bills, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyCategoryBillsRecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.billSubcategory.setText(bills.get(position).getCategory());
        holder.billAmount.setText(String.valueOf(bills.get(position).getAmount()));
        holder.billDate.setText(bills.get(position).getDate());
        holder.editBill.setVisibility(View.VISIBLE);
        holder.onEditClick(position);
        holder.deleteBill.setVisibility(View.VISIBLE);
        holder.onDeleteClick(position);
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView billSubcategory, billAmount,  billDate;
        Button editBill, deleteBill;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            billAmount = (TextView) itemView.findViewById(R.id.amountCatBillTV);
            billSubcategory = (TextView) itemView.findViewById(R.id.subcategoryCatBillTV);
            billDate = (TextView) itemView.findViewById(R.id.dateCatBillTV);
            editBill = (Button) itemView.findViewById(R.id.editCatBillsBTN);
            deleteBill = (Button) itemView.findViewById(R.id.deleteCatBillsBTN);
        }
        public void onEditClick(final int position) {
            editBill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*Intent intent = new Intent(context , MyCategoryBillsActivity.class);
                    //Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                    //bundle.putString();
                    intent.putExtra("category", bills.get(position).getCategory());
                    context.startActivity(intent);*/
                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void onDeleteClick(final int position) {
            deleteBill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle("Delete");
                    alertDialog.setMessage("Are you sure you want to delete this bill?");
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(context, position+" will be deleted.", Toast.LENGTH_SHORT).show();
                            deleteBillFromDatabase();
                            updateCategoryTotalAmountAndDate();
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.setCancelable(true);
                            Toast.makeText(context, position+" will NOT be deleted.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    alertDialog.create().show();

                    //Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteBillFromDatabase() {

    }

    private void updateCategoryTotalAmountAndDate() {

    }

}
