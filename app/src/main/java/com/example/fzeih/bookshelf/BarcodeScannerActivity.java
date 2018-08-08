package com.example.fzeih.bookshelf;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class BarcodeScannerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);

        getSupportActionBar().setTitle("Barcode Scanner");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Permissions
        checkForCameraPermission();
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


}
