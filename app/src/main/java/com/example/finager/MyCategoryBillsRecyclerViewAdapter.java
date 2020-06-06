package com.example.finager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyCategoryBillsRecyclerViewAdapter  extends RecyclerView.Adapter<MyCategoryBillsRecyclerViewAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<Bill> bills;
    private OnItemClickListener mListener;
    private int inc_or_exp;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public MyCategoryBillsRecyclerViewAdapter(Context c, ArrayList<Bill> b, int expense_or_income) {
        context = c;
        bills = b;
        inc_or_exp = expense_or_income;
    }

    @NonNull
    @Override
    public MyCategoryBillsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        if (inc_or_exp == 1) {
            v = LayoutInflater.from(context).inflate(R.layout.card_view_my_category_bills_expense, parent, false);
        } else {
            v = LayoutInflater.from(context).inflate(R.layout.card_view_my_category_bills_income, parent, false);
        }

        MyCategoryBillsRecyclerViewAdapter.MyViewHolder viewHolder = new MyCategoryBillsRecyclerViewAdapter.MyViewHolder(v, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyCategoryBillsRecyclerViewAdapter.MyViewHolder holder, int position) {
        Bill currentBill = bills.get(position);
        holder.billSubcategory.setText(currentBill.getSubcategory());

        if (currentBill.getExpense_or_income() == 1) {
            holder.billAmount.setText("-" + String.valueOf(currentBill.getAmount()));
        } else {
            holder.billAmount.setText(String.valueOf(currentBill.getAmount()));
        }

        holder.billDate.setText(currentBill.getDate());
        holder.editBill.setVisibility(View.VISIBLE);
        //holder.onEditClick(position);
        holder.deleteBill.setVisibility(View.VISIBLE);
        //holder.onDeleteClick(position);
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView billSubcategory, billAmount,  billDate;
        Button editBill, deleteBill;

        public MyViewHolder(@NonNull View itemView, final OnItemClickListener listener) { //konstruktor
            super(itemView);
            billAmount = (TextView) itemView.findViewById(R.id.amountCatBillTV);
            billSubcategory = (TextView) itemView.findViewById(R.id.subcategoryCatBillTV);
            billDate = (TextView) itemView.findViewById(R.id.dateCatBillTV);
            editBill = (Button) itemView.findViewById(R.id.editCatBillsBTN);
            deleteBill = (Button) itemView.findViewById(R.id.deleteCatBillsBTN);

            editBill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onEditClick(position);
                        }
                    }
                }
            });

            deleteBill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });


        }
        /*public void onEditClick(final int position) {
            editBill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }*/

        /*public void onDeleteClick(final int position) {
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
        }*/
    }

}
