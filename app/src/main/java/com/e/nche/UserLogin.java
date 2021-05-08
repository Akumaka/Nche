package com.e.nche;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.e.nche.Email.Connection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;

public class UserLogin extends AppCompatActivity {

    // Navigation_bar
//    private DrawerLayout drawerLayout;
//    private NavigationView navigationView;
//    private Toolbar toolbar;
//    private ActionBarDrawerToggle toggle;

    private EditText et_userName, et_pwd;
    private Button btn_login, btn_public, btn_complains, btn_security;
    private CheckBox cb_remember;

    private List<String> list_key = new ArrayList<>();
    private List<String> list_name = new ArrayList<>();
    private List<String> list_pwd = new ArrayList<>();

    private List<String> list_key_super = new ArrayList<>();
    private java.util.List<String> list_name_super = new ArrayList<>();
    private List<String> list_pwd_super = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference userRef = FirebaseFirestore.getInstance().collection("Users");

    private boolean connected = false;

    boolean cameraPermission = false;
    boolean readStoragePermission = false;
    boolean writeStoragePermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        Typeface font = Typeface.createFromAsset(getResources().getAssets(), "fonts/algerianRegular.ttf");
        TextView titleTv = findViewById(R.id.titleTv);
        titleTv.setTypeface(font);

        if (getIntent().getStringExtra("BackPress") == null)
            checkLogin();

        requestWriteStoragePermission();

        if (checkInternet()) {
            connected = true;
            checkLogin();
            databaseCollectionSuper();
        }

        //Navigation_bar
//        toolbar = findViewById(R.id.toolbar);
//        drawerLayout = findViewById(R.id.drawer_layout);
//        navigationView = findViewById(R.id.nav_view);
//        toggle = new ActionBarDrawerToggle(UserLogin.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

        et_userName = findViewById(R.id.editText_user_login_user_name);
        et_pwd = findViewById(R.id.editText_user_login_pwd_et);

        btn_login = findViewById(R.id.button_user_login);
        btn_public = findViewById(R.id.button_user_public);
        btn_complains = findViewById(R.id.button_user_complains);
        btn_security = findViewById(R.id.button_user_security);

        cb_remember = findViewById(R.id.checkbox_remember);

        et_userName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    et_userName.requestFocus();
                    InputMethodManager imm = (InputMethodManager) UserLogin.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        et_userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_userName.requestFocus();
                InputMethodManager imm = (InputMethodManager) UserLogin.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        et_pwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    et_pwd.requestFocus();
                    InputMethodManager imm = (InputMethodManager) UserLogin.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });

        et_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_pwd.requestFocus();
                InputMethodManager imm = (InputMethodManager) UserLogin.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog mProgressDialog = new ProgressDialog(UserLogin.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();

                if (et_userName.getText().toString().isEmpty()) {
                    et_userName.setError("user name require");
                    mProgressDialog.dismiss();
                    return;
                }

                if (et_pwd.getText().toString().isEmpty()) {
                    et_pwd.setError("password require");
                    mProgressDialog.dismiss();
                    return;
                }

                if (connected) {
                    if (!et_userName.getText().toString().isEmpty() && !et_pwd.getText().toString().isEmpty()) {
                        //mProgressDialog.show();
                        String email = et_userName.getText().toString() + "@mydomain.com";
                        String pwd = et_pwd.getText().toString();
                        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (cb_remember.isChecked()) {
                                        SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                        myEdit.putBoolean("CodeCoy_Vicab_Remember_User_Login", true);
                                        myEdit.putString("CodeCoy_Vicab_Remember_User_Login_Name", et_userName.getText().toString());
                                        myEdit.putString("CodeCoy_Vicab_Remember_User_Login_Pwd", et_pwd.getText().toString());
                                        myEdit.apply();
                                    }
                                    if (cameraPermission == true && readStoragePermission == true && writeStoragePermission == true) {
                                        Toast.makeText(UserLogin.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(UserLogin.this, VehicleInformation.class);
                                        intent.putExtra("UserName", et_userName.getText().toString());
                                        startActivity(intent);
                                        finish();
                                        mProgressDialog.dismiss();
                                    } else {
                                        Log.println(Log.ASSERT, "Not", "Permission");
                                        mProgressDialog.dismiss();
                                        requestCameraPermission();
                                        requestReadStoragePermission();
                                        requestWriteStoragePermission();
                                    }
                                } else {
                                    mProgressDialog.dismiss();
                                    superUser();
                                }
                            }
                        });
                    }
                } else {
                    mProgressDialog.dismiss();
                    if (checkInternet()) {
                        connected = true;
                        loginUser();
                        databaseCollectionSuper();
                    }
                }

            }
        });

        btn_public.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLogin.this, PublicWatch.class);
                startActivity(intent);
            }
        });

//        btn_complains.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(UserLogin.this, ComplainsActivity.class);
//                startActivity(intent);
//            }
//        });

        btn_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLogin.this, Login.class);
                startActivity(intent);

            }
        });

//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
//                    case R.id.action_login:
//                        Intent loginIntent = new Intent(UserLogin.this, UserLogin.class);
//                        startActivity(loginIntent);
//                        break;
//                    case R.id.action_admin:
//                        Intent adminIntent = new Intent(UserLogin.this, Admin.class);
//                        adminIntent.putExtra("status", "Admin");
//                        startActivity(adminIntent);
//                        break;
//                    case R.id.super_admin:
//                        Intent superAdminIntent = new Intent(UserLogin.this, Admin.class);
//                        superAdminIntent.putExtra("status", "Super Admin");
//                        startActivity(superAdminIntent);
//                        break;
//                }
//                return false;
//            }
//        });
    }

    private void normalUser() {
        if (list_name.contains(et_userName.getText().toString())) {
            int index = list_name.indexOf(et_userName.getText().toString());
            String userPwd = list_pwd.get(index);

            if (userPwd.equals(et_pwd.getText().toString())) {

                if (cb_remember.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putBoolean("CodeCoy_Vicab_Remember_User_Login", true);
                    myEdit.putString("CodeCoy_Vicab_Remember_User_Login_Name", et_userName.getText().toString());
                    myEdit.putString("CodeCoy_Vicab_Remember_User_Login_Pwd", et_pwd.getText().toString());
                    myEdit.apply();
                }
                if (cameraPermission == true && readStoragePermission == true && writeStoragePermission == true) {
                    Toast.makeText(UserLogin.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserLogin.this, VehicleInformation.class);
                    intent.putExtra("UserName", et_userName.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                    Log.println(Log.ASSERT, "Not_Normal", "Permission");
                    requestCameraPermission();
                    requestReadStoragePermission();
                    requestWriteStoragePermission();
                }
            } else {
                et_pwd.setError("Wrong Password");
            }
        } else {
            et_userName.setError("Wrong User Name");
        }
    }

    private void superUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(UserLogin.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Requesting");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        if (list_name_super.contains(et_userName.getText().toString())) {

            int index = list_name_super.indexOf(et_userName.getText().toString());
            String userPwd = list_pwd_super.get(index);

            if (userPwd.equals(et_pwd.getText().toString())) {
                Toast.makeText(UserLogin.this, "Login Successfully", Toast.LENGTH_SHORT).show();

                if (cb_remember.isChecked()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putBoolean("CodeCoy_Vicab_Remember_User_Login", true);
                    myEdit.putString("CodeCoy_Vicab_Remember_User_Login_Name", et_userName.getText().toString());
                    myEdit.putString("CodeCoy_Vicab_Remember_User_Login_Pwd", et_pwd.getText().toString());
                    myEdit.apply();
                }
                if (cameraPermission && readStoragePermission && writeStoragePermission) {
                    Toast.makeText(UserLogin.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserLogin.this, VehicleInformation.class);
                    intent.putExtra("UserName", et_userName.getText().toString());
                    startActivity(intent);
                    finish();
                    mProgressDialog.dismiss();
                } else {
                    mProgressDialog.dismiss();
                    requestCameraPermission();
                    requestReadStoragePermission();
                    requestWriteStoragePermission();
                }
            } else {
                mProgressDialog.dismiss();
                et_pwd.setError("Wrong Password");
            }
        } else {
            //mProgressDialog.dismiss();
            normalUser();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();
                }
            }, 1000);
        }
    }

    private void checkLogin() {
        SharedPreferences sh = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
        if (sh.contains("CodeCoy_Vicab_Remember_User_Login")) {
            //mProgressDialog.show();
            boolean status = sh.getBoolean("CodeCoy_Vicab_Remember_User_Login", false);
            if (status) {
                String name = sh.getString("CodeCoy_Vicab_Remember_User_Login_Name", "");
                String pwd = sh.getString("CodeCoy_Vicab_Remember_User_Login_Pwd", "");
                Intent intent = new Intent(UserLogin.this, VehicleInformation.class);
                intent.putExtra("UserLogin", "Remember");
                intent.putExtra("UserName", name);
                startActivity(intent);
                finish();
            } else
                loginUser();
        }
    }

    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(UserLogin.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(UserLogin.this);
        }
        return result;
    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(UserLogin.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.println(Log.ASSERT, "UserLogin", "Log in");

                            databaseCollection();

                            if (mProgressDialog != null)
                                mProgressDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.println(Log.ASSERT, "UserLogin", e.toString());
                    }
                });
    }

    private void databaseCollection() {
        list_key.clear();
        list_name.clear();
        list_pwd.clear();
        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsers(key);
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

    private void readUsers(String key) {
        list_key.add(key);
        db.collection("Users").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String name = documentSnapshot.get("Name").toString();
                String pwd = documentSnapshot.get("Password").toString();

                if (!name.equals(null) && !pwd.equals(null)) {
                    list_name.add(name);
                    list_pwd.add(pwd);

                    name = null;
                    pwd = null;
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
                if (task.isSuccessful() && task.getResult().size() > 0) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String key = document.getId();
                        readUsersSuper(key);

                    }
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

    @Override
    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawers();
//        } else {
        super.onBackPressed();
//        }
    }

//    @Override
//    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//        // Handle item selection
//        switch (menuItem.getItemId()) {
//            case R.id.action_login:
//                Intent loginIntent = new Intent(UserLogin.this, UserLogin.class);
//                startActivity(loginIntent);
//                break;
//            case R.id.action_admin:
//                Intent adminIntent = new Intent(UserLogin.this, Admin.class);
//                adminIntent.putExtra("status", "Admin");
//                startActivity(adminIntent);
//                break;
//            case R.id.super_admin:
//                Intent superAdminIntent = new Intent(UserLogin.this, Admin.class);
//                superAdminIntent.putExtra("status", "Super Admin");
//                startActivity(superAdminIntent);
//                break;
//        }
//        return false;
//    }

//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//        toggle.syncState();
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.menu_list, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                Intent loginIntent = new Intent(UserLogin.this, UserLogin.class);
                startActivity(loginIntent);
                return true;
            case R.id.action_admin:
                Intent adminIntent = new Intent(UserLogin.this, Admin.class);
                adminIntent.putExtra("status", "Admin");
                startActivity(adminIntent);
                return true;
            case R.id.super_admin:
                Intent superAdminIntent = new Intent(UserLogin.this, Admin.class);
                superAdminIntent.putExtra("status", "Super Admin");
                startActivity(superAdminIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void requestCameraPermission() {
        Permissions.check(UserLogin.this, Manifest.permission.CAMERA, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                cameraPermission = true;
                Log.println(Log.ASSERT, "Camera_Permission", "true");
                requestReadStoragePermission();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(UserLogin.this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
                requestWriteStoragePermission();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                new AlertDialog.Builder(UserLogin.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {

                new AlertDialog.Builder(UserLogin.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });


    }

    private void requestReadStoragePermission() {
        Permissions.check(UserLogin.this, Manifest.permission.READ_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Log.println(Log.ASSERT, "READ_EXTERNAL_STORAGE", "true");
                readStoragePermission = true;
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(UserLogin.this, "Read permission is required.", Toast.LENGTH_SHORT).show();
                requestCameraPermission();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                new AlertDialog.Builder(UserLogin.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {

                new AlertDialog.Builder(UserLogin.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });
    }

    private void requestWriteStoragePermission() {
        Log.println(Log.ASSERT, "Write", "Permission");
        Permissions.check(UserLogin.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                Log.println(Log.ASSERT, "WRITE_EXTERNAL_STORAGE", "true");
                writeStoragePermission = true;
                readStoragePermission = true;
                requestCameraPermission();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
                Toast.makeText(UserLogin.this, "Write permission is required.", Toast.LENGTH_SHORT).show();
                requestCameraPermission();
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                new AlertDialog.Builder(UserLogin.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return super.onBlocked(context, blockedList);
            }

            @Override
            public void onJustBlocked(Context context, ArrayList<String> justBlockedList, ArrayList<String> deniedPermissions) {

                new AlertDialog.Builder(UserLogin.this)
                        .setTitle("Permission Denied")
                        .setMessage("Permission is denied permanently, go to Settings > App > " + getResources().getString(R.string.app_name) + " > Permission. Allow Camera and Storage Permission.")
                        .setPositiveButton(android.R.string.ok, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

                super.onJustBlocked(context, justBlockedList, deniedPermissions);
            }
        });
    }
}
