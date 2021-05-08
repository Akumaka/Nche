package com.e.nche;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

public class Login extends AppCompatActivity {

    private EditText et_name, et_pwd;
    private Button btn_login;

    private CheckBox cb_remember;

    private List<String> list_key = new ArrayList<>();
    private List<String> list_name = new ArrayList<>();
    private List<String> list_pwd = new ArrayList<>();
    private List<String> list_type = new ArrayList<>();

    private List<String> list_key_super = new ArrayList<>();
    private java.util.List<String> list_name_super = new ArrayList<>();
    private List<String> list_pwd_super = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean connected = false;
    String type;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_name = findViewById(R.id.editText_login_user_name);
        et_pwd = findViewById(R.id.editText_login_user_pwd);

        btn_login = findViewById(R.id.button_login);

        cb_remember = findViewById(R.id.checkbox_remember);

        mDialog = new ProgressDialog(Login.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(false);

        SharedPreferences sh = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
        if (sh.contains("CodeCoy_Vicab_Remember_Security_Login")) {
            boolean status = sh.getBoolean("CodeCoy_Vicab_Remember_Security_Login", false);
            if (status) {
                String name = sh.getString("CodeCoy_Vicab_Remember_Security_Login_Name", "");
                String pwd = sh.getString("CodeCoy_Vicab_Remember_Security_Login_Pwd", "");
                String typ = sh.getString("CodeCoy_Vicab_Remember_Security_Login_Typ", "");
                Intent intent = new Intent(Login.this, SecurityAgents.class);
                intent.putExtra("UserName", name);
                intent.putExtra("type", typ);
                startActivity(intent);
                finish();
            }
        }


        if (checkInternet()) {
            connected = true;
            loginUser();
            databaseCollectionSuper();
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
                    if (!et_name.getText().toString().isEmpty() && !et_pwd.getText().toString().isEmpty()) {
                        mDialog.show();
                        String email = et_name.getText().toString() + "@mydomain.com";
                        String pwd = et_pwd.getText().toString();
                        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (cb_remember.isChecked()) {
                                        SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                        myEdit.putBoolean("CodeCoy_Vicab_Remember_Security_Login", true);
                                        myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Name", et_name.getText().toString());
                                        myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Pwd", et_pwd.getText().toString());
                                        myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Typ", type);
                                        myEdit.apply();
                                    }

                                    Intent intent = new Intent(Login.this, SecurityAgents.class);
                                    intent.putExtra("UserName", et_name.getText().toString());
                                    intent.putExtra("type", type);
                                    startActivity(intent);
                                    finish();
                                    mDialog.dismiss();
                                } else {
                                    superUser();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            normalUser();
                                        }
                                    }, 1000);
                                    ;
                                }
                            }
                        });
                    }
                } else {
                    if (checkInternet()) {
                        connected = true;
                        loginUser();
                        databaseCollectionSuper();
                    }
                }

            }
        });
    }

    private void normalUser() {
        if (list_name.contains(et_name.getText().toString())) {
            int index = list_name.indexOf(et_name.getText().toString());
            String userPwd = list_pwd.get(index);

            if (userPwd.equals(et_pwd.getText().toString())) {
                Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                type = list_type.get(index);

                if (cb_remember.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putBoolean("CodeCoy_Vicab_Remember_Security_Login", true);
                    myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Name", et_name.getText().toString());
                    myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Pwd", et_pwd.getText().toString());
                    myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Typ", type);
                    myEdit.apply();
                }

                Intent intent = new Intent(Login.this, SecurityAgents.class);
                intent.putExtra("UserName", et_name.getText().toString());
                intent.putExtra("type", type);
                startActivity(intent);
                finish();
                mDialog.dismiss();
            } else {
                mDialog.dismiss();
                et_pwd.setError("Wrong Password");
            }
        } else {
            mDialog.dismiss();
            et_name.setError("Wrong User Name");
        }
    }

    private void superUser() {
        if (list_name_super.contains(et_name.getText().toString())) {

            int index = list_name_super.indexOf(et_name.getText().toString());
            String userPwd = list_pwd_super.get(index);

            if (userPwd.equals(et_pwd.getText().toString())) {
                Toast.makeText(Login.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                if (cb_remember.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putBoolean("CodeCoy_Vicab_Remember_Security_Login", true);
                    myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Name", et_name.getText().toString());
                    myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Pwd", et_pwd.getText().toString());
                    myEdit.putString("CodeCoy_Vicab_Remember_Security_Login_Typ", type);
                    myEdit.apply();
                }
                Intent intent = new Intent(Login.this, SecurityAgents.class);
                intent.putExtra("UserName", et_name.getText().toString());
                intent.putExtra("type", type);
                startActivity(intent);
                finish();
                mDialog.dismiss();
            } else {
                mDialog.dismiss();
                et_pwd.setError("Wrong Password");
            }
        }
    }

    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(Login.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(Login.this);
        }
        return result;
    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(Login.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.println(Log.ASSERT, "Security", "Log in");
                    databaseCollection();
                    try {
                        mProgressDialog.dismiss();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void databaseCollection() {
        list_key.clear();
        list_name.clear();
        list_pwd.clear();
        list_type.clear();
        db.collection("Security").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsers(key);
                    }
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
        db.collection("Security").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("Name").toString();
                String pwd = documentSnapshot.get("Password").toString();
                String type = documentSnapshot.get("Type").toString();

                if (!name.equals(null) && !pwd.equals(null)) {
                    list_name.add(name);
                    list_pwd.add(pwd);
                    list_type.add(type);

                    name = null;
                    pwd = null;
                    type = null;
                }
            }
        });
    }

    private void databaseCollectionSuper() {
        final ProgressDialog mProgressDialog = new ProgressDialog(Login.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        list_key_super.clear();
        list_name_super.clear();
        list_pwd_super.clear();
        db.collection("SuperAdmin").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsersSuper(key);
                        mProgressDialog.dismiss();
                    }
                } else {
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
                }
            }
        });
    }

}
