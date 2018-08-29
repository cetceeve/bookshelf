package com.example.fzeih.bookshelf.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
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

    private GridView mImageGridview;
    private ArrayList<String> mImagePaths;
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
                if (checkForCameraPermission() && checkForExternalStoragePermission()) {
                    dispatchTakePictureIntent();
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
                    Snackbar.make(mImageGridview, "Bookshelf needs External Storage Permission to safe your Images!", Snackbar.LENGTH_LONG)
                            .setAction("Try Again", new WishGalleryActivity.ExternalStoragePermissionDeniedSnackbarListener()).show();
                }
                break;
            case PERMISSION_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Snackbar.make(mImageGridview, "Bookshelf needs Camera Permission to take Photos!", Snackbar.LENGTH_LONG)
                            .setAction("Try Again", new WishGalleryActivity.CameraPermissionDeniedSnackbarListener()).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            saveImagePath();
            galleryAddPic();
        }
    }

    private boolean checkForExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(WishGalleryActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(WishGalleryActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_EXTERNAL_STORAGE);
            return false;
        }
        return true;
    }

    private boolean checkForCameraPermission() {
        if (ContextCompat.checkSelfPermission(WishGalleryActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(WishGalleryActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            return false;
        }
        return true;
    }

    private void setImageAdapter() {
        mImageGridview = (GridView) findViewById(R.id.gridview_wishgallery);
        mImagePaths = new ArrayList<>();
        mImageAdapter = new ImageAdapter(this, R.layout.view_image, mImagePaths);
        mImageGridview.setAdapter(mImageAdapter);
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
                if (!mImagePaths.contains(imagePath)) {
                    mImageAdapter.add(imagePath);
                }
            } else {
                mImageAdapter.remove(imagePath);
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

    public void removeImageFromGridview(String deleteImagePath, int position) {
        mImageAdapter.remove(deleteImagePath);
        showUndoImageDeletionSnackbar(deleteImagePath, position);
    }

    private void showUndoImageDeletionSnackbar(final String deletedImagePath, int position) {
        Snackbar.make(mImageGridview, R.string.snackbar_wishgallery_undo_image_deletion, Snackbar.LENGTH_LONG)
                .setAction("Undo", new WishGalleryActivity.UndoImageDeletionListener(deletedImagePath, position))
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            deleteImage(deletedImagePath);
                        }
                    }
                }).show();
    }

    private class UndoImageDeletionListener implements View.OnClickListener {
        String mDeletedImagePath;
        int mPosition;

        private UndoImageDeletionListener(String deletedImagePath, int position) {
            mDeletedImagePath = deletedImagePath;
            mPosition = position;
        }

        @Override
        public void onClick(View v) {
            if (mDeletedImagePath != null) {
                mImagePaths.add(mPosition, mDeletedImagePath);
                mImageAdapter.notifyDataSetChanged();
            }
        }
    }

    private void deleteImage(String deleteImagePath) {
        File deleteFile = new File(deleteImagePath);
        if (deleteFile.exists()) {
            if (deleteFile.delete()) {
                removeFromGallery(deleteFile);
                rewriteImagePathFile();
            } else {
                Toast.makeText(WishGalleryActivity.this, "Error: Image could not be deleted!", Toast.LENGTH_LONG).show();
                mImageAdapter.add(deleteImagePath);
            }
        } else {
            Toast.makeText(WishGalleryActivity.this, "Error: No File to delete!", Toast.LENGTH_LONG).show();
        }
    }

    private void removeFromGallery(File deleteFile) {
        // Set up the projection (we only need the ID)
        String[] projection = { MediaStore.Images.Media._ID };

        // Match on the file path
        String selection = MediaStore.Images.Media.DATA + " = ?";
        String[] selectionArgs = new String[] { deleteFile.getAbsolutePath() };

        // Query for the ID of the media matching the file path
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = getContentResolver();
        Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
        if (c.moveToFirst()) {
            // We found the ID. Deleting the item via the content provider will also remove the file
            long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            contentResolver.delete(deleteUri, null, null);
        } else {
            // File not found in media store DB
        }
        c.close();
    }

    private class ExternalStoragePermissionDeniedSnackbarListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            checkForExternalStoragePermission();
        }
    }

    private class CameraPermissionDeniedSnackbarListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            checkForCameraPermission();
        }
    }
}
