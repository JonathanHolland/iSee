package com.main.hallucinationthesis;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.*;
import org.opencv.highgui.*;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The main class to perform the processing required for "Hallucination"
 * of a low resolution image into a high resolution one.
 * 
 * Typically, the magnification factor is a power of 2.
 * 
 * @author Jonathan Holland 2015
 * 
 */
public class HallucinationProcessor {
	
	private static final int CV_RGB2GRAY = 7;
	private static final int CV_16S = 3;
	
	private static Context hContext;
	
	private boolean 	hProcessingEnabled;
	private boolean 	hOnTouch;
	private int			hFrameSizeWidth;
	private int 		hFrameSizeHeight;
	
	private Map<String, List<ParentStructure>> library;
	private Map<String, Mat> originalHrImages;
	
	// Set flag to enable processing
	public void enableProcessing() {
		this.hProcessingEnabled = true;
	}

	// Set flag to disable processing
	public void disableProcessing() {
		this.hProcessingEnabled = false;
	}

	// Set the dimensions of the image from this particular phone
	public void setFrameSize(int width, int height) {
		this.hFrameSizeWidth = width;
		this.hFrameSizeHeight = height;
	}
	
	/**
	 * The constructor for the processor,
	 * ideally called on startup of the app.
	 * 
	 * @param path
	 * 				The location path to the high-res image library
	 * @throws NullPointerException
	 * 				If the path parameter is null or empty
	 */
	public HallucinationProcessor(File path) {
		
		if(path==null||path.length()==0) {
			throw new NullPointerException("Path cannot be null or empty");
		}
		
		List<Mat> result = new ArrayList<Mat>();
		
		this.library = deconstructLibrary();	
	}

	/**
	 * @param input
	 * @param scale
	 * @return
	 */
	public Mat hallucinate(Mat input, int scale) {
		if(this.hProcessingEnabled && this.hOnTouch) {
			// before hallucinating, apply a lanczos filter to the image to blow it up
			Mat lanczosInput = lanczos(input,scale);
			// Make the final image as the same size of the lanzcos'd input
			Mat finalImage = new Mat(lanczosInput.height(),lanczosInput.width(), CvType.CV_8UC4); // or same type as well?
			
			Double threshold = Double.MAX_VALUE;
			
			// Deconstruct the given LR image into its parent structures
			List<ParentStructure> inputParentStructures = deconstructImage(input);
			
			// For each ps in the low-res image
			for(int i=0; i < inputParentStructures.size(); i++) {
				ParentStructure currentInputPS = inputParentStructures.get(i);
				ParentStructure matchingHR = null;
				// For each image in the high-res library
				for(Map.Entry<String, List<ParentStructure>> image : this.library.entrySet()) {
					// For each parent structure in the high-res image
					for(int j=0; j<image.getValue().size(); j++) {
						ParentStructure currentHRPS = image.getValue().get(j);
						
						// If the weightedScores are within a threshold, 
						if(Math.abs(currentHRPS.getWeightedScore()-currentInputPS.getWeightedScore())<threshold) {
							// set this parent structure to be the current closest
							// and make threshold this new difference
							threshold = Math.abs(currentHRPS.getWeightedScore()-currentInputPS.getWeightedScore());
							matchingHR = currentHRPS;
						}
						
						// this will converge on the closest possible choice at the end of the image
						if(j==image.getValue().size()-1 && matchingHR !=null) {
							// apply the matchingHR PS
							finalImage.put((int)Math.floor(i/lanczosInput.width()),i%lanczosInput.width(), this.originalHrImages.get(image.getKey()).get((int)matchingHR.getPixelPosition().y, (int)matchingHR.getPixelPosition().x));
						}
					}
				}
				
				
			}
			
			return finalImage;
			
		} else {
			
			return input;
		}
	}
	
	private Mat lanczos(Mat input, int scale) {
		
		Mat output = new Mat(input.height()*scale, input.width()*scale, CvType.CV_8UC4);
		
		Size outputSize = new Size(input.width()*scale,input.height()*scale);
		
	    Imgproc.resize(input, output, outputSize,2,2,3);
	    
		return output;
	}

	/**
	 * A function to decompose the given image into a list of parentStructures;
	 * one for each pixel in the image.
	 * @param input
	 * @return
	 */
	private List<ParentStructure> deconstructImage(Mat input) {
		
		int scale = 1;
		int delta = 0;
		int ddepth = CV_16S;
		
		List<ParentStructure> currentStructures = new ArrayList<ParentStructure>();
		List<Mat> gaussianPyramid = new ArrayList<Mat>();
		List<Mat> laplacianPyramid = new ArrayList<Mat>();
		List<Mat> hFirstDerivativePyramid = new ArrayList<Mat>();
		List<Mat> vFirstDerivativePyramid = new ArrayList<Mat>();
		List<Mat> hSecondDerivativePyramid = new ArrayList<Mat>();
		List<Mat> vSecondDerivativePyramid = new ArrayList<Mat>();
		
		gaussianPyramid = getGaussianPyramid(input, 3);
		
		// Each downsized image is then added
		for(int i=0; i < 3; i++) {
			Mat currentGray = null, currentBlur = null, grad_x = null, 
					grad_y = null, grad2_x = null, grad2_y = null;
			
			Mat gaussian = gaussianPyramid.get(i);
			
			// The laplacian calculated from the gaussian
			laplacianPyramid.add(getLaplacian(gaussian,i));
			
			// Apply a gaussian blur before computing the horizontal and vertical derivatives
			// using either Scharr or Sobel (preferably Scharr)
			Imgproc.GaussianBlur(input, currentBlur, new Size(3,3), 0, 0, Imgproc.BORDER_DEFAULT);
			Imgproc.cvtColor(currentBlur, currentGray, CV_RGB2GRAY);
			/// Gradient X
			Imgproc.Sobel(currentGray, grad_x, ddepth, 1, 0, 3, scale, delta, Imgproc.BORDER_DEFAULT);
			Imgproc.Sobel(grad_x, grad2_x, ddepth, 1, 0, 3, scale, delta, Imgproc.BORDER_DEFAULT);
			/// Gradient Y
			Imgproc.Sobel(currentGray, grad_y, ddepth, 0, 1, 3, scale, delta, Imgproc.BORDER_DEFAULT);
			Imgproc.Sobel(grad_y, grad2_y, ddepth, 0, 1, 3, scale, delta, Imgproc.BORDER_DEFAULT);
			
			Core.convertScaleAbs( grad_x, grad_x);
			Core.convertScaleAbs( grad2_x, grad2_x);
			Core.convertScaleAbs( grad_y, grad_y);
			Core.convertScaleAbs( grad2_y, grad2_y);

			// form the first and second horizontal and vertical derivatives H1,H2,V1,V2 of the gaussian G(I)
			hFirstDerivativePyramid.add(grad_x);
			vFirstDerivativePyramid.add(grad_y);
			hSecondDerivativePyramid.add(grad2_x);
			vSecondDerivativePyramid.add(grad2_y);
		}
		
		// Finally, convert all of this data into usable Parent Structures
		
		// For all height levels
		for(int level = 0; level < 3; level++) {
			// Use the gaussian as a position reference for each height level in the pyramid
			Mat gCurrent = gaussianPyramid.get(level);
			for(int k = 0; k < gCurrent.rows(); k++) {
				for(int l = 0; l < gCurrent.cols(); l++) {
					
					List<double[]> currentFivePoints = extractPoints(laplacianPyramid,hFirstDerivativePyramid,
							hSecondDerivativePyramid,vFirstDerivativePyramid,vSecondDerivativePyramid, new Point(k,l), level);
					List<double[]> parentFivePoints = extractPoints(laplacianPyramid,hFirstDerivativePyramid,
							hSecondDerivativePyramid,vFirstDerivativePyramid,vSecondDerivativePyramid, new Point(k,l), level+1);
					Double[] weightings = new Double[5];
					
					ParentStructure ps = new ParentStructure(currentFivePoints, parentFivePoints, weightings, level, new Point(k,l));
					currentStructures.add(ps);
				}
			}
		}
		return currentStructures;
	}


	/**
	 * A function to decompose the library of high-resolution images
	 * into a mapping from the image name to a list of parentStructure objects.
	 * It should be called at app-startup.
	 * @return
	 */
	private Map<String, List<ParentStructure>> deconstructLibrary() {
		
		int scale = 1;
		int delta = 0;
		int ddepth = CV_16S;
		Map<String, List<ParentStructure>> library = new HashMap<String, List<ParentStructure>>();
		
		// Take all the images from the svg image library of text
		Map<String, Mat> storedImages = new HashMap<String,Mat>();
		storedImages = this.readInImages(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), storedImages);
		this.originalHrImages.putAll(storedImages);
		
		
		// for each image I, form the Gaussian pyramid G0(I) -> GN(I)
		// and the Laplacian pyramid L(I)
		Iterator<Entry<String, Mat>> it = storedImages.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			String imageName = (String) pair.getKey();
			Mat current = (Mat) pair.getValue();
			
			List<ParentStructure> currentStructures = this.deconstructImage(current);
			
			library.put(imageName, currentStructures);
			it.remove();
		}
		
		return library;
	}
	
	/**
	 * A function to extract the 5 parent structure points of the level @level
	 * from the given pyramid structures.
	 * 
	 * @param laplacianPyramid
	 * @param hFirstDerivativePyramid
	 * @param hSecondDerivativePyramid
	 * @param vFirstDerivativePyramid
	 * @param vSecondDerivativePyramid
	 * @param point
	 * @param level
	 * @return
	 */
	private List<double[]> extractPoints(List<Mat> laplacianPyramid,
			List<Mat> hFirstDerivativePyramid,
			List<Mat> hSecondDerivativePyramid,
			List<Mat> vFirstDerivativePyramid,
			List<Mat> vSecondDerivativePyramid, Point point, int level) {
		
		List<double[]> parentPoints = new ArrayList<double[]>();
		
		parentPoints.add(laplacianPyramid.get(level).get((int)point.y,(int)point.x));
		parentPoints.add(hFirstDerivativePyramid.get(level).get((int)point.y,(int)point.x));
		parentPoints.add(hSecondDerivativePyramid.get(level).get((int)point.y,(int)point.x));
		parentPoints.add(vFirstDerivativePyramid.get(level).get((int)point.y,(int)point.x));
		parentPoints.add(vSecondDerivativePyramid.get(level).get((int)point.y,(int)point.x));
		
		
		return parentPoints;
	}

	private List<Mat> getGaussianPyramid(Mat current, int height) {
		List<Mat> gaussianPyramid = new ArrayList<Mat>();
		for(int j=0; j < height; j++) {
			Imgproc.pyrDown(current, current);
			gaussianPyramid.add(current);
		}
		return gaussianPyramid;
	}

	/**
	 * A function to find the laplacian pyramid element at layer l
	 * 
	 * @return 
	 */
	private Mat getLaplacian(Mat img, int l) {
	    Mat currImg = img;
	    int i = 0;
	    Mat lap = null;
	    
	    while(i < l) {
	        Mat down = null, up = null;
	    	Imgproc.pyrDown(currImg, down);
	        Imgproc.pyrUp(down,up);
	        Core.subtract(currImg,up,lap);
	        currImg = down;
	        i++;
	    }
	    return lap;
	}	
	/**
	 * A function to simply read in the high-res images as mats and store them
	 * 
	 * @return
	 */
	private Map<String, Mat> readInImages(File directory, Map<String,Mat> result) {
		
		if(directory.exists()) {
			File[] images = directory.listFiles();
			
			for(int i = 0; i < images.length; i++) {
				File image = images[i];
				if(image.isDirectory()) {
					readInImages(image,result);
				} else {
					result.put(image.getName(),Highgui.imread(image.getAbsolutePath()));
				}
			}
		}
		return result;
	}

	public void setContext(Context context) {
		hContext = context;
	}

	public void touchEvent() {
		this.hOnTouch = true;
	}
}
