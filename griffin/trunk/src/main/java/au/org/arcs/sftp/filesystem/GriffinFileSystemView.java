package au.org.arcs.sftp.filesystem;

import java.io.IOException;

import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.session.ServerSession;

import au.org.arcs.sftp.SftpServerSession;
//import au.org.arcs.sftp.filesystem.SftpFileSystemView;

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.FileSystemConnection;

/**
 * <strong>Internal class, do not use directly.</strong>
 * 
 * The SFTP subsystem FileSystemView provider class
 * Implements FileSystemView to provide a Griffin connection
 * 
 * @author John Curtis
 */
public class GriffinFileSystemView extends SftpFileSystemView
{
	FileSystemConnection 	fileSystemConnection;
	SftpServerSession		server_session;

	public FileSystemConnection getFileSystemConnection()
	{
		return fileSystemConnection;
	}

	public void setFileSystemConnection(FileSystemConnection fileSystemConnection)
	{
		this.fileSystemConnection = fileSystemConnection;
	}

	public void init(ServerSession session)
	{
		if(session!=null && session instanceof SftpServerSession)
		{
			server_session=(SftpServerSession)session;
			fileSystemConnection=server_session.getSftpSessionContext().getFileSystemConnection();
			int internal_buffer_bytes=server_session.getSftpSessionContext().getDetails().getInternalFileBufferSize();
			super.getProperties().setInternalBufferSize(internal_buffer_bytes);
			boolean use_buffer_thread=server_session.getSftpSessionContext().getDetails().useFileBufferThread();
			super.getProperties().setUseBufferThread(use_buffer_thread);
		}
	}
	
	public void close() throws IOException
	{
		if(fileSystemConnection!=null)
			fileSystemConnection.close();
	}
	 
	/**
     * Get file object.
     * @param file The path to the file to get
     * @return The {@link SshFile} for the provided path
     */
	@Override
	public SshFile getFile(String path)
	{
		// get actual file object
		FileObject file_obj=getFileSystemConnection().getFileObject(path);
		return new GriffinSshFile(file_obj);
	}

	  /**
     * Get file object.
     * @param baseDir The reference towards which the file should be resolved
     * @param file The path to the file to get
     * @return The {@link SshFile} for the provided path
     */
	@Override
	public SshFile getFile(SshFile baseDir, String file)
	{
		// TODO What is needed here
		return null;
		//return getFile(file);
	}
}
