package com.example.finager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<MyCategoriesRecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Category> categories;

    public MyCategoriesRecyclerViewAdapter(Context c, ArrayList<Category> cat) {
        context = c;
        categories = cat;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.card_view_my_categories, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.categoryName.setText(categories.get(position).getCategory());
        holder.categoryAmount.setText(String.valueOf(categories.get(position).getTotal_amount()));
        holder.categoryDate.setText(categories.get(position).getDate());
        holder.showCategoryBills.setVisibility(View.VISIBLE);
        holder.onClick(position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, categoryAmount,  categoryDate;
        Button showCategoryBills;
        public MyViewHolder(@NonNull View itemView) {

            super(itemView);
            categoryName = (TextView) itemView.findViewById(R.id.categoryCatTV);
            categoryAmount = (TextView) itemView.findViewById(R.id.amountCatTV);
            categoryDate = (TextView) itemView.findViewById(R.id.dateCatTV);
            showCategoryBills = (Button) itemView.findViewById(R.id.showCatBillsBTN);
        }
        public void onClick(final int position) {
            showCategoryBills.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context , MyCategoryBillsActivity.class);
                    //Bundle bundle = new Bundle(); //slanje podataka iz aktivnosti u aktivnost preko Bundlea tj. dal je pritisnut expense ili income
                    //bundle.putString();
                    intent.putExtra("category", categories.get(position).getCategory());
                    context.startActivity(intent);
                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
