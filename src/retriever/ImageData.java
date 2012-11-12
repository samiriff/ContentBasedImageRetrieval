package retriever;

import java.awt.Canvas;
import java.util.Arrays;
import java.util.HashMap;

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
	
	public ImageData(String filename)
	{
		this.filename = filename;
		image = new GImage(filename);
		
		quantizedRed = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		quantizedGreen = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		quantizedBlue = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		
		quantizeColors();		
		
		histogram = new HashMap<String, Integer>();
		generateHistogram();
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
				quantizedRed[i][j] = (int)(GImage.getRed(pixelArray[i][j]) / (float)(256 / UserVariables.NUM_RED_BINS));
				quantizedGreen[i][j] = (int)(GImage.getGreen(pixelArray[i][j]) / (float)(256 / UserVariables.NUM_GREEN_BINS));
				quantizedBlue[i][j] = (int)(GImage.getBlue(pixelArray[i][j]) / (float)(256 / UserVariables.NUM_BLUE_BINS));
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
}
