package com.furfel.aruco.aruco;

import android.opengl.GLES20;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraGLSurfaceView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.util.Vector;

import es.ava.aruco.Marker;

import static org.opencv.core.CvType.CV_8UC4;

public class MarkersGLActivity extends AppCompatActivity implements CameraGLSurfaceView.CameraTextureListener {

    private CameraGLSurfaceView camera_view;
    private MarkerDetectorHelper markerDetector;

    private OverdrawView overdraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_markers_gl);

        camera_view = (CameraGLSurfaceView) findViewById(R.id.camera_view);
        camera_view.setMaxCameraPreviewSize(800,600);
        camera_view.setCameraTextureListener(this);
        camera_view.disableView();

        overdraw = findViewById(R.id.overdraw);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        if(markerDetector == null || markerDetector.getWidth()!=width || markerDetector.getHeight()!=height){
            markerDetector = new MarkerDetectorHelper(width, height);
            markerDetector.reloadParams(this);
            camera_view.setMaxCameraPreviewSize(800,600);
        }
    }

    @Override
    public void onCameraViewStopped() {
        if(camera_view!=null) camera_view.disableView();
    }

    @Override
    public boolean onCameraTexture(int texIn, int texOut, int width, int height) {
        if(width<=0 || height<=0) return false;
        ByteBuffer buffer = ByteBuffer.allocate(width*height*4);
        GLES20.glReadPixels(0,0,width,height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        Mat m = new Mat(height, width, CV_8UC4);
        m.put(0,0, buffer.array());
        Vector<Marker> markersVector = markerDetector.detectAndDraw(m, null, false);
        overdraw.setMarkers(markersVector);
        overdraw.placeMarkers(m, true, markersVector);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                overdraw.invalidate();
            }
        });
        byte[] data = new byte[width*height*4];
        m.get(0,0, data);
        buffer = ByteBuffer.wrap(data);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texOut);
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0,0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        return true;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    camera_view.enableView();
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
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
