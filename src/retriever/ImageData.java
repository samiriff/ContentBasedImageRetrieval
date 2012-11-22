package retriever;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
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
		
	private Connection connection;
	private boolean disableDatabase = false;
	
	public ImageData(String filename, boolean disableDB)
	{
		this.filename = filename;
		this.disableDatabase = disableDB;
		
		image = new GImage(filename);
		
		quantizedRed = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		quantizedGreen = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		quantizedBlue = new int[image.getPixelArray().length][image.getPixelArray()[0].length];
		
		quantizeColors();	
				
		if(UserVariables.useCSFD())
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
				//System.out.println(GImage.getRed(pixelArray[i][j]) + "\t" + GImage.getGreen(pixelArray[i][j]) + "\t" + GImage.getBlue(pixelArray[i][j]));
				
				quantizedRed[i][j] = (int)(GImage.getRed(pixelArray[i][j]) / (float)(256 / UserVariables.getRedBin()));
				quantizedGreen[i][j] = (int)(GImage.getGreen(pixelArray[i][j]) / (float)(256 / UserVariables.getGreenBin()));
				quantizedBlue[i][j] = (int)(GImage.getBlue(pixelArray[i][j]) / (float)(256 / UserVariables.getBlueBin()));
				
				//System.out.println(quantizedRed[i][j] + "\t" + quantizedGreen[i][j] + "\t" + quantizedBlue[i][j]);
				if(UserVariables.useCSFD())
					convertToHSV(i, j);
				
				//System.out.println(quantizedRed[i][j] + "\t" + quantizedGreen[i][j] + "\t" + quantizedBlue[i][j]);
				
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
		initDBConnection();
		
		if(retrieveSFDFromDB())
		{
			System.out.println("Retrieved From DB");
			return;
		}
		else
		{			
			generateHistogramsForCFSD();
			
			redSFD = calculateSFD(redHistogram);
			greenSFD = calculateSFD(greenHistogram);
			blueSFD = calculateSFD(blueHistogram);
			
			//System.out.println("Red = " + redSFD + "\tGreen = " + greenSFD + "\tBlue = " + blueSFD + "\t" + filename);
			
			insertSFDinDB();			
		}		
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
	
	private void convertToHSV(int i, int j)
	{
		 double hue = 0, saturation, value;
		 double red = quantizedRed[i][j];
		 double green = quantizedGreen[i][j];
		 double blue = quantizedBlue[i][j];
		
		 red = red * (256.0 / (double)UserVariables.getRedBin()); 
		 green = green * (256.0 / (double)UserVariables.getGreenBin());
		 blue = blue * (256.0 / (double)UserVariables.getBlueBin());
		 double minRGB = Math.min(red, Math.min(green, blue));
		 double maxRGB = Math.max(red, Math.max(green, blue));

		 // Black-gray-white
		 if (minRGB == maxRGB) 
		 {
			 quantizedRed[i][j] = 0;
			 quantizedGreen[i][j] = 0;
			 quantizedBlue[i][j] = (int)(minRGB * 100);
			 return;
		 }

		 // Colors other than black-gray-white:
		 double d = (red == minRGB) ? green - blue : ((blue == minRGB) ? red - green : blue - red);
		 double h = (red == minRGB) ? 3 : ((blue == minRGB) ? 1 : 5);
		 hue = 60 * (h - d / (maxRGB - minRGB));
		 saturation = (maxRGB - minRGB) / maxRGB;
		 value = maxRGB;
		 
		 quantizedRed[i][j] = (int)hue;
		 quantizedGreen[i][j] = (int)(saturation * 100);
		 quantizedBlue[i][j] = (int)(value * 100);
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
	
	
	
	//Required for DB Access
	
	//SET GLOBAL max_allowed_packet=16*1024*1024
	private void initDBConnection()
	{
		if(disableDatabase)
			return;
		
		try
		{
			Properties properties = new Properties();
			properties.put("user", "root");
			properties.put("password", "");
			properties.put("characterEncoding", "ISO-8859-1");
			properties.put("useUnicode", "true");
			
			String url = "jdbc:mysql://localhost:3306/image_cfsd";

			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(url, properties);
		}
		catch(SQLException e)
		{
			System.out.println(e);
		}
		
		System.out.println("DB Connection Established");
	}
	
	private void insertSFDinDB()
	{
		if(disableDatabase)
			return;
		
		try
		{
			String query = "INSERT INTO ImageData(Image_Path, redSFD, greenSFD, blueSFD) " +
							"VALUES(?, ?, ?, ?)";
							
			
			//String query = "INSERT INTO ImageData(Image_Path, redSFD, greenSFD, blueSFD) VALUES(\"abc\", 10, 20, 30)";
					
			PreparedStatement pstmt = connection.prepareStatement(query);			
			pstmt.setString(1, filename);
			pstmt.setDouble(2, redSFD);
			pstmt.setDouble(3, greenSFD);
			pstmt.setDouble(4, blueSFD);
					
			System.out.println("Result = " + pstmt.executeUpdate());			
			pstmt.close();		
			
			//query = "INSERT INTO quantizedValues VALUES(LAST_INSERT_ID(), 16, 16, 16)";
			query = "INSERT INTO quantizedValues VALUES(LAST_INSERT_ID(), ?, ?, ?)";
			pstmt = connection.prepareStatement(query);
			
			pstmt.setInt(1, UserVariables.getRedBin());
			pstmt.setInt(2, UserVariables.getGreenBin());
			pstmt.setInt(3, UserVariables.getBlueBin());
			
			pstmt.executeUpdate();
			pstmt.close();
			
			System.out.println("Inserted into DB");
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private boolean retrieveSFDFromDB()
	{
		if(disableDatabase)
			return false;
		
		try
		{
			String query = "SELECT redSFD, greenSFD, blueSFD " +
							"FROM ImageData " +
							"WHERE Image_ID IN " +
							"(" +
								"SELECT Image_ID " +
								"FROM quantizedvalues " +
								"WHERE qRed = ? AND qGreen = ? AND qBlue = ?" +
							") " +
							"AND Image_Path = ?";
			PreparedStatement pstmt = connection.prepareStatement(query);
			
			pstmt.setInt(1, UserVariables.getRedBin());
			pstmt.setInt(2, UserVariables.getGreenBin());
			pstmt.setInt(3, UserVariables.getBlueBin());
			pstmt.setString(4, filename);			
			
			//System.out.println("Prepared Statment = " + pstmt);
			
			ResultSet rs = pstmt.executeQuery();
			if(rs.first() == false)
				return false;		
			
			redSFD = rs.getDouble(1);
			greenSFD = rs.getDouble(2);
			blueSFD = rs.getDouble(3);			
			
			//System.out.println("Red SFD = " + redSFD + "\tGreen SFD = " + greenSFD + "\tBlue SFD = " + blueSFD);
			return true;
		}
		catch(SQLException e)
		{
			e.printStackTrace();			
			return false;
		}		
	}
}
