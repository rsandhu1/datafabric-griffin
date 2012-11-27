package au.org.arcs.sftp.config;


import java.io.IOException;

import au.org.arcs.sftp.utils.SftpProperties;


/**
 * The SFTP server application resource options stored in app.properties.
 * 
 * @author John Curtis
 */
public class SftpAppProperties extends SftpProperties
{
 	private static final long	serialVersionUID	= 3981072470639696181L;
	
	private static final String APP_PROPERTIES        = "/app.properties";
	private static final String	APP_OPT_VERSION		= "version";
	private static final String	APP_OPT_TITLE		= "title";
	private static final String	APP_OPT_BUILD_INFO	= "info";

	private String resource;
	
	public SftpAppProperties()
	{
		super();
	}

	/**
	 * {@inheritDoc}
	 * @throws IOException 
	 */
	public void load() throws IOException 
	{
		loadResource(APP_PROPERTIES);
		this.resource=APP_PROPERTIES;
	}

	public String getResource()
	{
		return resource;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAppTitle()
	{
		return getProperty(APP_OPT_TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAppVersion()
	{
		return getProperty(APP_OPT_VERSION);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAppBuildInfo()
	{
		return getProperty(APP_OPT_BUILD_INFO);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getServerIdentification()
	{
		return getAppTitle()+", v"+getAppVersion();
	}
}
