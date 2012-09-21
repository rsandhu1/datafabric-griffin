package au.org.arcs.sftp;

import org.apache.mina.core.session.IoSession;
import org.apache.sshd.SshServer;
import org.apache.sshd.server.session.ServerSession;

/**
 * Overrides <code>ServerSession</code> to add a <code>SftpSessionContext</code>
 * 
 * @author John Curtis
 */

public class SftpServerSession
		extends ServerSession
{
	// Our details
	private SftpSessionContext	sftpSessionContext;

	public SftpServerSession(SshServer server, IoSession ioSession) throws Exception
	{
		super(server, ioSession);

	}

	public void setSftpSessionContext(SftpSessionContext sftpSessionContext)
	{
		this.sftpSessionContext = sftpSessionContext;
	}

	public SftpSessionContext getSftpSessionContext()
	{
		return sftpSessionContext;
	}
}
