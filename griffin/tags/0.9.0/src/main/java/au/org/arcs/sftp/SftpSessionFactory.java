package au.org.arcs.sftp;

import org.apache.mina.core.session.IoSession;
import org.apache.sshd.common.session.AbstractSession;
import org.apache.sshd.server.session.SessionFactory;

/**
  * Overrides <code>SessionFactory</code> to provide our session <code>SftpServerSession</code>
 * 
 * @author John Curtis
 */

public class SftpSessionFactory
		extends SessionFactory
{
	@Override
	protected AbstractSession createSession(IoSession ioSession) throws Exception
	{
		return new SftpServerSession(server, ioSession);

	}
}
