package com.main.hallucinationthesis;

import java.io.File;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CameraActivity extends Activity implements CvCameraViewListener, View.OnTouchListener {

	private static final String  	TAG = "HallucinationThesis::CameraActivity";
	private static Context 			cContext;
    private CameraBridgeViewBase 	cOpenCvCameraView;
    private HallucinationProcessor	hProcess;
    private MenuItem             	cImageProcessOn;
    private MenuItem             	cImageProcessOff;
    private Mat						cRgba;
    
    // Provide a callback function to initiate openCV on application load
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    cOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!OpenCVLoader.initDebug()) {
        	Log.i(TAG, "OpenCV failed to initialise");
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "Creating and setting view");
        cOpenCvCameraView = new JavaCameraView(this, -1);
        cOpenCvCameraView.setMaxFrameSize(1920, 1080);
        
        setContentView(cOpenCvCameraView);
        cOpenCvCameraView.setCvCameraViewListener(this);
        
        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p = null;
		try {
			p = m.getPackageInfo(s, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        s = p.applicationInfo.dataDir;
        
        File path = new File(s);
        
        hProcess = new HallucinationProcessor(path);
        hProcess.setContext(cContext);
    }
    
	// Default pause function, disables the view, stops processes running
    @Override
    public void onPause()
    {
        super.onPause();
        if (cOpenCvCameraView != null)
            cOpenCvCameraView.disableView();
    }

    // Default resume function, resumes by reloading the openCV library
    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (cOpenCvCameraView != null)
            cOpenCvCameraView.disableView();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu()");
        cImageProcessOn = menu.add("Start Processing");
        cImageProcessOff = menu.add("Stop Processing");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == cImageProcessOn) {
            hProcess.enableProcessing();
        } else if (item == cImageProcessOff) {
            hProcess.disableProcessing();
        }
        return true;
    }

    @Override
	public boolean onTouch(View v, MotionEvent event) {
        return true;
	}
    
    @Override
	public boolean onTouchEvent(MotionEvent event) {
    	
    	int cols = cRgba.cols();
        int rows = cRgba.rows();

        int xOffset = (cOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (cOpenCvCameraView.getHeight() - rows) / 2;
    	
        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;
    	hProcess.touchEvent(x,y);
        return true;
	}
    
    public void onCameraViewStarted(int width, int height) {
        // Get and thus set a supported image capture size
        hProcess.setFrameSize(width, height);
    }

    public void onCameraViewStopped() {
    }


    public Mat onCameraFrame(Mat inputFrame) {
    	cRgba = hProcess.hallucinate(inputFrame,2);
        return cRgba;
    }
	
}