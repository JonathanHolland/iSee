package com.main.hallucinationthesis;

import java.util.List;

import org.opencv.core.Point;

/**
 * A class to represent the PS/parent structure of a point
 * in an image. Represents the same data structure as described
 * mathematically in Baker & Kanade 2002
 * 
 * @author Jonathan Holland 2015
 *
 */
public class ParentStructure {
	
	// A list of vectors of 5 doubles to represent the actual parent structure
	private List<List<Double>> valuesList;
	
	// The overall weightedScore of the PS
	// used as the matching point between images
	private Double weightedScore;
	
	// A 5 number array to describe the weightings of the laplacian,first horizontal derivative, 
	// first vertical derivative, second horizontal derivative, second vertical derivative respectively
	private List<Double> weightings;
	
	// The height on the pyramid at which this parentStructure sits
	// It is 0 indexed to allow this value to be used simply as a size muliplier in powers of 2
	// eg. level 1 of the pyramid is half the image size and can be found by using 2^(levelNumber)
	private int beginningPyramidHeight;
	
	// The pixel position on this level - will need to be extrapolated to a pixel at the base layer
	private Point pixelPositionAtHeight;
	
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
	public ParentStructure(List<List<Double>> values, List<Double> weightings) {
		if(values==null||values.contains(null)||weightings==null) {
			throw new NullPointerException("Inputs cannot be null");
		}
		this.valuesList = values;
		this.weightings = weightings;
		this.weightedScore = calculateWeightedScore();
	}
	
	private Double calculateWeightedScore() {
		//TODO
		return null;
	}

	public Double getWeightedScore() {
		return weightedScore;
	}

	public void setWeightedScore(Double weightedScore) {
		this.weightedScore = weightedScore;
	}

	public List<List<Double>> getValuesList() {
		return valuesList;
	}

	public void setValuesList(List<List<Double>> valuesList) {
		this.valuesList = valuesList;
	}

	public Point getPixelPositionAtHeight() {
		return pixelPositionAtHeight;
	}

	public void setPixelPositionAtHeight(Point pixelPositionAtHeight) {
		this.pixelPositionAtHeight = pixelPositionAtHeight;
	}

	public int getBeginningPyramidHeight() {
		return beginningPyramidHeight;
	}

	public void setBeginningPyramidHeight(int beginningPyramidHeight) {
		this.beginningPyramidHeight = beginningPyramidHeight;
	}
}
