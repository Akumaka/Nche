package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.e.nche.Email.Connection;
import com.e.nche.Email.GMailVerify;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

public class ComplainsActivity extends AppCompatActivity {

    private EditText et_unique, et_code, et_model, et_name, et_email, et_phone, et_remarks;
    private Button btn_addPic, btn_submit;
    private TextView tv_status;

    Uri imageUri;

    private static final int MY_STORAGE_PERMISSION_CODE = 1;
    private static final int PICK_IMAGE = 2;

    private boolean addPic = false;

    private boolean mImage = false, mVideo = false;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ProgressDialog mProgressDialog;

    private boolean connected = false;

    String type;
    String random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complains);

        mProgressDialog = new ProgressDialog(ComplainsActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Uploading");
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);

        type = getIntent().getStringExtra("type");
        random = getRandomUnique();

        et_unique = findViewById(R.id.editText_complains_unique);
        et_code = findViewById(R.id.editText_complains_code);
        et_model = findViewById(R.id.editText_complains_model);
        et_name = findViewById(R.id.editText_complains_name);
        et_email = findViewById(R.id.editText_complains_email);
        et_phone = findViewById(R.id.editText_complains_phone);
        et_remarks = findViewById(R.id.editText_complains_remarks);

        btn_addPic = findViewById(R.id.button_capture_image);
        btn_submit = findViewById(R.id.button_complains_submit);

        tv_status = findViewById(R.id.textView_complains_status);

        if (checkInternet()) {
            loginUser();
            connected = true;
        }

        if (!type.equals("General Complain")) {
            findViewById(R.id.textView_code).setVisibility(View.GONE);
            et_code.setVisibility(View.GONE);
        }

        TextView login_title = findViewById(R.id.login_title);
        login_title.setText(String.format("Send %s", type));

        btn_addPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addPic = true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_STORAGE_PERMISSION_CODE);
                        } else {
                            Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            imageIntent.setType("image/*");

                            Intent imagePickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            imagePickIntent.setType("image/*");

                            Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            imageIntent.setType("video/*");

                            Intent videoPickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            videoPickIntent.setType("video/*");

                            Intent chooserIntent = Intent.createChooser(imageIntent, "Select Image or Video");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{imagePickIntent, videoIntent, videoPickIntent});

                            startActivityForResult(chooserIntent, PICK_IMAGE);
                        }
                    } else {
                        Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        imageIntent.setType("image/*");

                        Intent imagePickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        imagePickIntent.setType("image/*");

                        Intent videoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        imageIntent.setType("video/*");

                        Intent videoPickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        videoPickIntent.setType("video/*");

                        Intent chooserIntent = Intent.createChooser(imageIntent, "Select Image or Video");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{imagePickIntent, videoIntent, videoPickIntent});

                        startActivityForResult(chooserIntent, PICK_IMAGE);
                    }
                } else {
                    Toast.makeText(ComplainsActivity.this, "File already attached", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected) {
                    if (notValidateInfo()) {
                        Toast.makeText(ComplainsActivity.this, "Required", Toast.LENGTH_LONG).show();
                    } else {
                        mProgressDialog.show();
                        uploadComplain();
                    }
                } else {
                    if (checkInternet()) {
                        connected = true;
                        loginUser();
                    }
                }
            }
        });
    }

    private boolean isValidEmailId(String email) {

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    private boolean checkInternet() {
        boolean result = false;
        Connection connection = new Connection();

        if (connection.isConnected(ComplainsActivity.this)) {
            result = true;
        }

        if (result) {
            result = connection.hasInternetAccess(ComplainsActivity.this);
        }
        return result;
    }

    private Uri downloadUrl;

    private void uploadComplain() {
        final String imageId = db.collection("randoms").document().getId();

        if (image) {
            final StorageReference sRef = FirebaseStorage.getInstance().getReference().child("Complains/" + imageId);
            sRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            downloadUrl = taskSnapshot.getUploadSessionUri();

                            Log.e("imageUri", "" + taskSnapshot.getUploadSessionUri());

                            if (downloadUrl != null) {
                                String url = downloadUrl.toString();

                                if (mImage) {
                                    registerComplain("Image", imageId);
                                }
                                if (mVideo) {
                                    registerComplain("Video", imageId);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.println(Log.ASSERT, "Storage", "Error -- " + e.getMessage());
                        }
                    });
        } else {
            String url = "Not Attached";
            registerComplain(url, imageId);
        }

    }

    private void registerComplain(final String url, final String id) {
        DatabaseReference realTimeRef = FirebaseDatabase.getInstance().getReference().child("Notification").child(id);
        Map<String, Object> mNote = new HashMap<>();
        mNote.put("title", "Complain generated");
        mNote.put("name", et_name.getText().toString().trim());
        realTimeRef.updateChildren(mNote).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    finalizeComplain(url, id);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.ASSERT, "real", e.toString());
            }
        });
    }

    private void finalizeComplain(String url, String id) {
        String timeStamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        final Map<String, Object> note = new HashMap<>();
        note.put("Code", et_code.getText().toString().trim());
        note.put("UniqueCode", random);
        note.put("Model", et_model.getText().toString().trim());
        note.put("Name", et_name.getText().toString().trim());
        note.put("Email", et_email.getText().toString().trim());
        note.put("Phone", et_phone.getText().toString().trim());
        note.put("Remarks", et_remarks.getText().toString().trim());
        note.put("Attachment", url);
        note.put("TimeStamp", timeStamp);

        db.collection(type + "s").document(id).set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        new AlertDialog.Builder(ComplainsActivity.this)
                                .setTitle("Complain")
                                .setMessage("Your complain is generated successfully.")
                                .setPositiveButton(android.R.string.ok, null)
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .show();

                        emptyFields();
                        if (!type.equals("General Complain"))
                            verifyEmail(note);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ComplainsActivity.this, "Not generated", Toast.LENGTH_LONG).show();
                        Log.println(Log.ASSERT, "Error in DB", e.toString());
                    }
                });

    }

    private void verifyEmail(final Map<String, Object> note) {
        final ProgressDialog mProgressDialog = new ProgressDialog(ComplainsActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setTitle("Sending Email");
        mProgressDialog.setMessage("Please wait ...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        final String body = "Code: " + random +
                ((note.get("Name") != null) || (note.get("Name") != "") ? "\nName: " + note.get("Name") : "") +
                "\nEmail: " + note.get("Email") +
                ((note.get("Phone") != null) || (note.get("Phone") != "") ? "\nPhone: " + note.get("Phone") : "") +
                "\nRemarks: " + note.get("Remarks") +
                "\nAttachment: " + note.get("Attachment");

        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailVerify gMailVerify = new GMailVerify("bunubunudata@gmail.com", "Admin123@@");

                    gMailVerify.allMessagesCount();
                    if (type.equals("Financial Crimes Complain"))
                        gMailVerify.sendMail(getResources().getString(R.string.app_name) + type,
                                body,
                                "bunubunudata@gmail.com",
                                "n.almushahid@yahoo.com");
                    else
                        gMailVerify.sendMail(getResources().getString(R.string.app_name) + type,
                                body,
                                "bunubunudata@gmail.com",
                                "zurielsoft@gmail.com,bunubunudata@gmail.com");

                    if (gMailVerify.sent) {
                        Log.println(Log.ASSERT, "verifiedLog", "Successful");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                        Log.println(Log.ASSERT, "verifiedLog", "Error");
                    }
                    mProgressDialog.dismiss();

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                    mProgressDialog.dismiss();
                    Log.println(Log.ASSERT, "verifiedLogFinal", "Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        sender.start();
    }

    private void emptyFields() {
        et_code.setText("");
        et_unique.setText("");
        et_model.setText("");
        et_name.setText("");
        et_email.setText("");
        et_phone.setText("");
        et_remarks.setText("");
        image = false;
        tv_status.setText("");
        tv_status.setVisibility(View.GONE);
        mProgressDialog.dismiss();
    }

    private boolean notValidateInfo() {
        boolean result = false;

        if (TextUtils.isEmpty(et_code.getText().toString())) {
            if (type.equals("General Complain")) {
                result = true;
                et_code.setError("");
            }
        }
        if (TextUtils.isEmpty(et_remarks.getText().toString())) {
            result = true;
            et_remarks.setError("");
        }
        if (TextUtils.isEmpty(et_email.getText().toString())) {
            result = true;
            et_email.setError("");
        }

        return result;
    }

    private void loginUser() {
        final ProgressDialog mProgressDialog = new ProgressDialog(ComplainsActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mProgressDialog.setMessage("Please wait...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        String email = "appuser@mydomain.com";
        String pwd = "iRyd3wHs3eph17uvSUj3";
        mAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.println(Log.ASSERT, "Complains", "Log in");

                    mProgressDialog.dismiss();
                }
            }
        });
    }

    private boolean image = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                image = true;
                addPic = true;
                imageUri = data.getData();

                tv_status.setVisibility(View.VISIBLE);

                if (imageUri.toString().contains("image")) {
                    String size = getImageSizeFromUri(ComplainsActivity.this, imageUri);

                    float imageSize = Float.valueOf(size);

                    float sizeInKB = imageSize / 1024;
                    float sizeInMB = sizeInKB / 1024;

                    if (sizeInMB > 10) {
                        tv_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
                        tv_status.setText("Image must be between 10 MB ");
                        image = false;
                    } else {
                        tv_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_verified));
                        tv_status.setText("Image attached");
                        mImage = true;
                    }

                    Log.println(Log.ASSERT, "Image_Size", sizeInMB + " MB");
                } else if (imageUri.toString().contains("video")) {
                    String size = getVideoSizeFromUri(ComplainsActivity.this, imageUri);

                    float videoSize = Float.parseFloat(size);

                    float sizeInKB = videoSize / 1000;
                    float sizeInMB = sizeInKB / 1000;

                    if (sizeInMB > 50) {
                        tv_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_notverified));
                        tv_status.setText("Video must be between 50 MB ");
                        image = false;
                    } else {
                        tv_status.setBackground(getResources().getDrawable(R.drawable.user_login_edittext_bg_verified));
                        tv_status.setText("Video attached");
                        mVideo = true;
                    }

                    Log.println(Log.ASSERT, "Video_Size", sizeInMB + " MB");
                }

            } else {
                Toast.makeText(ComplainsActivity.this, "Attachment Error", Toast.LENGTH_LONG).show();
                image = false;
            }
        }
    }

    private String getImageSizeFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] mediaSize = {MediaStore.Images.Media.SIZE};
            cursor = context.getContentResolver().query(uri, mediaSize, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getVideoSizeFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] mediaSize = {MediaStore.Video.Media.SIZE};
            cursor = context.getContentResolver().query(uri, mediaSize, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getRandomUnique() {
        return String.format("%06d", new Random().nextInt(1000000));
    }
}
