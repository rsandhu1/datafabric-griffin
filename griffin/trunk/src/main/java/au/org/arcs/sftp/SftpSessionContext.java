package au.org.arcs.sftp;

import java.io.IOException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.arcs.griffin.filesystem.FileSystemConnection;
import au.org.arcs.sftp.utils.SftpLog;


/**
 * SFTP to Irods SessionContext
 * This class serves as a means of transportation for data shared by a single
 * SFTP session. Instances of the <code>SftpSessionContext</code> class are
 * passed to each Sub system handler.
 * 
 * @author John Curtis
 */
public class SftpSessionContext implements SftpServerConstants
{
	private final Logger			log		= LoggerFactory.getLogger(getClass());

	private SftpServerDetails		details;
	private String					user;
	private FileSystemConnection	fileSystemConnection;
	private ResourceBundle			resourceBundle;

	/**
	 * Constructor.
	 * 
	 * @param details
	 *            The server details.
	 * @param username
	 *            The username.
	 * @param connection
	 *            The file connection for this session
	 * @param resourceBundle
	 *            The resource bundle that contains messages and texts.
	 */
	public SftpSessionContext(SftpServerDetails details, String username, FileSystemConnection connection,
			ResourceBundle resourceBundle)
	{
		super();
		this.details = details;
		this.user = username;
		this.fileSystemConnection = connection;
		this.resourceBundle = resourceBundle;
	}

	/**
	 * {@inheritDoc}
	 */
	public SftpServerDetails getDetails()
	{
		return details;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUser()
	{
		return user;
	}


	/**
	 * {@inheritDoc}
	 */
	public String getRes(String id)
	{
		return resourceBundle.getString(id);
	}


	public FileSystemConnection getFileSystemConnection()
	{
		return fileSystemConnection;
	}

	
	public void disconnectFileSystem()
	{
		log.debug("disconnectFileSystem");
		if (getFileSystemConnection() != null)
		{
			try
			{
				getFileSystemConnection().close();
			}
			catch (IOException e)
			{
				SftpLog.logError(log,e);
			}
		}
	}
}
