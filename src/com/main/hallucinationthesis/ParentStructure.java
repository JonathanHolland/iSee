package com.main.hallucinationthesis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Point;

import android.util.Log;

/**
 * A class to represent the PS/parent structure of a point
 * in an image. Represents the same data structure as described
 * mathematically in Baker & Kanade 2002
 * 
 * @author Jonathan Holland 2015
 *
 */
public class ParentStructure {
	
	// The five values for THIS level of the pyramid
	// Note that the values are double arrays (allowing for grayscale OR colour)
	private List<double[]> currentValues;
	
	// A list of vectors of 5 doubles to represent the parent values of THIS level of the pyramid
	private List<double[]> parentValues;
	
	// The overall weightedScore of the PS
	// used as the matching point between images
	private Double weightedScore;
	
	// A 5 number array to describe the weightings of the laplacian,first horizontal derivative, 
	// first vertical derivative, second horizontal derivative, second vertical derivative respectively
	private Double[] weightings;
	
	// The height on the pyramid at which this parentStructure sits
	// It is 0 indexed to allow this value to be used simply as a size muliplier in powers of 2
	// eg. level 1 of the pyramid is half the image size and can be found by using 2^(levelNumber)
	private int pyramidHeight;
	
	// The pixel position on this level - will need to be extrapolated to a pixel at the base layer
	private Point pixelPosition;
	
	/**
	 * The class constructor.
	 * Class variable invariants cannot be null
	 * 
	 * @param values
	 * 				The parent structure data
	 * @param weightings
	 * 				The beginning weightings for the data
	 * @throws NullPointerException
	 *             	if any inputs are null
	 */
	public ParentStructure(List<double[]> current, List<double[]> values, Double[] weightings,
							int height, Point position) {
		if(values==null||current==null||weightings==null||current.contains(null)) {
			throw new NullPointerException("Inputs cannot be null");
		}
		
		this.currentValues = current;
		if(values.contains(null)) {
			this.parentValues = new ArrayList<double[]>();
		} else {
			this.parentValues = values;
		}

		this.pyramidHeight = height;
		this.pixelPosition = position;
		this.weightings = weightings;
		this.weightedScore = calculateWeightedScore();
		
		System.gc();
	}
	
	// Use a weighted L^2 norm
	// Note that the CV8UC4 nature of the laplacian pyramid values
	// requires 4 values for this.currentValues.get(0)
	// All of the others are a single value
	private Double calculateWeightedScore() {
		return this.weightings[0]*(1/(2^this.pyramidHeight))*(this.currentValues.get(0)[0] + this.currentValues.get(0)[1] + this.currentValues.get(0)[2] + this.currentValues.get(0)[3])
				+ this.weightings[1]*(1/(2^this.pyramidHeight))*(this.currentValues.get(1)[0])
				+ this.weightings[2]*(1/(2^this.pyramidHeight))*(this.currentValues.get(2)[0])
				+ this.weightings[3]*(1/(2^this.pyramidHeight))*(this.currentValues.get(3)[0])
				+ this.weightings[4]*(1/(2^this.pyramidHeight))*(this.currentValues.get(4)[0]);
	}

	public Double getWeightedScore() {
		return weightedScore;
	}

	public void setWeightedScore(Double weightedScore) {
		this.weightedScore = weightedScore;
	}

	public List<double[]> getParentValues() {
		return parentValues;
	}

	public void setParentValues(List<double[]> values) {
		this.parentValues = values;
	}

	public Point getPixelPosition() {
		return pixelPosition;
	}

	public void setPixelPosition(Point pixelPosition) {
		this.pixelPosition = pixelPosition;
	}

	public int getPyramidHeight() {
		return pyramidHeight;
	}

	public void setPyramidHeight(int pyramidHeight) {
		this.pyramidHeight = pyramidHeight;
	}

	public Double[] getWeightings() {
		return weightings;
	}

	public void setWeightings(Double[] weightings) {
		this.weightings = weightings;
	}

	public List<double[]> getCurrentValues() {
		return currentValues;
	}

	public void setCurrentValues(List<double[]> currentValues) {
		this.currentValues = currentValues;
	}
}
