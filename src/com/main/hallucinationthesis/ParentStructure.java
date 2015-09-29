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
	
	// The five values for THIS level of the pyramid
	private List<Double> currentValues;
	
	// A list of vectors of 5 doubles to represent the parent values of THIS level of the pyramid
	private List<Double> parentValues;
	
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
	public ParentStructure(List<Double> current, List<Double> values, Double[] weightings,
							int height, Point position) {
		if(values==null||values.contains(null)||weightings==null) {
			throw new NullPointerException("Inputs cannot be null");
		}
		this.setCurrentValues(current);
		this.parentValues = values;
		this.pyramidHeight = height;
		this.pixelPosition = position;
		this.setWeightings(weightings);
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

	public List<Double> getParentValues() {
		return parentValues;
	}

	public void setParentValues(List<Double> values) {
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

	public List<Double> getCurrentValues() {
		return currentValues;
	}

	public void setCurrentValues(List<Double> currentValues) {
		this.currentValues = currentValues;
	}
}
