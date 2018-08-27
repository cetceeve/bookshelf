package com.example.fzeih.bookshelf.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.fzeih.bookshelf.R;
import com.example.fzeih.bookshelf.adapter.ImageAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WishGalleryActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 1;
    private static final int PERMISSION_REQUEST_CAMERA = 2;

    private GridView mGridviewImages;
    private ArrayList<String> mImagePaths;
    private ImageAdapter mImageAdapter;
    private ImageButton mDeleteImageButton;

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

        mGridviewImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("******************************* clicked on: *********************************");
                System.out.println("*******************************" + view.toString() + "*********************************");
                if (view == mDeleteImageButton) {
                    System.out.println("******************************* start delete: *********************************");
                    deleteImage(mImagePaths.get(position));
                }
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
        mImagePaths.clear();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getImagePaths();
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
            saveImagePath();
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

    private void setImageAdapter() {
        mDeleteImageButton = findViewById(R.id.wish_list_delete_image);
        mGridviewImages = (GridView) findViewById(R.id.gridview_wishgallery);
        mImagePaths = new ArrayList<>();
        mImageAdapter = new ImageAdapter(this, R.layout.view_image, mImagePaths);
        mGridviewImages.setAdapter(mImageAdapter);
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

    private void saveImagePath() {
        String writablePhotoPath = mCurrentPhotoPath + ",";
        appendToFile(writablePhotoPath, this);
    }

    private void getImagePaths() {
        boolean deadImagePathFlag = false;
        String pathString = readFromFile(this);
        String[] imagePaths = pathString.split(",");
        for (String imagePath: imagePaths) {
            if (isExistingImagePath(imagePath)) {
                mImageAdapter.add(imagePath);
            } else {
                deadImagePathFlag = true;
            }
        }

        if (deadImagePathFlag) {
            rewriteImagePathFile();
        }
    }

    private boolean isExistingImagePath(String imagePath) {
        File file = new File(imagePath);
        return file.exists();
    }

    public void rewriteImagePathFile() {
        String pathString = "";
        for (String imagePath: mImagePaths) {
            pathString += imagePath + ",";
        }
        writeToFile(pathString, this);
    }


    private void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter((context.openFileOutput(getString(R.string.file_name_image_paths), Context.MODE_PRIVATE)));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private void appendToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter((context.openFileOutput(getString(R.string.file_name_image_paths), Context.MODE_APPEND)));
            outputStreamWriter.append(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File append failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String res = "";

        try {
            InputStream inputStream = context.openFileInput(getString(R.string.file_name_image_paths));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                res = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return res;
    }

    private void deleteImage(String deleteImagePath) {
        mImageAdapter.remove(deleteImagePath);
        removeFromGallery(deleteImagePath);
        rewriteImagePathFile();
    }

    private void removeFromGallery(String deleteImagePath) {
        MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                Log.e("ExternalStorage", "Scanned " + path + ":");
                Log.e("ExternalStorage", "-> uri=" + uri);
            }
        });
    }
}
