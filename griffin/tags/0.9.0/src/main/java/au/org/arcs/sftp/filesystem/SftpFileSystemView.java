package au.org.arcs.sftp.filesystem;

import java.io.IOException;

import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.session.ServerSession;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * Provides overridable init() and close() methods for FileSystemView in the SFTP subsystem
 * Allowing custom setup of the SFTP subsystem and closing of resources in the event of 
 * an unexpected exception.
 * 
 * @author John Curtis
 */
public abstract class SftpFileSystemView implements FileSystemView
{
	private FileViewProperties properties=new FileViewProperties();
	
	public FileViewProperties getProperties()
	{
		return properties;
	}
	
	public void init(ServerSession session)
	{
	}
	public void close() throws IOException
	{
	}
}
