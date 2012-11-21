package au.org.arcs.sftp.config;

import java.io.File;
import java.util.Properties;

import au.org.arcs.sftp.utils.SftpProperties;


/**
 * The SFTP server options stored in the user config.xml file.
 * 
 * @author John Curtis
 */
public class SftpConfigProperties extends SftpProperties
{
	private static final long		serialVersionUID	= -3129823388248157327L;
	/** The key for data buffer size. */
	public static final String		OPT_SERVER_LABEL			= "server.label";
	/** The key for data buffer size. */
	public static final String		OPT_RESOURCES_LOCATION		= "resources.name";
	/** The key for data buffer size. */
	public static final String		OPT_WINDOW_BUFFER_SIZE		= "window.buffer.size";
	/** The key for data buffer size. */
	public static final String		OPT_INTERNAL_FILE_BUFFER_KB	= "internal.file.buffer.kb";
	/** The key for data buffer size. */
	public static final String		OPT_USE_FILE_BUFFER_THREAD	= "use.file.buffer.thread";
	
    /** The key for data maximum idle seconds until the session times out. */
    public static final String   	OPT_MAX_IDLE_SECONDS      = "max.idle.seconds";
    /** The key for the client connection limit. */
    public static final String   	OPT_MAX_USER_CONNECTIONS  = "max.user.connections";
	/* Server option keys */
	/** The key for the default root directory. */
	public static final String		OPT_HOME_DIR				= "sftp.home.dir";
	/** The key for the default log directory. */
	public static final String		OPT_LOG_DIR					= "sftp.root.dir";
	/** The host key file to use. */
	public static final String		OPT_HOSTKEY_FILE			= "sftp.hostkey.file";

	/** The key for the FTP port to be used (22 is default). */
	public static final String		OPT_SFTP_PORT				= "sftp.port";

    /** The key for the black list of ip v4 addresses. */
    public static final String   	OPT_IPV4_BLACK_LIST         = "ipv4.black.list";
    /** The key for the black list of ip v4 addresses. */
    public static final String   	OPT_IPV6_BLACK_LIST         = "ipv6.black.list";
    
    /** The key for the global maximum upload rate in KB/s. */
    public static final String   	OPT_MAX_UPLOAD_RATE       = "max.upload.rate";
    /** The key for the global maximum download rate in KB/s. */
    public static final String   	OPT_MAX_DOWNLOAD_RATE     = "max.download.rate";

	//Default values for properties
	private static final int		DEFAULT_WINDOW_BUFFER_SIZE	= 2*1024*1024;
	private static final int		DEFAULT_INTERNAL_FILE_BUFFER_KB	= 0;
	private static final boolean	DEFAULT_OPT_USE_FILE_BUFFER_THREAD	= false;
	
	//We are using 8080 as it is an outgoing port that is often open
	private static final int		DEFAULT_ISFTP_PORT	= 8080;	//22
	private static final int		DEFAULT_MAX_IDLE_SECONDS= 600;
	private static final int		DEFAULT_MAX_USER_CONNECTIONS= 10;
    
	
	public SftpConfigProperties(Properties rhs)
	{
		super(rhs);		
	}

	/**
	 * {@inheritDoc}
	 */
	public String getServerLabel()
	{
		return getProperty(OPT_SERVER_LABEL);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getResources()
	{
		return getProperty(OPT_RESOURCES_LOCATION);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getWindowBufferSize()
	{
		return getInt(OPT_WINDOW_BUFFER_SIZE, DEFAULT_WINDOW_BUFFER_SIZE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getInternalFileBufferSizeKb()
	{
		int sizeKb=getInt(OPT_INTERNAL_FILE_BUFFER_KB, DEFAULT_INTERNAL_FILE_BUFFER_KB);
		return sizeKb;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public boolean useFileBufferThread()
	{
		return getBoolean(OPT_USE_FILE_BUFFER_THREAD, DEFAULT_OPT_USE_FILE_BUFFER_THREAD);
	}


	/**
	 * {@inheritDoc}
	 */
	public int getSftpPort()
	{
		return getInt(OPT_SFTP_PORT, DEFAULT_ISFTP_PORT);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxIdleSeconds()
	{
		return getInt(OPT_MAX_IDLE_SECONDS, DEFAULT_MAX_IDLE_SECONDS);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxUserConnections()
	{
		return getInt(OPT_MAX_USER_CONNECTIONS, DEFAULT_MAX_USER_CONNECTIONS);
	}
		

	/**
	 * {@inheritDoc}
	 */
	public String getHomeDirectory()
	{
		return getProperty(OPT_HOME_DIR);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLoggingDirectory()
	{
		return getProperty(OPT_LOG_DIR);
	}

	/**
	 * {@inheritDoc}
	 */
	public File getHostKeyFile()
	{
		String key_path=getString(OPT_HOSTKEY_FILE,"");
		if(key_path.isEmpty())
			return null;
		return new File(key_path);
	}
		

	public String getBlackList(boolean isIP6)
	{
		String listKey = isIP6 ? OPT_IPV6_BLACK_LIST:OPT_IPV4_BLACK_LIST;
		return getString(listKey, "");
	}
}
