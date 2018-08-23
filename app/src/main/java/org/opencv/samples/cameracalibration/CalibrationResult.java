package org.opencv.samples.cameracalibration;

import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class CalibrationResult {
    private static final String TAG = "CalibrationResult";

    private static final int CAMERA_MATRIX_ROWS = 3;
    private static final int CAMERA_MATRIX_COLS = 3;
    private static final int DISTORTION_COEFFICIENTS_SIZE = 5;

    public static void save(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        cameraMatrix.get(0,  0, cameraMatrixArray);
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                Integer id = i * CAMERA_MATRIX_ROWS + j;
                editor.putFloat(id.toString(), (float)cameraMatrixArray[id]);
            }
        }

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        distortionCoefficients.get(0, 0, distortionCoefficientsArray);
        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (Integer i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            editor.putFloat(i.toString(), (float)distortionCoefficientsArray[i-shift]);
        }

        editor.commit();
        Log.i(TAG, "Saved camera matrix: " + cameraMatrix.dump());
        Log.i(TAG, "Saved distortion coefficients: " + distortionCoefficients.dump());
    }

    public static void saveToFile(Mat cameraMatrix, Mat distortionCoeffs) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aruco");
        dir.mkdirs();
        File calibration = new File(dir,"calibration.bin");
        try {
            DataOutputStream fos = new DataOutputStream(new FileOutputStream(calibration));

            double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
            cameraMatrix.get(0,  0, cameraMatrixArray);
            for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
                for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                    fos.writeFloat((float)cameraMatrixArray[i * CAMERA_MATRIX_ROWS + j]);
                }
            }

            double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
            distortionCoeffs.get(0, 0, distortionCoefficientsArray);
            int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
            for (Integer i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
                fos.writeFloat((float)distortionCoefficientsArray[i-shift]);
            }
            fos.close();
        } catch(IOException e) {
            Log.e("Calibration", e.getMessage());
        }
    }

    public static boolean loadFromFile(Activity activity, Mat cameraMatrix, Mat distortionCoeffs) {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/aruco");
        dir.mkdirs();
        File calibration = new File(dir,"calibration.bin");
        if(!calibration.exists()) {
            Toast.makeText(activity, "No calibration file found.", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "No previous calibration results found in "+calibration.getAbsolutePath());
            return false;
        }

        try {
            DataInputStream fis = new DataInputStream(new FileInputStream(calibration));

            double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
            for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
                for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                    cameraMatrixArray[i * CAMERA_MATRIX_ROWS + j] = fis.readFloat();
                }
            }
            cameraMatrix.put(0, 0, cameraMatrixArray);
            Log.i(TAG, "Loaded camera matrix: " + cameraMatrix.dump());

            double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
            int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
            for (int i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
                distortionCoefficientsArray[i - shift] = fis.readFloat();
            }
            distortionCoeffs.put(0, 0, distortionCoefficientsArray);
            fis.close();
        } catch(IOException e) {
            Log.e("Calibration", e.getMessage());
            return false;
        }

        return true;
    }

    public static boolean tryLoad(Activity activity, Mat cameraMatrix, Mat distortionCoefficients) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        if (sharedPref.getFloat("0", -1) == -1) {
            Toast.makeText(activity, "No previous calibration found", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "No previous calibration results found");
            return false;
        }

        double[] cameraMatrixArray = new double[CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS];
        for (int i = 0; i < CAMERA_MATRIX_ROWS; i++) {
            for (int j = 0; j < CAMERA_MATRIX_COLS; j++) {
                Integer id = i * CAMERA_MATRIX_ROWS + j;
                cameraMatrixArray[id] = sharedPref.getFloat(id.toString(), -1);
            }
        }
        cameraMatrix.put(0, 0, cameraMatrixArray);
        Log.i(TAG, "Loaded camera matrix: " + cameraMatrix.dump());

        double[] distortionCoefficientsArray = new double[DISTORTION_COEFFICIENTS_SIZE];
        int shift = CAMERA_MATRIX_ROWS * CAMERA_MATRIX_COLS;
        for (Integer i = shift; i < DISTORTION_COEFFICIENTS_SIZE + shift; i++) {
            distortionCoefficientsArray[i - shift] = sharedPref.getFloat(i.toString(), -1);
        }
        distortionCoefficients.put(0, 0, distortionCoefficientsArray);
        Log.i(TAG, "Loaded distortion coefficients: " + distortionCoefficients.dump());

        return true;
    }
}
