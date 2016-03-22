package utility;

public class PrintTime {
	
	public static void stampTime(String testo, long prima, long dopo)
	{
		System.err.println("tempo per svolgere " + testo + " :" + (dopo-prima));
	}

}
