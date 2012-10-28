
package au.org.arcs.sftp.filesystem;

import java.io.IOException;

import org.apache.sshd.common.Session;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;

import au.org.arcs.sftp.SftpServerSession;

/**
 * Griffin file system factory.
 * Provides backend access to the generic Griffin file system.
 * 
 * @author John Curtis
 */
public class GriffinFileSystemFactory implements FileSystemFactory
{
	//Create the appropriate user file system view.
	@Override
	public FileSystemView createFileSystemView(Session session) throws IOException
	{
		GriffinFileSystemView fileSystemView=new GriffinFileSystemView();
		fileSystemView.init((SftpServerSession)session);
		return fileSystemView;
	}
}
