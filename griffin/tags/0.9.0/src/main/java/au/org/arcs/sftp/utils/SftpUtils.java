package au.org.arcs.sftp.utils;

/**
 * SFTP general helper class
 * 
 * @author John Curtis
 */
public class SftpUtils
{
	public static int getNextPowerOf2(int i)
	{
		int j = 1;
		while (j < i)
		{
			j <<= 1;
		}
		return j;
	}

}
