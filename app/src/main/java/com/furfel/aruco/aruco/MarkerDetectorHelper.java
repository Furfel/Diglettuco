package com.furfel.aruco.aruco;

import android.app.Activity;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.samples.cameracalibration.CalibrationResult;

import java.util.Vector;

import es.ava.aruco.CameraParameters;
import es.ava.aruco.Marker;
import es.ava.aruco.MarkerDetector;

public class MarkerDetectorHelper {

    private int width = -1, height = -1;
    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();
    private CameraParameters cameraParameters;

    private MarkerDetector detector;

    public int getWidth() {return width;}
    public int getHeight() {return height;}
    public Mat getCamMat() {return mCameraMatrix;}
    public Mat getDistCoeffs() {return mDistortionCoefficients;}

    public MarkerDetectorHelper(int width, int height) {
        this.width = width;
        this.height = height;

        cameraParameters = new CameraParameters();

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);

        copyToCamParams();

        detector = new MarkerDetector();
    }

    private void copyToCamParams() {
        mCameraMatrix.copyTo(cameraParameters.getCameraMatrix());
        mDistortionCoefficients.copyTo(cameraParameters.getDistCoeff());
    }

    public void reloadParams(final Activity activity) {
        if(CalibrationResult.loadFromFile(activity, mCameraMatrix, mDistortionCoefficients)) {
            copyToCamParams();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Loaded calibration from file.", Toast.LENGTH_SHORT).show();
                }
            });
        } else if(CalibrationResult.tryLoad(activity, mCameraMatrix, mDistortionCoefficients)) {
            copyToCamParams();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Loaded calibration from sharedPref", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Camera calibration not loaded. Using defaults.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public Vector<Marker> detect(Mat frame, Mat grayframe) {
        Vector<Marker> found = new Vector<Marker>();
        detector.detect(frame, grayframe, found, cameraParameters, 0.4f);
        return found;
    }

    public Vector<Marker> detectAndDraw(Mat frame, Mat grayframe, boolean cubes) {
        Vector<Marker> found = new Vector<Marker>();
        detector.detect(frame, grayframe, found, cameraParameters, 0.4f);
        for(Marker m : found)
            if(cubes) m.draw3dCube(frame, cameraParameters, new Scalar(50,190,190));
            else m.draw(frame, new Scalar(255, 20, 255), 2, true);
        org.opencv.imgproc.Imgproc.putText(frame, "Markers: "+found.size(), new Point(128,128), Core.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(60,90,220), 2);
        return found;
    }

}
