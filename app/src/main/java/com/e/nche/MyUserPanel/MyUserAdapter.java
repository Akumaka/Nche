package com.e.nche.MyUserPanel;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.e.nche.MyUser;
import com.e.nche.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class MyUserAdapter extends RecyclerView.Adapter<MyUserAdapter.MyViewHolder> {

    private List<MyUserModel> userList;
    private String dbName;

    public MyUserAdapter(List<MyUserModel> userList, String dbName) {
        this.userList = userList;
        this.dbName = dbName;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_my_user, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        final MyUserModel model = userList.get(position);

        holder.m_u_id.setText(model.getId());
        holder.m_u_name.setText(model.getName());
        holder.m_u_pwd.setText(model.getPassword());
        if (dbName.equals("Security"))
            holder.m_u_type.setText(model.getType());
        else
            holder.m_u_type.setVisibility(View.GONE);

        holder.m_u_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyUser.dbName.equals("SuperAdmin")) {
                    if (MyUser.superAdmin.equals("MainSuperAdmin")) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Delete entry")
                                .setMessage("Are you sure you want to delete this entry?")

                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                        userList.remove(holder.getAdapterPosition());
                                        notifyItemRemoved(holder.getAdapterPosition());
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        db.collection(MyUser.dbName).document(model.getId()).delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.println(Log.ASSERT, "Delete", "Ok");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.println(Log.ASSERT, "Delete", "Error" + e.toString());
                                                    }
                                                });
                                    }
                                })

                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();

                    } else {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle("Delete entry")
                                .setMessage("You have no rights to delete a super admin.")

                                .setPositiveButton(android.R.string.ok, null)
                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }

                } else {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this entry?")

                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    userList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection(MyUser.dbName).document(model.getId()).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.println(Log.ASSERT, "Delete", "Ok");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.println(Log.ASSERT, "Delete", "Error" + e.toString());
                                                }
                                            });
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }

            }
        });

        holder.update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MyUser.class);
                intent.putExtra("UpdateUserKey", model.getId());
                intent.putExtra("dbUser", MyUser.dbUser);
                v.getContext().startActivity(intent);

                ((Activity) v.getContext()).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView m_u_id, m_u_name, m_u_pwd, m_u_type;
        public ImageButton m_u_delete;
        public LinearLayout update;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            m_u_id = itemView.findViewById(R.id.m_u_id);
            m_u_name = itemView.findViewById(R.id.m_u_name);
            m_u_pwd = itemView.findViewById(R.id.m_u_pwd);
            m_u_type = itemView.findViewById(R.id.m_u_type);
            m_u_delete = itemView.findViewById(R.id.m_u_delete);
            update = itemView.findViewById(R.id.linearlayout_update);
        }
    }
}
