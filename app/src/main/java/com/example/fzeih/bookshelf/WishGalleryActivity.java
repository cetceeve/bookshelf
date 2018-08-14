package com.example.fzeih.bookshelf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WishGalleryActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;

    private DatabaseReference mPhotoGalleryDatabaseReference;
    private ChildEventListener mChildEventListener;

    private GridView mGridviewPhotos;
    private ArrayList<Uri> mPhotoUris;
    private ImageAdapter mImageAdapter;

    static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Wish Gallery");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Permissions
        checkForCameraPermission();
        checkForExternalStoragePermission();

        //Data
        getDatabaseReference();
        setImageAdapter();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // device does not have a camera
            Toast.makeText(getApplicationContext(), "No camera detected", Toast.LENGTH_SHORT).show();
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachReadDatabaseListener();
        mPhotoUris.clear();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        attachDatabaseReadListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    // TODO: handle permission denied
                    // Wunschliste dann nicht anbieten?
                    Toast.makeText(WishGalleryActivity.this, "External storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    // TODO: handle permission denied
                    // Wunschliste dann nicht anbieten?
                    Toast.makeText(WishGalleryActivity.this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            galleryAddPic();
            firebaseAddPicUri();
        }
    }

    private void checkForExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(WishGalleryActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(WishGalleryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);

        }
    }

    private void checkForCameraPermission() {
        if (ContextCompat.checkSelfPermission(WishGalleryActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(WishGalleryActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    private void getDatabaseReference() {
        mPhotoGalleryDatabaseReference = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getUid()).child(Constants.key_db_reference_photogallery);
    }

    private void setImageAdapter() {
        mGridviewPhotos = (GridView) findViewById(R.id.gridview_wishgallery);
        mPhotoUris = new ArrayList<>();
        mImageAdapter = new ImageAdapter(this, R.layout.view_image, mPhotoUris);
        mGridviewPhotos.setAdapter(mImageAdapter);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri currentPhotoUri = FileProvider.getUriForFile(getApplicationContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }

    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        if (mCurrentPhotoPath == null) {
            return;
        }

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void firebaseAddPicUri() {
        mPhotoGalleryDatabaseReference.push().setValue(mCurrentPhotoPath);
    }

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String photoPath = (String) dataSnapshot.getValue();
                    File file = new File(photoPath);
                    if (file.exists()) {
                        mImageAdapter.add(Uri.parse(photoPath));
                    } else {
                        mPhotoGalleryDatabaseReference.child(dataSnapshot.getKey()).removeValue();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String photoPath = (String) dataSnapshot.getValue();
                    mImageAdapter.remove(Uri.parse(photoPath));
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };
        }
        mPhotoGalleryDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private void detachReadDatabaseListener() {
        if (mChildEventListener != null) {
            mPhotoGalleryDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
