package au.org.arcs.griffin.filesystem;

import java.io.IOException;

import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.exception.FtpConfigException;

/**
 * Interface to abstracted file system. 
 * @author Shunde Zhang
 *
 */
public interface FileSystem {
	/**
	 * @return File system impmenentation's file path separator
	 *     (e. g. "/" on UNIX).
	 */
	public String getSeparator();
	
	/**
	 * Initialise access to the file system.
	 * 
	 * @throws IOException In case of failed access.
	 */
	public void init() throws IOException;
	
	/**
	 * Actively connects to file system.
	 * 
	 * @param credential Credentials to authorise access.
	 * @return File system connection handle.
	 * @throws FtpConfigException In case of problems with the configuration
	 *     used to access the file system.
	 * @throws IOException In case of failed access.
	 */
	public FileSystemConnection createFileSystemConnection(GSSCredential credential)
	        throws FtpConfigException, IOException;
	
    /**
     * Actively connects to file system.
     * 
     * @param username User name.
     * @param sshKeyType Type of SSH key used.
     * @param base64KeyString Base64 encoded SSH key.
     * @return File system connection handle.
     * @throws FtpConfigException In case of problems with the configuration
     *     used to access the file system.
     * @throws IOExceptionIn case of failed access.
     */
    public FileSystemConnection createFileSystemConnectionWithPublicKey(
            String username, String sshKeyType, String base64KeyString)
            throws FtpConfigException, IOException;

    /**
	 * Actively connects to file system.
	 * 
	 * @param username User name.
	 * @param password Password.
	 * @return File system connection handle.
	 * @throws FtpConfigException In case of problems with the configuration
     *     used to access the file system.
	 * @throws IOException In case of failed access.
	 */
	public FileSystemConnection createFileSystemConnection(String username, String password)
	        throws FtpConfigException, IOException;
	
	/**
	 * Exit file system, clear states etc., called when Griffin is stopped.
	 */
	public void exit();
}
