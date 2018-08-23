package com.furfel.aruco.aruco;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.opencv.samples.cameracalibration.CameraCalibrationActivity;

public class ArucoActivity extends AppCompatActivity {

    public static final int CAMERA_REQUEST = 101;

    private RadioGroup camera_engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aruco);
        camera_engine = findViewById(R.id.camera_engine);
    }

    public void buttonClicked(View v) {
        if(v.getId()==R.id.calibrator) {
            startActivity(new Intent(this, CameraCalibrationActivity.class));
        } else if(v.getId()==R.id.detector) {
            int id=camera_engine.getCheckedRadioButtonId();
            if(id==R.id.engine_java)
                startActivity(new Intent(this, MarkersActivity.class));
            else if(id==R.id.engine_gl)
                startActivity(new Intent(this, MarkersGLActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_REQUEST) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Aruco","Granted");
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
            }
        }
    }
}
