package retriever;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ImageCollection
{
	ArrayList<ImageData> images;
	
	public ImageCollection(String path)
	{
		images = new ArrayList<ImageData>();
		
		System.out.println("Initializing Data Structures...");
		initImagesFromFiles(path);
		System.out.println("Completed.");
	}
	
	public void displayImageList()
	{
		for(ImageData image : images)
			System.out.println(image.getFileName());
	}
	
	public ArrayList<ImageData> computeNearestTarget(ImageData queryImage)
	{
		TreeMap<Integer, ImageData> targets = new TreeMap<Integer, ImageData>();
		
		for(ImageData image : images)
		{
			int euclideanDistance = computeEuclideanDistance(image, queryImage);
			//System.out.println(image.getFileName() + "--> " + euclideanDistance);
			targets.put(euclideanDistance, image);
		}
		
		Set<?> set = targets.entrySet();
		Iterator<?> i = set.iterator();
		ArrayList<ImageData> sortedTargets = new ArrayList<ImageData>();
		while(i.hasNext()) 
		{
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
			System.out.print(me.getKey() + ": ");
			System.out.println(((ImageData) (me.getValue())).getFileName());
			sortedTargets.add((ImageData) me.getValue());
		}

		return sortedTargets;
	}	

	private int computeEuclideanDistance(ImageData image, ImageData queryImage)
	{
		int euclideanDistance = 0;
		for(int r = 0; r < UserVariables.getRedBin(); r++)
		{
			for(int g = 0; g < UserVariables.getGreenBin(); g++)
			{
				for(int b = 0; b < UserVariables.getBlueBin(); b++)
				{
					String rgbBin = "" + r + "," + g + "," + b;
					euclideanDistance += Math.abs(image.getValueFromHistogram(rgbBin) - queryImage.getValueFromHistogram(rgbBin));
				}
			}
		}
		
		return euclideanDistance;
	}

	private void initImagesFromFiles(String path)
	{		
		File file = new File(path);
		File[] listOfFolders = file.listFiles(); 
 
		for (int i = 0; i < listOfFolders.length; i++) 
		{ 
			if (listOfFolders[i].isDirectory()) 
			{
				String folder = listOfFolders[i].getName();
				initImageData(folder, 5);
			}
		}
	}

	private void initImageData(String folder, int num)
	{
		String fileName = folder + "/" + folder + "_";
		for(int i = 1; i <= num; i++)
		{
			images.add(new ImageData(fileName + i + ".jpg", false));
		}
	}
	
	
	
	//For CFSD	
	public ArrayList<ImageData> computeNearestCFSDTarget(ImageData queryImage)
	{
		TreeMap<Double, ImageData> targets = new TreeMap<Double, ImageData>();
		
		for(ImageData image : images)
		{
			double sfdDistance = computeSFDDistance(image, queryImage);
			System.out.println(image.getFileName() + "--> " + sfdDistance);
			targets.put(sfdDistance, image);
		}
		
		Set<?> set = targets.entrySet();
		Iterator<?> i = set.iterator();
		ArrayList<ImageData> sortedTargets = new ArrayList<ImageData>();
		while(i.hasNext()) 
		{
			Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
			System.out.print(me.getKey() + ": ");
			//System.out.println(((ImageData) (me.getValue())).getFileName());
			System.out.println("Red = " + ((ImageData) (me.getValue())).getRedSFD() + "\tGreen = " + ((ImageData) (me.getValue())).getGreenSFD() + "\tBlue = " + ((ImageData) (me.getValue())).getBlueSFD() + "\t" + ((ImageData) (me.getValue())).getFileName());
			sortedTargets.add((ImageData) me.getValue());
		}

		return sortedTargets;
	}
	
	private double computeSFDDistance(ImageData image, ImageData queryImage)
	{
		double redSFDDistance = Math.abs(image.getRedSFD() - queryImage.getRedSFD());
		double greenSFDDistance = Math.abs(image.getGreenSFD() - queryImage.getGreenSFD());
		double blueSFDDistance = Math.abs(image.getBlueSFD() - queryImage.getBlueSFD());
		
		return (redSFDDistance + greenSFDDistance + blueSFDDistance) / 3.0;		
	}

}
