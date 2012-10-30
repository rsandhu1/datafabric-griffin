package au.org.arcs.griffin.filesystem;

import java.io.IOException;


import org.ietf.jgss.GSSCredential;

import au.org.arcs.griffin.exception.FtpConfigException;

/**
 * File system interface 
 * @author Shunde Zhang
 *
 */
public interface FileSystem {
	public String getSeparator();
	public void init() throws IOException;
	public FileSystemConnection createFileSystemConnection(GSSCredential credential) throws FtpConfigException, IOException;
	public FileSystemConnection createFileSystemConnection(String username, String password) throws FtpConfigException, IOException;
	public void exit();
	public FileSystemConnection createFileSystemConnectionWithPublicKey(
			String username, String sshKeyType, String base64KeyString) throws FtpConfigException, IOException;
}
