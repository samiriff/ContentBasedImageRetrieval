package retriever;

public class ColorFrequency implements  Comparable<ColorFrequency>
{
	private int value;
	private int frequency;
	
	public ColorFrequency(int v, int f)
	{
		value = v;
		frequency = f;
	}
	
	public int getColorValue()
	{
		return value;
	}
	
	public int getColorFrequency()
	{
		return frequency;
	}
	
	public String toString()
	{
		return "V = " + value + ", F = " + frequency;
	}

	@Override
	public int compareTo(ColorFrequency o)
	{
		return o.frequency - this.frequency;
	}
}
