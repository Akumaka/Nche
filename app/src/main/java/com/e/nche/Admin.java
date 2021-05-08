package com.e.nche;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.e.nche.Email.Connection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Admin extends AppCompatActivity {

    private EditText et_name, et_pwd;

    private Button btn_login;

    private CheckBox cb_remember;

    private String status;

    private List<String> list_key = new ArrayList<>();
    private java.util.List<String> list_name = new ArrayList<>();
    private List<String> list_pwd = new ArrayList<>();

    private List<String> list_key_super = new ArrayList<>();
    private java.util.List<String> list_name_super = new ArrayList<>();
    private List<String> list_pwd_super = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ProgressDialog mDialog;

    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mDialog = new ProgressDialog(Admin.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(false);

        et_name = findViewById(R.id.editText_admin_user_name);
        et_pwd = findViewById(R.id.editText_admin_user_pwd);

        btn_login = findViewById(R.id.button_admin_login);

        cb_remember = findViewById(R.id.checkbox_remember);

        Intent intent = getIntent();
        status = intent.getStringExtra("status");

        if (status.equals("Admin")) {
            SharedPreferences shAdmin = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
            if (shAdmin.contains("CodeCoy_Vicab_Remember_Admin_Login")) {
                boolean login = shAdmin.getBoolean("CodeCoy_Vicab_Remember_Admin_Login", false);
                if (login) {
                    SharedPreferences sharedPreferences = getSharedPreferences("statusAdmin", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("status", "Admin");
                    myEdit.apply();

                    Intent admin = new Intent(Admin.this, AdminVehicles.class);
                    admin.putExtra("status", "Admin");
                    startActivity(admin);
                    finish();
                }
            }
        } else {
            SharedPreferences shMainSuperAdmin = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
            if (shMainSuperAdmin.contains("CodeCoy_Vicab_Remember_Main_Super_Admin_Login")) {
                boolean login = shMainSuperAdmin.getBoolean("CodeCoy_Vicab_Remember_Main_Super_Admin_Login", false);
                if (login) {
                    Intent sMIntent = new Intent(Admin.this, SuperAdmin.class);
                    sMIntent.putExtra("MainAdmin", "MainSuperAdmin");
                    startActivity(sMIntent);
                    finish();
                }
            }

            SharedPreferences shSuperAdmin = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
            if (shSuperAdmin.contains("CodeCoy_Vicab_Remember_Super_Admin_Login")) {
                boolean login = shSuperAdmin.getBoolean("CodeCoy_Vicab_Remember_Super_Admin_Login", false);
                if (login) {
                    Intent sIntent = new Intent(Admin.this, SuperAdmin.class);
                    sIntent.putExtra("MainAdmin", "SuperAdmin");
                    startActivity(sIntent);
                    finish();
                }
            }
        }

        if (checkInternet()) {
            connected = true;
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_name.getText().toString().isEmpty()) {
                    et_name.setError("user name require");
                    return;
                }

                if (et_pwd.getText().toString().isEmpty()) {
                    et_pwd.setError("password require");
                    return;
                }

                if (connected) {
                    if (status.equals("Admin")) {
                        mDialog.show();
                        if (!et_name.getText().toString().isEmpty() && !et_pwd.getText().toString().isEmpty()) {
                            String email = et_name.getText().toString() + "@mydomain.com";
                            String pwd = et_pwd.getText().toString();
                            mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        if (cb_remember.isChecked()) {
                                            SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                            myEdit.putBoolean("CodeCoy_Vicab_Remember_Admin_Login", true);
                                            myEdit.remove("CodeCoy_Vicab_Remember_Main_Super_Admin_Login");
                                            myEdit.remove("CodeCoy_Vicab_Remember_Super_Admin_Login");
                                            myEdit.apply();
                                        }

                                        SharedPreferences sharedPreferences = getSharedPreferences("statusAdmin", MODE_PRIVATE);
                                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                        myEdit.putString("status", "Admin");
                                        myEdit.apply();

                                        Intent intent = new Intent(Admin.this, AdminVehicles.class);
                                        intent.putExtra("status", "Admin");
                                        startActivity(intent);
                                        finish();
                                    }else {
                                        loginUser();
                                    }
                                }
                            });
                        }
                    } else {
                        mDialog.show();
                        if (!et_name.getText().toString().isEmpty() && !et_pwd.getText().toString().isEmpty()) {
                            String email = et_name.getText().toString() + "@mydomain.com";
                            String pwd = et_pwd.getText().toString();

                            mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Admin.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                                        if (cb_remember.isChecked()) {
                                            SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                            myEdit.putBoolean("CodeCoy_Vicab_Remember_Main_Super_Admin_Login", true);
                                            myEdit.remove("CodeCoy_Vicab_Remember_Admin_Login");
                                            myEdit.remove("CodeCoy_Vicab_Remember_Super_Admin_Login");
                                            myEdit.apply();
                                        }

                                        Intent intent = new Intent(Admin.this, SuperAdmin.class);
                                        intent.putExtra("MainAdmin", "MainSuperAdmin");
                                        startActivity(intent);
                                        finish();
                                        mDialog.dismiss();
                                    } else {
                                        databaseCollectionSuper();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    if (checkInternet()) {
                        connected = true;
                    }
                }
            }
        });
    }

    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(Admin.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(Admin.this);
        }
        return result;
    }

    private void loginUser() {

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    databaseCollection();
                }
            }
        });
    }

    private void databaseCollection() {
        list_key.clear();
        list_name.clear();
        list_pwd.clear();
        db.collection("Admin").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsers(key);
                    }
                    databaseCollectionAdminSuper();
                } else {
                    mDialog.dismiss();
                    et_name.setError("");
                    et_pwd.setError("");
                    //Log.println(Log.ASSERT, "Error login :", task.getException().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void readUsers(String key) {
        list_key.add(key);
        db.collection("Admin").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("Name").toString();
                String pwd = documentSnapshot.get("Password").toString();

                if (!name.equals(null) && !pwd.equals(null)) {
                    list_name.add(name);
                    list_pwd.add(pwd);

                    if (list_name.contains(et_name.getText().toString())) {
                        int index = list_name.indexOf(et_name.getText().toString());
                        String userPwd = list_pwd.get(index);

                        if (userPwd.equals(et_pwd.getText().toString())) {
                            Toast.makeText(Admin.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                            if (cb_remember.isChecked()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.putBoolean("CodeCoy_Vicab_Remember_Admin_Login", true);
                                myEdit.remove("CodeCoy_Vicab_Remember_Main_Super_Admin_Login");
                                myEdit.remove("CodeCoy_Vicab_Remember_Super_Admin_Login");
                                myEdit.apply();
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences("statusAdmin", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            myEdit.putString("status", "Admin");
                            myEdit.apply();

                            Intent intent = new Intent(Admin.this, AdminVehicles.class);
                            intent.putExtra("status", "Admin");
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                        } else {
                            mDialog.dismiss();
                            et_pwd.setError("Wrong Password");
                        }
                    }

                    name = null;
                    pwd = null;
                }
            }
        });
    }

    private void databaseCollectionAdminSuper() {
        list_key_super.clear();
        list_name_super.clear();
        list_pwd_super.clear();
        db.collection("SuperAdmin").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.isSuccessful() && task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsersSuperAdmin(key);
                    }
                } else {
                    mDialog.dismiss();
                    et_name.setError("");
                    et_pwd.setError("");
                    //Toast.makeText(Admin.this, "There is no Super Admin.", Toast.LENGTH_SHORT).show();
                    //Log.println(Log.ASSERT, "Error login :", task.getException().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void readUsersSuperAdmin(String key) {
        list_key_super.add(key);
        db.collection("SuperAdmin").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("Name").toString();
                String pwd = documentSnapshot.get("Password").toString();

                if (!name.equals(null) && !pwd.equals(null)) {
                    list_name_super.add(name);
                    list_pwd_super.add(pwd);

                    name = null;
                    pwd = null;

                    if (list_name_super.contains(et_name.getText().toString())) {

                        int index = list_name_super.indexOf(et_name.getText().toString());
                        String userPwd = list_pwd_super.get(index);

                        if (userPwd.equals(et_pwd.getText().toString())) {
                            Toast.makeText(Admin.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                            if (cb_remember.isChecked()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.putBoolean("CodeCoy_Vicab_Remember_Admin_Login", true);
                                myEdit.remove("CodeCoy_Vicab_Remember_Main_Super_Admin_Login");
                                myEdit.remove("CodeCoy_Vicab_Remember_Super_Admin_Login");
                                myEdit.apply();
                            }

                            SharedPreferences sharedPreferences = getSharedPreferences("statusAdmin", MODE_PRIVATE);
                            SharedPreferences.Editor myEdit = sharedPreferences.edit();
                            myEdit.putString("status", "Admin");
                            myEdit.apply();

                            Intent intent = new Intent(Admin.this, AdminVehicles.class);
                            intent.putExtra("status", "Admin");
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                        }
                    } else {
                        et_name.setError("Not a user");
                        mDialog.dismiss();
                    }
                }
            }
        });
    }

    private void databaseCollectionSuper() {
        list_key_super.clear();
        list_name_super.clear();
        list_pwd_super.clear();
        db.collection("SuperAdmin").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.isSuccessful() && task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsersSuper(key);
                    }
                } else {
                    mDialog.dismiss();
                    et_name.setError("");
                    et_pwd.setError("");
                    //Toast.makeText(Admin.this, "There is no Super Admin.", Toast.LENGTH_SHORT).show();
                    //Log.println(Log.ASSERT, "Error login :", task.getException().toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void readUsersSuper(String key) {
        list_key_super.add(key);
        db.collection("SuperAdmin").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("Name").toString();
                String pwd = documentSnapshot.get("Password").toString();

                if (!name.equals(null) && !pwd.equals(null)) {
                    list_name_super.add(name);
                    list_pwd_super.add(pwd);

                    name = null;
                    pwd = null;

                    if (list_name_super.contains(et_name.getText().toString())) {

                        int index = list_name_super.indexOf(et_name.getText().toString());
                        String userPwd = list_pwd_super.get(index);

                        if (userPwd.equals(et_pwd.getText().toString())) {
                            Toast.makeText(Admin.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                            if (cb_remember.isChecked()) {
                                SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.putBoolean("CodeCoy_Vicab_Remember_Super_Admin_Login", true);
                                myEdit.remove("CodeCoy_Vicab_Remember_Main_Super_Admin_Login");
                                myEdit.remove("CodeCoy_Vicab_Remember_Admin_Login");
                                myEdit.apply();
                            }

                            Intent intent = new Intent(Admin.this, SuperAdmin.class);
                            intent.putExtra("MainAdmin", "SuperAdmin");
                            startActivity(intent);
                            finish();
                            mDialog.dismiss();
                        } else {
                            et_pwd.setError("Wrong Password");
                            mDialog.dismiss();
                        }
                    } else {
                        et_name.setError("Not a user");
                        mDialog.dismiss();
                    }
                }
            }
        });
    }
}
