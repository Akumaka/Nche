package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class UpdateComplain extends AppCompatActivity {

    private EditText et_code, et_model, et_name, et_email, et_phone, et_remarks;
    private Button btn_submit, btn_delete;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String complainId;

    private ImageView iv_download;
    private VideoView vv_download;
    private LinearLayout layout_download;
    private TextView tv_status, tv_loginTitle;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_complain);

        mDialog = new ProgressDialog(UpdateComplain.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        mDialog.setTitle("Loading");
        mDialog.setMessage("Please wait ...");
        mDialog.setCancelable(false);
        mDialog.show();

        et_code = findViewById(R.id.editText_complains_code);
        et_model = findViewById(R.id.editText_complains_model);
        et_name = findViewById(R.id.editText_complains_name);
        et_email = findViewById(R.id.editText_complains_email);
        et_phone = findViewById(R.id.editText_complains_phone);
        et_remarks = findViewById(R.id.editText_complains_remarks);

        btn_submit = findViewById(R.id.button_complains_submit);
        btn_delete = findViewById(R.id.button_complains_delete);

        iv_download = findViewById(R.id.imageView_download);
        vv_download = findViewById(R.id.videoView_download);
        layout_download = findViewById(R.id.layout_download);
        tv_status = findViewById(R.id.textView_update_status);
        tv_loginTitle = findViewById(R.id.login_title);

        final MediaController mediaController = new MediaController(this);
        vv_download.setMediaController(mediaController);
        mediaController.setAnchorView(vv_download);

        Intent intent = getIntent();
        complainId = intent.getStringExtra("ComplainId");

        if (getIntent().getStringExtra("Hide").equals("Agent")) {
            tv_loginTitle.setText("Complain");
            btn_submit.setVisibility(View.INVISIBLE);
            btn_delete.setVisibility(View.GONE);
        }

        Log.e("Complain iD", "" + complainId);
        downloadComplain(complainId);

        vv_download.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //vv_download.start();
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        mDialog.dismiss();
                    }
                });
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notValidateInfo()) {
                    Toast.makeText(UpdateComplain.this, "Values required", Toast.LENGTH_LONG).show();
                } else {
                    updateComplain(complainId);
                }
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(UpdateComplain.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                final ProgressDialog progressDialog = new ProgressDialog(UpdateComplain.this);
                                progressDialog.setTitle("Deleting");
                                progressDialog.setMessage("Please wait...");
                                progressDialog.show();

                                if (downloadUri != null) {
                                    StorageReference attachmentRef = FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(downloadUri));

                                    attachmentRef.delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.println(Log.ASSERT, "Complain", "download");
                                                    db.collection("General Complaintss").document(complainId).delete();
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            progressDialog.dismiss();
                                                            finish();
                                                        }
                                                    }, 2000);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                } else {
                                    Log.println(Log.ASSERT, "Complain", "null");
                                    db.collection("General Complaintss").document(complainId).delete();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.dismiss();
                                            finish();
                                        }
                                    }, 2000);

                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
    }

    private void updateComplain(String complainId) {
        final Map<String, Object> note = new HashMap<>();
        note.put("Code", et_code.getText().toString().trim());
        note.put("Model", et_model.getText().toString().trim());
        note.put("Name", et_name.getText().toString().trim());
        note.put("Email", et_email.getText().toString().trim());
        note.put("Phone", et_phone.getText().toString().trim());
        note.put("Remarks", et_remarks.getText().toString().trim());


        db.collection("General Complaints").document(complainId).update(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(UpdateComplain.this, "Complain Updated", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateComplain.this, "Not Updated", Toast.LENGTH_LONG).show();
                        Log.println(Log.ASSERT, "Error in DB", e.toString());
                    }
                });
    }

    private Uri downloadUri;

    private void downloadComplain(final String complainId) {

        String type = "General Complaints";
        if (getIntent().getStringExtra("type") != null)
            type = getIntent().getStringExtra("type");
        Log.e("typeeeee", "" + type);

        if (!type.equals("General Complaints")) {
            findViewById(R.id.textView_code).setVisibility(View.GONE);
            findViewById(R.id.textView_model).setVisibility(View.GONE);
            et_code.setVisibility(View.GONE);
            et_model.setVisibility(View.GONE);
        }

        db.collection(type + "s").document(complainId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        Log.e("......", documentSnapshot.get("Attachment") + "..." + documentSnapshot.get("Code")
                                + "..." + documentSnapshot.get("Email") + "..." + documentSnapshot.get("Model") + "..." + documentSnapshot.get("Name")
                                + "..." + documentSnapshot.get("Phone") + "..." + documentSnapshot.get("Remarks"));

                        if (documentSnapshot != null
                                && documentSnapshot.get("Attachment") != null
                                && documentSnapshot.get("Code") != null
                                && documentSnapshot.get("Email") != null
                                && documentSnapshot.get("Model") != null
                                && documentSnapshot.get("Name") != null
                                && documentSnapshot.get("Phone") != null
                                && documentSnapshot.get("Remarks") != null
                        ) {
                            et_code.setText(documentSnapshot.get("Code").toString());
                            et_model.setText(documentSnapshot.get("Model").toString());
                            et_name.setText(documentSnapshot.get("Name").toString());
                            et_email.setText(documentSnapshot.get("Email").toString());
                            et_phone.setText(documentSnapshot.get("Phone").toString());
                            et_remarks.setText(documentSnapshot.get("Remarks").toString());
                            final String attachment = documentSnapshot.get("Attachment").toString();

                            if (attachment.equals("Not Attached")) {
                                mDialog.dismiss();
                                tv_status.setVisibility(View.VISIBLE);
                                tv_status.setText(attachment);
                            } else {
                                layout_download.setVisibility(View.VISIBLE);

                                StorageReference refImage = FirebaseStorage.getInstance().getReference().child("Complains/" + complainId);

                                refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        downloadUri = uri;

                                        if (attachment.equals("Image")) {
                                            iv_download.setVisibility(View.VISIBLE);
                                            Glide.with(UpdateComplain.this).load(downloadUri).into(iv_download);
                                            mDialog.dismiss();
                                        } else if (attachment.equals("Video")) {
                                            Log.println(Log.ASSERT, "Video", "Loading");
                                            vv_download.setVisibility(View.VISIBLE);
                                            vv_download.setVideoURI(downloadUri);
                                            vv_download.start();
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
                        } else {
                            new AlertDialog.Builder(UpdateComplain.this)
                                    .setTitle("Alert")
                                    .setMessage("An error has occurred, Please try again.")
                                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
//                                    .setNegativeButton(android.R.string.no, null)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                    }
                });
    }

    private boolean notValidateInfo() {
        boolean result = false;

        if (TextUtils.isEmpty(et_code.getText().toString())) {
            result = true;
            et_code.setError("");
        }
        if (TextUtils.isEmpty(et_model.getText().toString())) {
            result = true;
            et_model.setError("");
        }
        if (TextUtils.isEmpty(et_name.getText().toString())) {
            result = true;
            et_name.setError("");
        }
        if (TextUtils.isEmpty(et_email.getText().toString())) {
            result = true;
            et_email.setError("");
        }
        if (TextUtils.isEmpty(et_phone.getText().toString())) {
            result = true;
            et_phone.setError("");
        }
        if (TextUtils.isEmpty(et_remarks.getText().toString())) {
            result = true;
            et_remarks.setError("");
        }

        return result;
    }

    @Override
    public void onBackPressed() {

        finish();

        super.onBackPressed();
    }
}
