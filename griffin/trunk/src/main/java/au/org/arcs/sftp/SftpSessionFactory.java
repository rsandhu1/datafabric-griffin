package au.org.arcs.sftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private static Log       log                         = LogFactory.getLog(SftpSessionFactory.class);
	@Override
	protected AbstractSession createSession(IoSession ioSession) throws Exception
	{
		log.debug("New session request:"+ioSession);
		return new SftpServerSession(server, ioSession);

	}
}
