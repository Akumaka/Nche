package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.e.nche.MyUserPanel.MyUserAdapter;
import com.e.nche.MyUserPanel.MyUserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyUser extends AppCompatActivity {

    private List<MyUserModel> userList = new ArrayList<>();
    private MyUserModel model;
    private MyUserAdapter adapter;

    private RecyclerView recyclerView;

    private ProgressDialog mDialog;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Button btn_addUser, btn_add, btn_update;

    private EditText et_id, et_name, et_pwd, et_u_id, et_u_name, et_u_pwd;

    private TextView tv_cancel, tv_u_cancel, tv_complain_type;
    Spinner spinner_complain_type;

    private LinearLayout addNewUser;
    private LinearLayout updateUser;
    private RelativeLayout mainLayout;

    private String updateKey;

    public static String dbUser, dbName, superAdmin;

    private boolean update = false;
    private boolean add = false;

    private List<String> list_keys = new ArrayList<>();
    private List<String> list_Names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_user);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading, Please wait...");
        mDialog.setCancelable(false);
        mDialog.show();

        if (getIntent().getStringExtra("superAdmin") != null) {
            superAdmin = getIntent().getStringExtra("superAdmin");
        }

        recyclerView = findViewById(R.id.recyclerView_my_user);
        tv_complain_type = findViewById(R.id.tv_complain_type);
        spinner_complain_type = findViewById(R.id.spinner_complain_type);

        btn_addUser = findViewById(R.id.button_add_user);
        btn_add = findViewById(R.id.button_add_user_add);
        btn_update = findViewById(R.id.button_add_user_update_update);

        et_id = findViewById(R.id.editText_my_user_id);
        et_name = findViewById(R.id.editText_my_user_name);
        et_pwd = findViewById(R.id.editText_my_user_pwd);

        et_u_id = findViewById(R.id.editText_my_user_update_id);
        et_u_name = findViewById(R.id.editText_my_user_update_name);
        et_u_pwd = findViewById(R.id.editText_my_user_update_pwd);

        tv_cancel = findViewById(R.id.my_user_cancel);
        tv_u_cancel = findViewById(R.id.my_user_update_cancel);

        addNewUser = findViewById(R.id.newUser);
        updateUser = findViewById(R.id.updateUser);
        mainLayout = findViewById(R.id.my_user_main);

        Intent dbIntent = getIntent();
        dbUser = dbIntent.getStringExtra("dbUser");

        if (dbUser != null) {
            if (dbUser.equals("Users")) {
                dbName = "Users";
            } else if (dbUser.equals("Security")) {
                dbName = "Security";
            } else if (dbUser.equals("Admin")) {
                dbName = "Admin";
            } else if (dbUser.equals("SuperAdmin")) {
                dbName = "SuperAdmin";
            }
        }


        if (dbIntent.getStringExtra("UpdateUserKey") != null) {
            updateKey = dbIntent.getStringExtra("UpdateUserKey");
            updateUser(updateKey);
        }

        prepareUsersData();

        btn_addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add = true;
                addNewUser.setVisibility(View.VISIBLE);
                mainLayout.setVisibility(View.GONE);
            }
        });

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewUser.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            }
        });

        tv_u_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
            }
        });

        adapter = new MyUserAdapter(userList, dbName);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        if (dbName.equals("Security")) {
            spinner_complain_type.setVisibility(View.VISIBLE);
            tv_complain_type.setVisibility(View.VISIBLE);
        }

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Validate()) {

                    if (list_keys.contains(et_id.getText().toString())) {
                        et_id.setError("Already Exist");
                    } else if (list_Names.contains(et_name.getText().toString())) {
                        et_name.setError("Already Exist");
                    } else {
                        updateInDatabase();

                        addNewUser.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);
                        if (dbName.equals("Security"))
                            model = new MyUserModel(et_id.getText().toString(), et_name.getText().toString(), et_pwd.getText().toString(), spinner_complain_type.getSelectedItem().toString().trim());
                        else
                            model = new MyUserModel(et_id.getText().toString(), et_name.getText().toString(), et_pwd.getText().toString(), "");
                        userList.add(model);
                        adapter.notifyDataSetChanged();

                        et_id.setText("");
                        et_name.setText("");
                        et_pwd.setText("");
                    }
                } else {
                    Toast.makeText(MyUser.this, "Values Required", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uValidate()) {

                    if (list_Names.contains(et_u_name.getText().toString())) {
                        et_u_name.setError("Already Exist");
                    } else {
                        updateToDatabase();

                        updateUser.setVisibility(View.GONE);
                        mainLayout.setVisibility(View.VISIBLE);

                        et_u_id.setText("");
                        et_u_name.setText("");
                        et_u_pwd.setText("");

                        userList.clear();
                        prepareUsersData();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (update || add) {
            update = false;
            add = false;
            updateUser.setVisibility(View.GONE);
            mainLayout.setVisibility(View.VISIBLE);
            addNewUser.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    private void updateToDatabase() {
        Map<String, Object> note = new HashMap<>();

        note.put("Name", et_u_name.getText().toString());
        note.put("Password", et_u_pwd.getText().toString());

        db.collection(dbName).document(et_u_id.getText().toString()).update(note);

    }

    public void updateUser(String key) {
        //mDialog.show();
        update = true;
        prepareUserData(key);
        updateUser.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
    }

    private void prepareUserData(final String key) {
        db.collection(dbName).document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {

                            //User
                            String Name = documentSnapshot.get("Name").toString();
                            String Password = documentSnapshot.get("Password").toString();

                            if (key != null && Name != null && Password != null) {

                                et_u_id.setText(key);
                                et_u_name.setText(Name);
                                et_u_pwd.setText(Password);

                                Name = null;
                                Password = null;

                                mDialog.dismiss();
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            mDialog.dismiss();
                        }
                    }
                });
    }

    private void updateInDatabase() {
        Map<String, Object> note = new HashMap<>();

        note.put("Name", et_name.getText().toString());
        note.put("Password", et_pwd.getText().toString());
        if (dbName.equals("Security"))
            note.put("Type", spinner_complain_type.getSelectedItem().toString().trim());

        db.collection(dbName).document(et_id.getText().toString()).set(note);
    }


    private boolean Validate() {
        boolean result = true;

        if (TextUtils.isEmpty(et_id.getText().toString())) {
            result = false;
            et_id.setError("");
        }
        if (TextUtils.isEmpty(et_name.getText().toString())) {
            result = false;
            et_name.setError("");
        }
        if (TextUtils.isEmpty(et_pwd.getText().toString())) {
            result = false;
            et_pwd.setError("");
        }

        return result;
    }

    private boolean uValidate() {
        boolean result = true;

        if (TextUtils.isEmpty(et_u_id.getText().toString())) {
            result = false;
            et_u_id.setError("");
        }
        if (TextUtils.isEmpty(et_u_name.getText().toString())) {
            result = false;
            et_u_name.setError("");
        }
        if (TextUtils.isEmpty(et_u_pwd.getText().toString())) {
            result = false;
            et_u_pwd.setError("");
        }

        return result;
    }

    private void prepareUsersData() {
        list_keys.clear();
        list_Names.clear();
        mDialog.show();
        db.collection(dbName).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().size() > 0) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    String key = document.getId();
                                    prepareUsersDataEntries(key);
                                }
                            }
                        } else {
                            mDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.println(Log.ASSERT, "Users", e.toString());
                    }
                });
    }

    private void prepareUsersDataEntries(final String key) {
        db.collection(dbName).document(key)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot != null) {

                            //User
                            String Name, Password, Type = "";

                            if (documentSnapshot.exists()) {
                                Name = documentSnapshot.get("Name").toString();
                                Password = documentSnapshot.get("Password").toString();
                                if (dbName.equals("Security"))
                                    Type = documentSnapshot.get("Type").toString();

                                if (key != null && Name != null && Password != null) {

                                    list_keys.add(key);
                                    list_Names.add(Name);

                                    model = new MyUserModel(key, Name, Password, Type);

                                    Log.println(Log.ASSERT, "keys", key);
                                    userList.add(model);

                                    adapter.notifyDataSetChanged();
                                    Name = null;
                                    Password = null;

                                    mDialog.dismiss();
                                }
                            }
                        }
                    }
                });
    }
}
