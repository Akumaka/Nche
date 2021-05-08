package com.e.nche.SecurityAgentsModel;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.e.nche.AgentComplain;
import com.e.nche.R;
import com.e.nche.ShowComplain;
import com.e.nche.UpdateComplain;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ComplainModelAdapter extends RecyclerView.Adapter<ComplainModelAdapter.MyViewHolder> {

    private List<ComplainModel> complainList;
    String type;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> list_key = new ArrayList<>();

    public ComplainModelAdapter(List<ComplainModel> complainList, String type) {
        this.complainList = complainList;
        this.type = type;
        loginUser();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_complain, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final ComplainModel complainModel = complainList.get(position);

        holder.c_code.setText(complainModel.getCode());
        holder.c_unique.setText(complainModel.getUnique());
        holder.c_model.setText(complainModel.getModel());
        holder.c_name.setText(complainModel.getName());
        holder.c_email.setText(complainModel.getEmail());
        holder.c_phone.setText(complainModel.getPhone());
        holder.c_remarks.setText(complainModel.getRemarks());
        holder.c_attachment.setText(complainModel.getAttachment());

        if (!type.equals("General Complains")) {
            holder.c_code.setVisibility(View.GONE);
            holder.c_model.setVisibility(View.GONE);
        }

        holder.c_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                holder.c_code.setBackground(v.getContext().getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));

                if (list_key.contains(complainModel.getCode())) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            holder.c_code.setBackground(v.getContext().getResources().getDrawable(R.drawable.recycler_view_borders));
                            Intent intent = new Intent(v.getContext(), ShowComplain.class);
                            intent.putExtra("ShowComplainId", complainModel.getCode());
                            v.getContext().startActivity(intent);
                        }
                    }, 1000);
                } else {
                    holder.c_code.setBackground(v.getContext().getResources().getDrawable(R.drawable.recycler_view_borders));
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Not Registered")
                            .setMessage("Unique Id is not registered?")
                            .setPositiveButton(android.R.string.ok, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }
        });

        holder.c_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                holder.c_attachment.setBackground(v.getContext().getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        holder.c_attachment.setBackground(v.getContext().getResources().getDrawable(R.drawable.recycler_view_borders));
                        Intent intent = new Intent(v.getContext(), UpdateComplain.class);
                        intent.putExtra("Hide", "Agent");
                        intent.putExtra("type", type);
                        Log.e("ComplainId", "Complain Model Adapter: " + AgentComplain.complainIdList.get(position));
                        intent.putExtra("ComplainId", AgentComplain.complainIdList.get(position));
                        v.getContext().startActivity(intent);
                    }
                }, 1000);
            }
        });
    }

    @Override
    public int getItemCount() {
        return complainList.size();
    }

    public void filterList(ArrayList<ComplainModel> filteredList) {
        complainList = filteredList;
        notifyDataSetChanged();
    }

    private void loginUser() {

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.println(Log.ASSERT, "PublicWatch", "Log in");

                    uploadCollection();
                }
            }
        });
    }

    private void uploadCollection() {
        db.collection("Registered Vehicles").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                list_key.add(document.getId());
                            }
                        } else {
                            Log.println(Log.ASSERT, "Error login :", task.getException().toString());
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView c_code, c_unique, c_model, c_name, c_email, c_phone, c_remarks, c_attachment;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            c_code = itemView.findViewById(R.id.c_code);
            c_unique = itemView.findViewById(R.id.c_unique);
            c_model = itemView.findViewById(R.id.c_model);
            c_name = itemView.findViewById(R.id.c_name);
            c_email = itemView.findViewById(R.id.c_email);
            c_phone = itemView.findViewById(R.id.c_phone);
            c_remarks = itemView.findViewById(R.id.c_remarks);
            c_attachment = itemView.findViewById(R.id.c_attach);
        }
    }
}
