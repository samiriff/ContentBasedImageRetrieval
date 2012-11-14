package retriever;

public class UserVariables
{
	private static int NUM_RED_BINS = 48;
	private static int NUM_GREEN_BINS = 48;
	private static int NUM_BLUE_BINS = 48;	
	
	private static boolean changed;
	
	private static int NUM_SIMILAR_IMAGES = 6;
	
	public static boolean USE_CSFD_ALGORITHM = false;
	
	public static boolean hasChanged()
	{
		return changed;
	}
	
	public static void resetChanged()
	{
		changed = false;
	}
	
	public static void setRedBin(int value)
	{
		NUM_RED_BINS = value;
		changed = true;
	}
	
	public static void setGreenBin(int value)
	{
		NUM_GREEN_BINS = value;
		changed = true;
	}
	
	public static void setBlueBin(int value)
	{
		NUM_BLUE_BINS = value;
		changed = true;
	}
	
	public static int getRedBin()
	{
		return NUM_RED_BINS;
	}
	
	public static int getGreenBin()
	{
		return NUM_GREEN_BINS;
	}

	public static int getBlueBin()
	{
		return NUM_BLUE_BINS;
	}
	
	public static int getNumOfSimilarImages()
	{
		return NUM_SIMILAR_IMAGES;
	}

	public static void setNumOfSimilarImages(int num)
	{
		NUM_SIMILAR_IMAGES = num;
	}

}
