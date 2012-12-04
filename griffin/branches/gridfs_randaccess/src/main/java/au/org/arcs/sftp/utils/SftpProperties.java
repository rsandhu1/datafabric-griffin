package au.org.arcs.sftp.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * SFTP Properties helper class
 * 
 * @author John Curtis
 */
public class SftpProperties extends Properties
{
    /** Default text separator. */
    private static final String  DEFAULT_SEPARATOR                 = ",";
   /**
	 * 
	 */
	private static final long	serialVersionUID	= -5649185708131391281L;
	
	private String	seperator=DEFAULT_SEPARATOR;
	
	public SftpProperties()
	{
		super();
	}

	//NB. For normal Properties the Properties param sets the defaults. 
	//We don't want this behaviour
	public SftpProperties(Properties rhs)
	{
		super();
		super.putAll(rhs);
	}

	public String getSeperator()
	{
		return seperator;
	}
	
	public void setSeperator(String seperator)
	{
		assert seperator!=null && !seperator.isEmpty():"Bad seperator";
		this.seperator = seperator;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInt(String optionName, int defaultValue)throws NumberFormatException 
	{
		int result = defaultValue;
		String intStr = getProperty(optionName);
		if (intStr != null && intStr.length() > 0)
		{
			result = Integer.parseInt(intStr);
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setInt(String optionName, int value)
	{
		put(optionName,Integer.toString(value));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String optionName, boolean defaultValue)
	{
		boolean result;
		String boolStr = getProperty(optionName);
		if ("true".equalsIgnoreCase(boolStr) || "false".equalsIgnoreCase(boolStr))
		{
			result = Boolean.valueOf(boolStr).booleanValue();
		}
		else
		{
			result = defaultValue;
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setBoolean(String optionName, boolean value)
	{
		put(optionName,value ? "true" : "false");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getString(String optionName, String defaultValue)
	{
		String result = getProperty(optionName);
		if (result == null || result.length() == 0)
		{
			result = defaultValue;
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getStringArray(String optionName, String[] defaultValues,String seperator)
	{
		String[] result = defaultValues;
		String strList = getProperty(optionName);
		if (strList != null && strList.length() > 0)
		{
			result = strList.split(seperator);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getStringArray(String optionName, String[] defaultValues)
	{
		return getStringArray(optionName,defaultValues,seperator);
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getIntArray(String optionName, int[] defaultValues,String seperator)throws NumberFormatException 
	{
		int[] result = defaultValues;
		String strList = getProperty(optionName);
		if (strList != null && strList.length() > 0)
		{
			String[] elems = strList.split(seperator);
			result = new int[elems.length];
			for (int i = 0; i < elems.length; i++)
			{
				result[i] = Integer.parseInt(elems[i].trim());
			}
		}
		return result;
	}
	/**
	 * {@inheritDoc}
	 */
	public int[] getIntArray(String optionName, int[] defaultValues)throws NumberFormatException 
	{
		return getIntArray(optionName,defaultValues,seperator);
	}
	
	/**
	 * {@inheritDoc}
	 * load
	 * @throws IOException 
	 */
	public void loadResource(String resource) throws IOException
	{
		InputStream is = org.apache.commons.io.IOUtils.class.getResourceAsStream(resource);
        super.load(is);
 	}
	
	/**
	 * {@inheritDoc}
	 * logAll logging helper
	 */
	public void logAll(Log log,String label)
	{
		log.info(label+":");
		Set<Object> keyset = keySet();
		for (Object key : keyset)
		{
			String value = getProperty(key.toString());
			log.info(" * " + key + ": " + value);
		}
	}

	
}
