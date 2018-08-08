package com.example.fzeih.bookshelf;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

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

        // ZXing Barcode Scanner
        mScannerView = new ZXingScannerView(this);
        // Huawei phones need special setting
        if (Build.MANUFACTURER.toUpperCase().equals("HUAWEI")) {
            mScannerView.setAspectTolerance(0.5f);
        }
        mScannerView.startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        setContentView(mScannerView);
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
                    // TODO: handle permission denied
                    // Wunschliste dann nicht anbieten?
                    Toast.makeText(BarcodeScannerActivity.this, "External storage permission denied", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void checkForCameraPermission() {
        if (ContextCompat.checkSelfPermission(BarcodeScannerActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // permission is not granted
            ActivityCompat.requestPermissions(BarcodeScannerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CAMERA);
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
        Intent addByIsbnIntent = new Intent(BarcodeScannerActivity.this, IsbnSearchActivity.class);
        addByIsbnIntent.putExtra(Constants.key_intent_booklistkey, mBookListKey);
        addByIsbnIntent.putExtra(Constants.key_intent_isbn, rawResult.getText());
        startActivity(addByIsbnIntent);
        finish();
    }

    private void readIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        mBookListKey = extras.getString(Constants.key_intent_booklistkey);
    }

}
