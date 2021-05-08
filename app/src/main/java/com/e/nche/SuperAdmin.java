package com.e.nche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.e.nche.Email.Connection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class SuperAdmin extends AppCompatActivity {

    private Button btn_user, btn_security, btn_admin, btn_superAdmin, btn_vehicle, btn_complain;

    private ImageButton ib_menu;

    private String status, superAdmin;

    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_super_admin);

        FirebaseMessaging.getInstance().subscribeToTopic("Notification")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.println(Log.ASSERT, "Super Admin", getString(R.string.Subscribe));
                        }
                    }
                });

        btn_user = findViewById(R.id.button_super_admin_user);
        btn_security = findViewById(R.id.button_super_admin_security);
        btn_admin = findViewById(R.id.button_super_admin_admin);
        btn_superAdmin = findViewById(R.id.button_super_admin_super);
        btn_vehicle = findViewById(R.id.button_super_admin_user_information);
        btn_complain = findViewById(R.id.button_super_admin_complains);

        ib_menu = findViewById(R.id.imageButton_menu);

        Intent intent = getIntent();
        status = intent.getStringExtra("MainAdmin");

        if (status.equals("MainSuperAdmin")){
            superAdmin = "MainSuperAdmin";
        }else {
            superAdmin = "SuperAdmin";
        }

        if (checkInternet()){
            connected = true;
        }

        btn_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected){
                    Intent intent = new Intent(SuperAdmin.this, MyUser.class);
                    intent.putExtra("dbUser", "Users");
                    startActivity(intent);
                }else {
                    if (checkInternet()){
                        connected = true;
                    }
                }
            }
        });

        btn_security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected){
                    Intent intent = new Intent(SuperAdmin.this, MyUser.class);
                    intent.putExtra("dbUser", "Security");
                    startActivity(intent);
                }else {
                    if (checkInternet()){
                        connected = true;
                    }
                }
            }
        });

        btn_admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected){
                    Intent intent = new Intent(SuperAdmin.this, MyUser.class);
                    intent.putExtra("dbUser", "Admin");
                    startActivity(intent);
                }else {
                    if (checkInternet()){
                        connected = true;
                    }
                }
            }
        });

        btn_superAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected){
                    Intent intent = new Intent(SuperAdmin.this, MyUser.class);
                    intent.putExtra("dbUser", "SuperAdmin");
                    intent.putExtra("superAdmin", superAdmin);
                    startActivity(intent);
                }else {
                    if (checkInternet()){
                        connected = true;
                    }
                }
            }
        });

        btn_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected){
                    SharedPreferences sharedPreferences = getSharedPreferences("statusAdmin", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putString("status", "SuperAdmin");
                    myEdit.apply();

                    Intent intent = new Intent(SuperAdmin.this, AdminVehicles.class);
                    intent.putExtra("status", "SuperAdmin");
                    startActivity(intent);
                }else {
                    if (checkInternet()){
                        connected = true;
                    }
                }
            }
        });

        btn_complain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected){
                    Intent intent = new Intent(SuperAdmin.this, MyComplain.class);
                    startActivity(intent);
                }else {
                    if (checkInternet()){
                        connected = true;
                    }
                }
            }
        });

        ib_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(SuperAdmin.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_logout, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout:
                                if (superAdmin.equals("MainSuperAdmin")){
                                    SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    myEdit.remove("CodeCoy_Vicab_Remember_Main_Super_Admin_Login");
                                    myEdit.apply();

                                    Intent logout = new Intent(SuperAdmin.this, Admin.class);
                                    logout.putExtra("status", "Admin");
                                    startActivity(logout);
                                    finish();
                                }else {
                                    SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                    myEdit.remove("CodeCoy_Vicab_Remember_Super_Admin_Login");
                                    myEdit.apply();

                                    Intent logout = new Intent(SuperAdmin.this, Admin.class);
                                    logout.putExtra("status", "Admin");
                                    startActivity(logout);
                                    finish();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });

    }


    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(SuperAdmin.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(SuperAdmin.this);
        }
        return result;
    }
}
