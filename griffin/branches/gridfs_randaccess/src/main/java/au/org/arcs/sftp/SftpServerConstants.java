package au.org.arcs.sftp;

import au.org.arcs.sftp.SftpConstants;

/**
 * General constants of the application.
 * 
 * @author Shunde Zhang
 * @author John Curtis
 */
public interface SftpServerConstants extends SftpConstants
{

	/* Various constants */
	/**Configuration of the Spring application context. */
	public static final String	DEFAULT_BEAN_RES			= "griffin-ctx.xml";
	public static final String	DEFAULT_LOG_RES				= "log4j.properties";

	public static final String	DEFAULT_BEAN_SFTP_OPTIONS	= "sftpirods-options";
	public static final String	DEFAULT_BEAN_FILE_SYSTEM	= "fileSystem";

	/**
	 * Environment property key that points to the application's home directory.
	 */
	public static final String	SYS_HOME_DIR				= "isftp.home.dir";
	public static final String	SYS_LOG_DIR					= "isftp.log.dir";
		

    /* Statistics */
    /** Uploaded bytes limit. */
    public static final String   STAT_BYTES_UPLOADED       = "Bytes uploaded";

    /** Uploaded file limit. */
    public static final String   STAT_FILES_UPLOADED       = "Files uploaded";

    /** Downloaded bytes limit. */
    public static final String   STAT_BYTES_DOWNLOADED     = "Bytes downloaded";

    /** Downloaded file limit. */
    public static final String   STAT_FILES_DOWNLOADED     = "Files downloaded";

    /** Download rate (KB/s) limit. */
    public static final String   STAT_DOWNLOAD_RATE        = "Download rate";

    /** Upload rate (KB/s) limit. */
    public static final String   STAT_UPLOAD_RATE          = "Upload rate";
 }
