package com.main.hallucinationthesis;

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
import java.util.List;
import java.util.Map;

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
	
	private static Context hContext;
	
	private boolean 	hProcessingEnabled;
	private boolean 	hOnTouch;
	private int			hFrameSizeWidth;
	private int 		hFrameSizeHeight;
	
	private Map<String, List<ParentStructure>> library;
	private List<Mat> hRImages;
	
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
		
		this.hRImages = readInImages(path, result);
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
			
			
			Mat dst = null;
			Double threshold = Double.MAX_VALUE;
			
			// Deconstruct the given LR image into its parent structures
			List<ParentStructure> inputParentStructures = deconstructImage(input);
			
			// For each ps in the low-res image
			for(int i=0; i < inputParentStructures.size(); i++) {
				ParentStructure currentInputPS = inputParentStructures.get(i);
				ParentStructure matchingHR;
				// For each image in the high-res library
				for(Map.Entry<String, List<ParentStructure>> image : this.library.entrySet()) {
					// For each parent structure in the high-res image
					for(int j=0; j<image.getValue().size(); j++) {
						ParentStructure currentHRPS = image.getValue().get(j);
						
						if(Math.abs(currentHRPS.getWeightedScore()-currentInputPS.getWeightedScore())<threshold) {
							threshold = Math.abs(currentHRPS.getWeightedScore()-currentInputPS.getWeightedScore());
							matchingHR = currentHRPS;
						}
						
						if(j==image.getValue().size()-1) {
							// apply the matchingHR PS
							finalImage.put(Math.floor(i/lanczosInput.width()),i%lanczosInput.width(), matchingHR.)
						}
						// If the weightedScores are within a threshold, 
						// set this parent structure to be the current closest
						// and make threshold this new difference
						// this will converge on the closest possible choice
						
					}
				}
				
				
			}
			
			return dst;
			
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
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * A function to decompose the library of high-resolution images
	 * into a mapping from the image name to a list of parentStructure objects.
	 * It should be called at app-startup.
	 * @return
	 */
	private Map<String, List<ParentStructure>> deconstructLibrary() {
		// Take all the images from the svg image library of text
		// for each image I, form the guassian pyramid G0(I) -> GN(I)
		// and then laplacian pyramid L(I)
		// form the first and second horizontal and vertical derivatives H1,H2,V1,V2 of the gaussian G(I)
		
		// Thus for each pixel, we have five stored values
		// the laplacian, and the four derivatives of the gaussian
		
		// Then we have the Parent Structure vector PSl(m,n) of a pixel (m,n) where
		// it consists of the vector for the pixel and the vectors for each parent in the l+1... n
		// from layer L to N
		
		// Recognition involves matching this PSl(m,n)
		
		// equations 25 from the paper
		// PSl(I)(m,n) = (Fl(I)(m,n), Fl+1(I)([m/2],[n/2])......... FN(I)([m/(2^(N-1))],[n/(2^(N-1))]
		// I.e. the parent structure of pixel (m,n) in the level l of the pyramid is the feature of that level
		// and every level above it at the same point (registered by halving the pixel co-ordinates each time)
		
		
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * A function to simply read in the high-res images as mats and store them
	 * 
	 * @return
	 */
	private List<Mat> readInImages(File directory, List<Mat> result) {
		
		if(directory.exists()) {
			File[] images = directory.listFiles();
			
			for(int i = 0; i < images.length; i++) {
				File image = images[i];
				if(image.isDirectory()) {
					readInImages(image,result);
				} else {
					result.add(Highgui.imread(image.getAbsolutePath()));
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
