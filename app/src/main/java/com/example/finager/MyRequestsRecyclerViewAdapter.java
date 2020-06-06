package com.example.finager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRequestsRecyclerViewAdapter extends RecyclerView.Adapter<MyRequestsRecyclerViewAdapter.MyViewHolder>{
    private FirebaseDatabase database;
    private DatabaseReference reffMyGroups;
    private String group_name;

    private Context context;
    private ArrayList<Request> requests;
    private MyRequestsRecyclerViewAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onYesClick(int position);
        void onNoClick(int position);
    }

    public void setOnItemClickListener(MyRequestsRecyclerViewAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public MyRequestsRecyclerViewAdapter(Context c, ArrayList<Request> r) {
        context = c;
        requests = r;
    }

    @NonNull
    @Override
    public MyRequestsRecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.card_view_my_request, parent, false);
        MyRequestsRecyclerViewAdapter.MyViewHolder viewHolder = new MyRequestsRecyclerViewAdapter.MyViewHolder(v, mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyRequestsRecyclerViewAdapter.MyViewHolder holder, final int position) {
        database = FirebaseDatabase.getInstance();
        final Request currentRequest = requests.get(position);
        reffMyGroups = database.getReference().child("myGroups").child(currentRequest.getGroup_id()).child("name");

        reffMyGroups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) group_name = (String) dataSnapshot.getValue();
                String request_message = currentRequest.getInvited_by() + " invites you to join the group " + group_name;
                holder.requestTV.setText(request_message);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.yesBTN.setVisibility(View.VISIBLE);
        holder.noBTN.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView requestTV;
        Button yesBTN, noBTN;

        public MyViewHolder(@NonNull View itemView, final MyRequestsRecyclerViewAdapter.OnItemClickListener listener) { //konstruktor
            super(itemView);
            requestTV = (TextView) itemView.findViewById(R.id.requestMessageTV);
            yesBTN = (Button) itemView.findViewById(R.id.yesRequestBTN);
            noBTN = (Button) itemView.findViewById(R.id.noRequestBTN);

            yesBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onYesClick(position);
                        }
                    }
                }
            });

            noBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onNoClick(position);
                        }
                    }
                }
            });

        }
    }

}
