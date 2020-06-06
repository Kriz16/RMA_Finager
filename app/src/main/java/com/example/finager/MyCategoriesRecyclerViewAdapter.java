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

public class MyCategoriesRecyclerViewAdapter extends RecyclerView.Adapter<MyCategoriesRecyclerViewAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Category> categories;
    private OnItemClickListenerCat mListener;

    public interface OnItemClickListenerCat {
        void onItemClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListenerCat listener) {
        mListener = listener;
    }


    public MyCategoriesRecyclerViewAdapter(Context c, ArrayList<Category> cat) {
        context = c;
        categories = cat;
    }

    @NonNull
    @Override
    public MyCategoriesRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_view_my_categories, parent, false);
        MyCategoriesRecyclerViewAdapter.MyViewHolder viewHolder = new MyCategoriesRecyclerViewAdapter.MyViewHolder(v, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyCategoriesRecyclerViewAdapter.MyViewHolder holder, int position) {
        Category currentCat = categories.get(position);
        holder.categoryName.setText(currentCat.getCategory());

        if (currentCat.getExpense_or_income() == 1) {
            holder.categoryAmount.setText("-" + String.valueOf(currentCat.getTotal_amount()));
        } else {
            holder.categoryAmount.setText(String.valueOf(currentCat.getTotal_amount()));
        }
        //holder.categoryDate.setText(currentCat.getDate());
        holder.showCategoryBills.setVisibility(View.VISIBLE);
        //holder.onClick(position);
        holder.editCategory.setVisibility(View.VISIBLE);
        holder.deleteCategory.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName, categoryAmount;
        //TextView categoryDate;
        Button showCategoryBills;
        Button editCategory;
        Button deleteCategory;

        public MyViewHolder(@NonNull View itemView, final OnItemClickListenerCat listener) { //konstruktor
            super(itemView);
            categoryName = (TextView) itemView.findViewById(R.id.categoryCatTV);
            categoryAmount = (TextView) itemView.findViewById(R.id.amountCatTV);
            //categoryDate = (TextView) itemView.findViewById(R.id.dateCatTV);
            showCategoryBills = (Button) itemView.findViewById(R.id.showCatBillsBTN);
            editCategory = (Button) itemView.findViewById(R.id.editCatBillsBTN);
            deleteCategory = (Button) itemView.findViewById(R.id.deleteCatBillsBTN);

            editCategory.setOnClickListener(new View.OnClickListener() {
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

            deleteCategory.setOnClickListener(new View.OnClickListener() {
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

            showCategoryBills.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });

        }


        //prijasne riješenje
        /*public void onClick(final int position) { //uz to je zakomentiran holder.onClick(position); u onBindViewHolder
            showCategoryBills.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context , MyCategoryBillsActivity.class);
                    intent.putExtra("category", categories.get(position).getCategory());
                    context.startActivity(intent);
                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
                }
            });
        }*/
    }
}
