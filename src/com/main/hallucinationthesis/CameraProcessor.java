package com.main.hallucinationthesis;

import java.io.File;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class CameraProcessor {

	
	private static final String TAG = "HallucinationThesis::CameraProcessing";
	private static Context		cContext;
	
	private boolean 	cProcessingEnabled;
	private boolean 	cOnTouch;
	private int			cFrameSizeWidth;
	private int 		cFrameSizeHeight;
	private int			cCount;
	private Point 		cTouch;
	
	// Set flag to enable processing
	public void enableProcessing() {
		cProcessingEnabled = true;
	}

	// Set flag to disable processing
	public void disableProcessing() {
		cProcessingEnabled = false;
	}

	// Set the dimensions of the image from this particular phone
	public void setFrameSize(int width, int height) {
		cFrameSizeWidth = width;
		cFrameSizeHeight = height;
	}

	
	public Mat processImage(Mat inputFrame) {
		if(cProcessingEnabled && cOnTouch) {
			cCount++;
			try {
				
				Log.i(TAG,"Width: "+cFrameSizeWidth+" Height: "+ cFrameSizeHeight);
//				Mat input = inputFrame.submat((int)cTouch.x - (108/2), (int)cTouch.x + (108/2), (int)cTouch.y - (192/2), (int)cTouch.y + (192/2));
//				Log.i(TAG, "Passed submat");
//				Mat lRgba = new Mat(cFrameSizeHeight, cFrameSizeWidth, CvType.CV_8UC4);
//				Log.i(TAG, "Initialised new image with preview size 1920x1080");
//				Size outputSize = new Size(cFrameSizeWidth,cFrameSizeHeight);
//			    Imgproc.resize(input, lRgba, outputSize,2,2,3);
				
				//Mat touchPointMat = subSampleOnTouch(inputFrame);
				
				//Mat outputMat = hallucinate(touchPointMat, 8);
				
			    //Mat saveMat = new Mat();
			    //Imgproc.cvtColor(outputMat, saveMat, Imgproc.COLOR_RGBA2BGR, 3);
			    //saveImage(saveMat,"Hallucination" + cCount);
			} catch(Exception e) {
				Log.d(TAG, e.getLocalizedMessage());
			}
			
		}
		return inputFrame;

	}
	
	private Mat subSampleOnTouch(Mat input) {
		// One-eigth of the original image
		return input.submat((int)cTouch.x - (int)(306/2), (int)cTouch.x + (int)(408/2), (int)cTouch.y - (int)(306/2), (int)cTouch.y + (int)(408/2));
	}
	
	private void saveImage (Mat mRgba, String filename) {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	    File file =  new File(path,filename);
		String fileWrite = file.toString();
		boolean bool = Highgui.imwrite(fileWrite,mRgba);

	    if (bool == true) {
	        Log.d(TAG, "SUCCESS writing image to external storage");
	    }
	    else {
	        Log.d(TAG, "Fail writing image to external storage");
	    }
	}

	public void setContext(Context mContext) {
		cContext = mContext;
	}

	public void touchArea(int x, int y) {
		cTouch = new Point(x,y);
		cOnTouch =  true;
	}
}
