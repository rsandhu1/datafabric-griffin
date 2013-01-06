package au.org.arcs.sftp;

import java.net.InetSocketAddress;

import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.session.ServerSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements <code>ForwardingFilter</code> to provide blacklist IP checking for sftpirods
 * 
 * @author John Curtis
 */
public class SftpForwardingFilter implements ForwardingFilter
{
	private final Logger		log	= LoggerFactory.getLogger(getClass());
	private SftpServerDetails	server_details;

	public SftpForwardingFilter(SftpServerDetails serverDetails)
	{
		super();
		server_details = serverDetails;
	}

	public boolean canForwardAgent(ServerSession session)
	{
		return true;
	}

	public boolean canForwardX11(ServerSession session)
	{
		return true;
	}

	public boolean canListen(InetSocketAddress address, ServerSession session)
	{
		return true;
	}

	public boolean canConnect(InetSocketAddress address, ServerSession session)
	{
		//Check blacklisted addresses
		log.debug("Client requests connection. IP: " + address.getAddress());
		if(server_details.checkBlackList(address.getAddress()))
			return true;
		log.info("Client with IP address " + address.getAddress().getHostAddress() + " rejected (blacklisted).");
		return false;
	}
}
