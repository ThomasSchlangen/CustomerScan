package de.tsc.customerscan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRScanActivity extends AppCompatActivity implements SurfaceHolder.Callback, Detector.Processor<Barcode> {
    CameraSource cam;
    SurfaceView svQRScan;
    ToneGenerator toneG;
    String strBarcode ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);

        svQRScan = findViewById(R.id.svQRScan);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if(!barcodeDetector.isOperational()){
            //txtView.setText("Could not set up the detector!");
            return;
        }

        cam = new CameraSource
                .Builder(getApplicationContext(),barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                //.setRequestedPreviewSize(640, 480)  // TODO: set resolution to a better value
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        svQRScan.getHolder().addCallback(this);

        barcodeDetector.setProcessor(this);
    }

    // Callbacks from surface view svQScan
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(QRScanActivity.this,new String[]{android.Manifest.permission.CAMERA},1001);
                return;
            }
            cam.start(svQRScan.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        cam.stop();
    }

    // Callbacks from barcodeDetector
    @Override
    public void release() {
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();

        if (barcodes.size() != 0) {
            strBarcode = barcodes.valueAt(0).displayValue;
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);

            // try to get back to previous screen
            Intent intent = new Intent();
            intent.putExtra("customer",strBarcode);
            setResult(RESULT_OK,intent);

            this.finish();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

