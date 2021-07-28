package com.apps.cqtest;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.apps.cqtest.adapters.PhotosAdapter;
import com.apps.cqtest.model.DataModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HomeActivity extends AppCompatActivity {

    private Button btnCamera, btnFinish;
    private TextView tvInfo;
    private static final int CAPTURE_IMAGE_REQUEST = 1, EDIT_IMAGE_REQUEST = 2;
    private File photoFile = null;
    Bitmap photo;
    private List<DataModel> list = new ArrayList<>();
    private RecyclerView rvPhotos;
    PhotosAdapter photosAdapter = null;
    //==================Firebase Auth=========
    FirebaseAuth mAuth;
    //==============Firebase Storage=================
    private StorageReference storageReference;
    public String photoFileName = "photo.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //============firebase auth instantiation=========
        mAuth = FirebaseAuth.getInstance();
//===========Firebase storage reference==========
        storageReference = FirebaseStorage.getInstance().getReference(); // please go to above link and setup firebase storage for android
//===================
        setDataToViews();
        //=====recyclerview=========================
        rvPhotos.setHasFixedSize(true);
        rvPhotos.setLayoutManager(new GridLayoutManager(this, 3));
        //================================================
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < list.size(); i++) {
                    uploadImage(CommonMethods.getImageUri(HomeActivity.this, list.get(i).getBmPic()));
                }
            }
        });
    }
    private void setDataToViews() {
        btnCamera = findViewById(R.id.btnCamera);
        btnFinish = findViewById(R.id.btnFinish);
        rvPhotos = findViewById(R.id.rvPhotos);
        tvInfo = findViewById(R.id.tvInfo);
        tvInfo.setVisibility(View.VISIBLE);
    }

    private void captureImage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
//            openCamera();
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(getApplicationContext(), "Read Phone State Permission Granted", Toast.LENGTH_SHORT).show();
            openCamera();
        } else
            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();

    }

    private void openCamera() {

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            photo = (Bitmap) data.getExtras().get("data");

            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to set the  markers and labels?").setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Convert to byte array
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            startActivityForResult(new Intent(HomeActivity.this, FilterCheckActivity.class)
                                    .putExtra("image", byteArray), EDIT_IMAGE_REQUEST);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                    ivPic.setImageBitmap(photo);
                            tvInfo.setVisibility(View.GONE);
                            list.add(new DataModel(photo));
                            if (photosAdapter == null) {
                                photosAdapter = new PhotosAdapter(list, HomeActivity.this);
                                rvPhotos.setAdapter(photosAdapter);
                            } else {
                                photosAdapter.notifyDataSetChanged();
                            }
                            dialog.dismiss();
                        }
                    });
            try {
                builder.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /*else {
            displayMessage(this, "Request cancelled or something went wrong.");
        }*/

        if (requestCode == EDIT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            assert data != null;

            Bitmap bmp = null;
            String filename = data.getStringExtra("editedImageResult");
            try {
                FileInputStream is = this.openFileInput(filename);
                bmp = BitmapFactory.decodeStream(is);
                is.close();
                tvInfo.setVisibility(View.GONE);
                list.add(new DataModel(bmp));
                if (photosAdapter == null) {
                    photosAdapter = new PhotosAdapter(list, HomeActivity.this);
                    rvPhotos.setAdapter(photosAdapter);
                } else {
                    photosAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void displayMessage(HomeActivity homeActivity, String s) {
        Toast.makeText(homeActivity, s, Toast.LENGTH_SHORT).show();
    }


    // UploadImage method
    private void uploadImage(Uri filePath) {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(HomeActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    list.clear();
                                    photosAdapter.notifyDataSetChanged();
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(HomeActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploading.. "
//                                                    +
//                                                    (int) progress + "%"
                                    );
                                }
                            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(getClass().getSimpleName(), "signInAnonymously:FAILURE", exception);
                    }
                });
    }

}
