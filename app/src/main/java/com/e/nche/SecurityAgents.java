package com.e.nche;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.e.nche.Email.Connection;
import com.e.nche.MyUserPanel.MyComplainModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class SecurityAgents extends AppCompatActivity {

    private EditText et_status, et_digits, et_registrationNumber, et_trackingNumber, et_vehicleType, et_vehicleRoute, et_phoneNumber, et_address;
    private TextView tv_status, login_title, textView_code;
    private LinearLayout layout;
    LinearLayout layout_complain;
    private Button btn_complains, btn_qrScan, btn_search;
    private ImageButton ib_menu;
    private ImageView iv_operator;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<String> list_key = new ArrayList<>();
    List<MyComplainModel> list = new ArrayList<>();
    private boolean connected = false;
    String type;
    private Uri downloadUri;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_agents);

        FirebaseMessaging.getInstance().subscribeToTopic("Notification")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.println(Log.ASSERT, "Security Agent", getString(R.string.Subscribe));
                        }
                    }
                });

        layout_complain = findViewById(R.id.layout_complain);
        et_status = findViewById(R.id.editText_security_status);
        et_digits = findViewById(R.id.editText_security_code);
        et_registrationNumber = findViewById(R.id.editText_registration_number);
        et_trackingNumber = findViewById(R.id.editText_tracking_number);
        et_vehicleType = findViewById(R.id.editText_type);
        et_vehicleRoute = findViewById(R.id.editText_route);
        et_phoneNumber = findViewById(R.id.editText_phone);
        et_address = findViewById(R.id.editText_address);

        tv_status = findViewById(R.id.textView_security_status);
        login_title = findViewById(R.id.login_title);
        textView_code = findViewById(R.id.textView_code);

        layout = findViewById(R.id.layout_content);

        iv_operator = findViewById(R.id.imageView_driverImage);

        btn_complains = findViewById(R.id.button_security_complains);
        btn_qrScan = findViewById(R.id.button_agent_qrScan);
        btn_search = findViewById(R.id.button_security_search);
        ib_menu = findViewById(R.id.imageButton_menu);

        type = getIntent().getStringExtra("type");
        if (getIntent().getStringExtra("type") != null) {
            login_title.setText(type);
            if (type != null && !type.equals("General Complains"))
                textView_code.setText("6 Digits Code");
        } else
            login_title.setText("Admin");

        readComplains();

        if (checkInternet()) {
            loginUser();
            connected = true;
        }

        btn_complains.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    String type = getIntent().getStringExtra("type");
                    Intent intent = new Intent(SecurityAgents.this, AgentComplain.class);
                    if (type != null)
                        intent.putExtra("type", type);
                    startActivity(intent);
                } else {
                    if (checkInternet())
                        connected = true;
                }

            }
        });

        btn_qrScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityAgents.this, QrScan.class);
                intent.putExtra("ShowRegistrationStatus", "SecurityAgents");
                startActivity(intent);
            }
        });

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    if (TextUtils.isEmpty(et_digits.getText().toString())) {
                        et_digits.setError("Required ...");
                    } else {
                        mDialog = new ProgressDialog(SecurityAgents.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                        mDialog.setTitle("Loading");
                        mDialog.setMessage("Please wait ...");
                        mDialog.setCancelable(false);
                        mDialog.show();
                        if (et_digits.getText().toString().matches("\\d+(?:\\.\\d+)?") || !type.equals("General Complains")) {
                            boolean chk = false;
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).getUnique().equals(et_digits.getText().toString())) {
                                    Log.e("///////////", "" + list.get(i).getEmail());
                                    chk = true;

                                    layout_complain.setVisibility(View.VISIBLE);
                                    tv_status.setVisibility(View.VISIBLE);
                                    et_status.setVisibility(View.VISIBLE);

                                    //background and text color
                                    et_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_verified));
                                    et_status.setTextColor(getResources().getColor(R.color.vTextColor));

                                    et_status.setText("Registered");

                                    EditText editText_complains_name = findViewById(R.id.editText_complains_name);
                                    editText_complains_name.setText(list.get(i).getName());
                                    EditText editText_complains_email = findViewById(R.id.editText_complains_email);
                                    editText_complains_email.setText(list.get(i).getEmail());
                                    EditText editText_complains_phone = findViewById(R.id.editText_complains_phone);
                                    editText_complains_phone.setText(list.get(i).getPhone());
                                    EditText editText_complains_remarks = findViewById(R.id.editText_complains_remarks);
                                    editText_complains_remarks.setText(list.get(i).getRemarks());
                                    final ImageView iv_download = findViewById(R.id.imageView_download);
                                    final VideoView vv_download = findViewById(R.id.videoView_download);
                                    final LinearLayout layout_download = findViewById(R.id.layout_download);

                                    final MediaController mediaController = new MediaController(SecurityAgents.this);
                                    vv_download.setMediaController(mediaController);
                                    mediaController.setAnchorView(vv_download);

                                    vv_download.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                        @Override
                                        public void onPrepared(MediaPlayer mp) {
                                            //vv_download.start();
                                            mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                                @Override
                                                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                                    if (mDialog.isShowing())
                                                        mDialog.dismiss();
                                                }
                                            });
                                        }
                                    });

                                    final String attachment = list.get(i).getAttachment();
                                    if (attachment.equals("Not Attached")) {
                                        if (mDialog.isShowing())
                                            mDialog.dismiss();
                                        tv_status.setVisibility(View.VISIBLE);
                                        tv_status.setText(attachment);
                                    } else {
                                        layout_download.setVisibility(View.VISIBLE);

                                        StorageReference refImage = FirebaseStorage.getInstance().getReference().child("Complains/" + list.get(i).getKey());

                                        refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                downloadUri = uri;

                                                if (attachment.equals("Image")) {
                                                    iv_download.setVisibility(View.VISIBLE);
                                                    vv_download.setVisibility(View.GONE);

                                                    Glide.with(SecurityAgents.this).load(downloadUri).into(iv_download);
                                                    if (mDialog.isShowing())
                                                        mDialog.dismiss();
                                                } else if (attachment.equals("Video")) {
                                                    Log.println(Log.ASSERT, "Video", "Loading");
                                                    vv_download.setVisibility(View.VISIBLE);
                                                    iv_download.setVisibility(View.GONE);

                                                    vv_download.setVideoURI(downloadUri);
                                                    vv_download.start();
                                                    if (mDialog.isShowing())
                                                        mDialog.dismiss();
                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.println(Log.ASSERT, "Image", "Error");
                                            }
                                        });
                                    }

                                    TextView textView_model = findViewById(R.id.textView_model);
                                    EditText editText_complains_model = findViewById(R.id.editText_complains_model);
                                    if (type.equals("General Complains")) {
                                        textView_model.setVisibility(View.VISIBLE);
                                        editText_complains_model.setVisibility(View.VISIBLE);
                                        editText_complains_model.setText(list.get(i).getModel());
                                    } else {
                                        textView_model.setVisibility(View.GONE);
                                        editText_complains_model.setVisibility(View.GONE);
                                    }
                                }
                            }
                            if (!chk) {
                                layout_complain.setVisibility(View.GONE);
                                tv_status.setVisibility(View.VISIBLE);
                                et_status.setVisibility(View.VISIBLE);

                                //background and text color
                                et_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
                                et_status.setTextColor(getResources().getColor(R.color.vTextColor));
                                et_status.setText("Not registered");

                                if (mDialog.isShowing())
                                    mDialog.dismiss();
                            }
                        } else if (type.equals("General Complains")) {
                            final ProgressDialog mProgressDialog = new ProgressDialog(SecurityAgents.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                            mProgressDialog.setTitle("Checking in database");
                            mProgressDialog.setMessage("Please wait...");
                            mProgressDialog.setCancelable(false);
                            String uniqueCode = et_digits.getText().toString();

                            if (list_key.contains(uniqueCode)) {
                                readUsers(uniqueCode);
                            } else {
                                layout.setVisibility(View.GONE);
                                tv_status.setVisibility(View.VISIBLE);
                                et_status.setVisibility(View.VISIBLE);

                                //background and text color
                                et_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
                                et_status.setTextColor(getResources().getColor(R.color.vTextColor));

                                et_status.setText("Not registered");
                            }
                        }
                    }
                } else {
                    if (checkInternet()) {
                        loginUser();
                        connected = true;
                    }
                }
            }
        });

        et_digits.addTextChangedListener(new

                                                 TextWatcher() {
                                                     @Override
                                                     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                                     }

                                                     @Override
                                                     public void onTextChanged(CharSequence s, int start, int before, int count) {

                                                     }

                                                     @Override
                                                     public void afterTextChanged(Editable s) {
                                                         if (s.length() <= 0) {
                                                             layout.setVisibility(View.GONE);
                                                             tv_status.setVisibility(View.GONE);
                                                             et_status.setVisibility(View.GONE);
                                                         }
                                                     }
                                                 });

        ib_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(SecurityAgents.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu_logout, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.logout:
                                SharedPreferences sharedPreferences = getSharedPreferences("CodeCoy_Vicab_App", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.remove("CodeCoy_Vicab_Remember_Security_Login");
                                myEdit.apply();

                                Intent logout = new Intent(SecurityAgents.this, Login.class);
                                startActivity(logout);
                                finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });
    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(SecurityAgents.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.println(Log.ASSERT, "PublicWatch", "Log in");

                    uploadCollection();

                    mProgressDialog.dismiss();
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

    private void readUsers(final String key) {
        final ProgressDialog mProgressDialog = new ProgressDialog(SecurityAgents.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Loading Information");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        db.collection("Registered Vehicles").document(key).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String Registration_Number = documentSnapshot.get("Vehicle_Registration_Number").toString();
                String Tracking_Number = documentSnapshot.get("Vehicle_Tracking_Number").toString();
                String Vehicle_Type = documentSnapshot.get("Vehicle_Type").toString();
                String Vehicle_Route = documentSnapshot.get("Vehicle_Route").toString();
                String Driver_Phone_Number = documentSnapshot.get("Driver_Phone_Number").toString();
                String Driver_Residence_Address = documentSnapshot.get("Driver_Residence_Address").toString();
                String Driver_Image = documentSnapshot.get("Driver_Image").toString();

                if (!Registration_Number.equals(null) && !Tracking_Number.equals(null) && !Vehicle_Type.equals(null) && !Vehicle_Route.equals(null)
                        && !Driver_Phone_Number.equals(null) && !Driver_Residence_Address.equals(null) && !Driver_Image.equals(null)) {

                    tv_status.setVisibility(View.VISIBLE);
                    et_status.setVisibility(View.VISIBLE);
                    layout.setVisibility(View.VISIBLE);

                    //background and text color
                    et_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_verified));
                    et_status.setTextColor(getResources().getColor(R.color.vTextColor));

                    et_status.setText("Registered");
                    et_registrationNumber.setText(Registration_Number);
                    et_trackingNumber.setText(Tracking_Number);
                    et_vehicleType.setText(Vehicle_Type);
                    et_vehicleRoute.setText(Vehicle_Route);
                    et_phoneNumber.setText(Driver_Phone_Number);
                    et_address.setText(Driver_Residence_Address);

                    StorageReference refImage = FirebaseStorage.getInstance().getReference().child("driver images/" + key + "/" + key);
                    refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide.with(SecurityAgents.this).load(uri).into(iv_operator);
                            mProgressDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.println(Log.ASSERT, "Image", "Error");
                        }
                    });

                    Registration_Number = null;
                    Tracking_Number = null;
                    Vehicle_Type = null;
                    Vehicle_Route = null;
                    Driver_Phone_Number = null;
                    Driver_Residence_Address = null;
                    Driver_Image = null;
                }
            }
        });
    }

    private void readComplains() {
//        final ProgressDialog mProgressDialog = new ProgressDialog(SecurityAgents.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
//        mProgressDialog.setTitle("Loading Information");
//        mProgressDialog.setMessage("Please wait ...");
//        mProgressDialog.setCancelable(false);
//        mProgressDialog.show();

        db.collection(type).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {

                        String Key = documentSnapshot.getId();
                        String Code = documentSnapshot.get("Code").toString();
                        String UniqueCode = documentSnapshot.get("UniqueCode").toString();
                        String Model = documentSnapshot.get("Model").toString();
                        String Name = documentSnapshot.get("Name").toString();
                        String Email = documentSnapshot.get("Email").toString();
                        String Phone = documentSnapshot.get("Phone").toString();
                        String Remarks = documentSnapshot.get("Remarks").toString();
                        String Attachment = documentSnapshot.get("Attachment").toString();
                        String TimeStamp = documentSnapshot.get("TimeStamp").toString();

                        MyComplainModel model = new MyComplainModel(Key, Code, UniqueCode, Model, Name, Email, Phone, Remarks, Attachment, TimeStamp);
                        list.add(model);

                        Log.e("......Unique", "" + documentSnapshot.getId());
                    }

//                    if (mProgressDialog.isShowing())
//                        mProgressDialog.dismiss();

                    Log.e("TAG", list.toString());
                } else {
                    Log.e("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(SecurityAgents.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(SecurityAgents.this);
        }
        return result;
    }

}
