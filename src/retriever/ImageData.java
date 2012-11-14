package retriever;

import java.awt.Canvas;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import acm.graphics.GCanvas;
import acm.graphics.GImage;

public class ImageData
{
	private GImage image;
	private String filename;
	
	private int quantizedRed[][];
	private int quantizedGreen[][];
	private int quantizedBlue[][];
	
	private HashMap<String, Integer> histogram;
	
	//Required for CFSD
	private ArrayList<ColorFrequency> redHistogram;
	private ArrayList<ColorFrequency> greenHistogram;
	private ArrayList<ColorFrequency> blueHistogram;
	
	private double redSFD;
	private double greenSFD;
	private double blueSFD;
	
	public ImageData(String filename)
	{
		this.filename = filename;
		image = new GImage(filename);
		
		quantizedRed = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		quantizedGreen = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		quantizedBlue = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		
		quantizeColors();		
				
		if(UserVariables.USE_CSFD_ALGORITHM)
		{
			calculateSFD();
		}
		else
		{
			histogram = new HashMap<String, Integer>();
			generateHistogram();
		}
	}
	
	private void generateHistogram()
	{
		int pixelArray[][] = image.getPixelArray();
		for(int i=0; i<pixelArray.length; i++)
		{
			for(int j=0; j<pixelArray[0].length; j++)
			{
				String rgbBin = "" + quantizedRed[i][j] + "," + quantizedGreen[i][j] + "," + quantizedBlue[i][j];
				if(histogram.containsKey(rgbBin))
					histogram.put(rgbBin, histogram.get(rgbBin) + 1);
				else
					histogram.put(rgbBin, 1);
			}
		}
	}

	private void quantizeColors()
	{
		int pixelArray[][] = image.getPixelArray();
		for(int i=0; i<pixelArray.length; i++)
			for(int j=0; j<pixelArray[0].length; j++)
			{
				quantizedRed[i][j] = (int)(GImage.getRed(pixelArray[i][j]) / (float)(256 / UserVariables.getRedBin()));
				quantizedGreen[i][j] = (int)(GImage.getGreen(pixelArray[i][j]) / (float)(256 / UserVariables.getGreenBin()));
				quantizedBlue[i][j] = (int)(GImage.getBlue(pixelArray[i][j]) / (float)(256 / UserVariables.getBlueBin()));
			}		
	}
	
	public GImage getQuantizedImage()
	{
		int newPixelArray[][] = image.getPixelArray().clone();
		
		int pixelArray[][] = image.getPixelArray();
		for(int i=0; i<pixelArray.length; i++)
			for(int j=0; j<pixelArray[0].length; j++)
			{
				newPixelArray[i][j] = GImage.createRGBPixel(quantizedRed[i][j], quantizedGreen[i][j], quantizedBlue[i][j]);
			}
		
		return new GImage(newPixelArray);
	}
	
	public void drawImage(GCanvas canvas, int x, int y)
	{
		canvas.add(image, x, y);
	}
	
	public String getFileName()
	{
		return filename;
	}
	
	public void displayHistogram()
	{
		for(String rgbBin : histogram.keySet())
		{
			System.out.println(rgbBin + "--> " + histogram.get(rgbBin));
		}
	}
	
	public int getValueFromHistogram(String rgbBin)
	{
		if(histogram.containsKey(rgbBin))
			return histogram.get(rgbBin);
		else
			return 0;
	}
	
	public GImage getImage()
	{
		return image;
	}
	
	
	//New Methods for CFSD Method
	
	private void calculateSFD()
	{
		generateHistogramsForCFSD();
		
		redSFD = calculateSFD(redHistogram);
		greenSFD = calculateSFD(greenHistogram);
		blueSFD = calculateSFD(blueHistogram);
		
		//System.out.println("Red = " + redSFD + "\tGreen = " + greenSFD + "\tBlue = " + blueSFD + "\t" + filename);
	}
	
	private double calculateSFD(ArrayList<ColorFrequency> histogram)
	{
		double sfd = 0;
		
		for(int index = 0; index < histogram.size(); index++)
		{			
			double w = 1.0 / (Math.abs(histogram.get(index).getColorValue() - index) + 1.0); 
			double h = histogram.get(index).getColorFrequency();
			
			sfd += w * h;
		}
		
		return sfd;
	}

	private void generateHistogramsForCFSD()
	{
		TreeMap<Integer, Integer> redMap = new TreeMap<Integer, Integer>();
		TreeMap<Integer, Integer> greenMap = new TreeMap<Integer, Integer>();
		TreeMap<Integer, Integer> blueMap = new TreeMap<Integer, Integer>();
		
		int pixelArray[][] = image.getPixelArray();
		for(int i=0; i<pixelArray.length; i++)
		{
			for(int j=0; j<pixelArray[0].length; j++)
			{
				addToHistogram(redMap, quantizedRed[i][j]);
				addToHistogram(greenMap, quantizedGreen[i][j]);
				addToHistogram(blueMap, quantizedBlue[i][j]);				
			}
		}
		
		redHistogram = convertToArrayList(redMap);
		greenHistogram = convertToArrayList(greenMap);
		blueHistogram = convertToArrayList(blueMap);
		
		sortInDescendingOrderOfFrequency(redHistogram);
		sortInDescendingOrderOfFrequency(greenHistogram);
		sortInDescendingOrderOfFrequency(blueHistogram);
	}
	
	private void sortInDescendingOrderOfFrequency(ArrayList<ColorFrequency> histogram)
	{
		Collections.sort(histogram);
	}

	private ArrayList<ColorFrequency> convertToArrayList(TreeMap<Integer, Integer> histogram)
	{
		ArrayList<ColorFrequency> list = new ArrayList<ColorFrequency>();
		Set<?> set = histogram.entrySet();
		Iterator<?> i = set.iterator();

		while(i.hasNext()) 
		{
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
			list.add(new ColorFrequency((Integer)me.getKey(), (Integer)me.getValue()));
		}
		
		return list;
	}

	private void addToHistogram(TreeMap<Integer, Integer> histogram, int color)
	{
		if(histogram.containsKey(color))
			histogram.put(color, histogram.get(color) + 1);
		else
			histogram.put(color, 1);
	}
	
	public double getRedSFD()
	{
		return redSFD;
	}
	
	public double getGreenSFD()
	{
		return greenSFD;
	}
	
	public double getBlueSFD()
	{
		return blueSFD;
	}
}
