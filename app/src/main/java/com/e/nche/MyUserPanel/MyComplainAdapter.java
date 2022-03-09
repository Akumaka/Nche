package com.e.nche.MyUserPanel;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.e.nche.MyComplain;
import com.e.nche.R;
import com.e.nche.UpdateComplain;

import java.util.ArrayList;
import java.util.List;

public class MyComplainAdapter extends RecyclerView.Adapter<MyComplainAdapter.MyViewHolder> {
    private List<MyComplainModel> complainList;
    private String type;

    public MyComplainAdapter(List<MyComplainModel> complainList, String type) {
        this.complainList = complainList;
        this.type = type;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_complain, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        final MyComplainModel complainModel = complainList.get(position);

        Log.e("...,,,///", "" + complainModel.getTimeStamp());
        holder.c_time.setText(complainModel.getTimeStamp());
        holder.c_code.setText(complainModel.getCode());
//        holder.c_model.setText(complainModel.getModel());
        holder.c_unique.setText(complainModel.getUnique());
        holder.c_name.setText(complainModel.getName());
        holder.c_email.setText(complainModel.getEmail());
        holder.c_phone.setText(complainModel.getPhone());
        holder.c_remarks.setText(complainModel.getRemarks());
        holder.c_attachment.setText(complainModel.getAttachment());

        holder.c_model.setVisibility(View.GONE);
        if (complainModel.getModel().equals("General Complaints"))
            holder.c_code.setVisibility(View.VISIBLE);
        else
            holder.c_code.setVisibility(View.GONE);

        holder.click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.println(Log.ASSERT, "Key", complainModel.getCode());
                //Log.println(Log.ASSERT, "KeyBy", MyComplain.complainIdList.get(position));
                Intent intent = new Intent(v.getContext(), UpdateComplain.class);
                intent.putExtra("type", type);
                intent.putExtra("Hide", "Super");
                intent.putExtra("ComplainId", MyComplain.complainIdList.get(position));
                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return complainList.size();
    }

    public void filterList(ArrayList<MyComplainModel> filteredList) {
        complainList = filteredList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView c_time, c_code, c_unique, c_model, c_name, c_email, c_phone, c_remarks, c_attachment;
        public LinearLayout click;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            c_time = itemView.findViewById(R.id.c_time);
            c_code = itemView.findViewById(R.id.c_code);
            c_unique = itemView.findViewById(R.id.c_unique);
            c_model = itemView.findViewById(R.id.c_model);
            c_name = itemView.findViewById(R.id.c_name);
            c_email = itemView.findViewById(R.id.c_email);
            c_phone = itemView.findViewById(R.id.c_phone);
            c_remarks = itemView.findViewById(R.id.c_remarks);
            c_attachment = itemView.findViewById(R.id.c_attach);
            click = itemView.findViewById(R.id.click);
        }
    }
}
