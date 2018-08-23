package com.furfel.aruco.aruco;

import android.opengl.GLES20;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraGLSurfaceView;
import org.opencv.android.JavaCamera2View;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.samples.cameracalibration.CalibrationResult;
import org.opencv.samples.cameracalibration.CameraCalibrationActivity;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.OverlappingFileLockException;
import java.util.Vector;

import es.ava.aruco.Marker;

import static org.opencv.core.CvType.CV_8U;
import static org.opencv.core.CvType.CV_8UC4;

public class MarkersActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView camera_view_2;
    private MarkerDetectorHelper markerDetector;
    private CheckBox drawCubes;
    private OverdrawView overdraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markers);

        camera_view_2 = (JavaCameraView) findViewById(R.id.camera_view_2);
        //camera_view_2.setMaxFrameSize(720, 1280);
        camera_view_2.setCvCameraViewListener(this);
        camera_view_2.enableFpsMeter();
        camera_view_2.setVisibility(View.VISIBLE);

        overdraw = findViewById(R.id.overdraw);

        drawCubes = (CheckBox) findViewById(R.id.drawCubes);

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if(markerDetector == null || markerDetector.getWidth()!=width || markerDetector.getHeight()!=height){
            markerDetector = new MarkerDetectorHelper(width, height);
            markerDetector.reloadParams(this);
        }
    }

    @Override
    public void onCameraViewStopped() {
        //if(camera_view_2!=null) camera_view_2.disableView();
        if(camera_view_2!=null) camera_view_2.disableView();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        Vector<Marker> markersVector = markerDetector.detectAndDraw(frame, inputFrame.gray(), drawCubes.isChecked());
        overdraw.setMarkers(markersVector);
        overdraw.placeMarkers(frame, false, markersVector);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overdraw.invalidate();
            }
        });
        return frame;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    camera_view_2.enableView();
                    //camera_view_2.setOnTouchListener(MarkersActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        overdraw.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overdraw.onDestroy();
    }
}
