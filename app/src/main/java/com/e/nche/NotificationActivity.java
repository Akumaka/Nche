package com.e.nche;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
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

public class NotificationActivity extends AppCompatActivity {

    private EditText et_code, et_model, et_name, et_email, et_phone, et_remarks;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String complainId;

    private ImageView iv_download;
    private VideoView vv_download;
    private LinearLayout layout_download;
    private TextView tv_status;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mDialog = new ProgressDialog(NotificationActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
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

        iv_download = findViewById(R.id.imageView_download);
        vv_download = findViewById(R.id.videoView_download);
        layout_download = findViewById(R.id.layout_download);
        tv_status = findViewById(R.id.textView_update_status);

        final MediaController mediaController = new MediaController(this);
        vv_download.setMediaController(mediaController);
        mediaController.setAnchorView(vv_download);

        Intent intent = getIntent();
        complainId = intent.getStringExtra("notification_id");

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

    }

    private Uri downloadUri;
    private void downloadComplain(final String complainId) {

        db.collection("General Complains").document(complainId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        et_code.setText(documentSnapshot.get("Code").toString());
                        et_model.setText(documentSnapshot.get("Model").toString());
                        et_name.setText(documentSnapshot.get("Name").toString());
                        et_email.setText(documentSnapshot.get("Email").toString());
                        et_phone.setText(documentSnapshot.get("Phone").toString());
                        et_remarks.setText(documentSnapshot.get("Remarks").toString());
                        final String attachment = documentSnapshot.get("Attachment").toString();

                        if (attachment.equals("Not Attached")){
                            mDialog.dismiss();
                            tv_status.setVisibility(View.VISIBLE);
                            tv_status.setText(attachment);
                        }else {
                            layout_download.setVisibility(View.VISIBLE);

                            StorageReference refImage = FirebaseStorage.getInstance().getReference().child("Complains/"+ complainId);

                            refImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    downloadUri = uri;

                                    if (attachment.equals("Image")){
                                        iv_download.setVisibility(View.VISIBLE);
                                        Glide.with(NotificationActivity.this).load(downloadUri).into(iv_download);
                                        mDialog.dismiss();
                                    }
                                    if (attachment.equals("Video")){
                                        Log.println(Log.ASSERT, "Video", "Loading");
                                        vv_download.setVisibility(View.VISIBLE);
                                        vv_download.setVideoURI(downloadUri);
                                        vv_download.start();
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.println(Log.ASSERT, "Image", "Error");
                                }
                            });
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(NotificationActivity.this, UserLogin.class);
        intent.putExtra("Notification", complainId);
        startActivity(intent);
    }
}
