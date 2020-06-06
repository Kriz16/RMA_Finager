package com.example.finager;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.cardview.widget.CardView;
        import androidx.recyclerview.widget.RecyclerView;

        import java.util.ArrayList;

public class MyGroupsRecyclerViewAdapter  extends RecyclerView.Adapter<MyGroupsRecyclerViewAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<Group> groups;
    private OnItemClickListener mListener;
    private int inc_or_exp;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public MyGroupsRecyclerViewAdapter(Context c, ArrayList<Group> g) {
        context = c;
        groups = g;
    }

    @NonNull
    @Override
    public MyGroupsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_view_my_groups, parent, false);
        MyGroupsRecyclerViewAdapter.MyViewHolder viewHolder = new MyGroupsRecyclerViewAdapter.MyViewHolder(v, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyGroupsRecyclerViewAdapter.MyViewHolder holder, int position) {
        Group currentGroup = groups.get(position);
        holder.groupNameTV.setText(currentGroup.getName());
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTV;

        public MyViewHolder(@NonNull View itemView, final OnItemClickListener listener) { //konstruktor
            super(itemView);
            groupNameTV = (TextView) itemView.findViewById(R.id.groupNameTV);
            groupNameTV.setOnClickListener(new View.OnClickListener() {
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
    }

}
