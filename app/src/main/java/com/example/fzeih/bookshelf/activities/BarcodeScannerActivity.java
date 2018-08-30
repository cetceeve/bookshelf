package com.example.fzeih.bookshelf.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.fzeih.bookshelf.Constants;
import com.example.fzeih.bookshelf.R;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private static final int PERMISSION_REQUEST_CAMERA = 2;

    private ZXingScannerView mScannerView;

    private String mBookListKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        getSupportActionBar().setTitle("Barcode Scanner");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Permissions
        checkForCameraPermission();

        // Intent
        readIntent();

        // Barcode Scanner
        initZXingScannerView();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                    Snackbar.make(mScannerView, "Barcode scanner needs camera permission to work!", Snackbar.LENGTH_LONG)
                            .setAction("Try Again", new PermissionDeniedSnackbarListener()).show();
                }
                break;
        }
    }

    private void checkForCameraPermission() {
        if (ContextCompat.checkSelfPermission(BarcodeScannerActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(BarcodeScannerActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        // start IsbnSearchActivity with input
        Intent isbnSearchIntent = new Intent(BarcodeScannerActivity.this, IsbnSearchActivity.class);
        isbnSearchIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
        isbnSearchIntent.putExtra(Constants.key_intent_isbn, rawResult.getText());
        startActivity(isbnSearchIntent);
        finish();
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mBookListKey = extras.getString(Constants.key_intent_booklistkey);
        } else {
            Toast.makeText(BarcodeScannerActivity.this, "ERROR: Missing BookListKey", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initZXingScannerView() {
        mScannerView = new ZXingScannerView(this);

        // Huawei phones need special setting
        if (Build.MANUFACTURER.toUpperCase().equals("HUAWEI")) {
            mScannerView.setAspectTolerance(0.5f);
        }

        mScannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        setContentView(mScannerView);
    }

    private class PermissionDeniedSnackbarListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            checkForCameraPermission();
        }
    }
}
