package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.HostPort;
import org.globus.ftp.exception.FTPReplyParseException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.globus.ftp.extended.GridFTPControlChannel;
import org.globus.ftp.vanilla.Command;
import org.globus.ftp.vanilla.Reply;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.DataChannelInfo;
import au.org.arcs.griffin.cmd.RemoteGridFTPControlChannelProvider;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpIllegalProtocolVersion;
import au.org.arcs.griffin.location.GridFTPLocation;
import au.org.arcs.griffin.location.NearestLocationFinder;
import au.org.arcs.griffin.server.impl.ConductorConstants;

/**
 * @author Shunde Zhang
 */
public class FtpCmdPasvConductor extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdPasvConductor.class);
    private NearestLocationFinder locationFinder;
    

    
    public NearestLocationFinder getLocationFinder() {
		return locationFinder;
	}

	public void setLocationFinder(NearestLocationFinder locationFinder) {
		this.locationFinder = locationFinder;
	}

	/**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        try {

        	String remoteIp=getCtx().getClientSocket().getInetAddress().getHostAddress();
            log.debug("remote socket:"+remoteIp);
            
            int mode = getCtx().getTransmissionMode();
            getCtx().closeDataChannels();
            
            RemoteGridFTPControlChannelProvider provider=new RemoteGridFTPControlChannelProvider(remoteIp,locationFinder,getCtx().getServiceContext().getDelegCred());
            provider.setDcau(getCtx().getDCAU());
            provider.setMode(mode);
            DataChannelInfo info=provider.init();
        	getCtx().setDataChannelProvider(provider);

        	/* Send connection parameters */
            out(createResponseMessage(info.getAddress(), info.getPort()));

            /*
             * Connecting the client (ServerSocket.accept()) is deferred until data channel is
             * needed.
             */

        } catch (FtpIllegalProtocolVersion e) {
        	e.printStackTrace();
//            log.error(e.toString());
            msgOut(MSG522);
        } catch (IOException e) {
        	e.printStackTrace();
//            log.error(e.toString());
            msgOut(MSG425);
        } catch (RuntimeException e) {
        	e.printStackTrace();
//            log.error(e.toString());
            msgOut(MSG501);
        } catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            msgOut(MSG501);
		}
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

    /**
     * Returns the preferred protocol version (1=IPv4, 2=IPv6, 0=undefined).
     * 
     * @return The protocol version.
     */
    protected int getPreferredProtocol() {
    	return 0;
    }

    /**
     * Returns the reponse string encoding ip address, port and protocol type. Example: 229 Entering
     * Extended Passive Mode (|||6000|).
     * 
     * @param protocol The protocol index (1=IPv4, 2=IPv6).
     * @param addr The address.
     * @param port The port.
     * @return The string encoding the connection data in an appropriate format.
     */
    protected String createResponseMessage(String ip, int port) {
        StringBuffer addrPort = new StringBuffer();
        String[] ipParts = ip.split("\\.");
        int idx = 0;
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        int p1 = (port >> BYTE_LENGTH) & BYTE_MASK;
        int p2 = port & BYTE_MASK;
        addrPort.append(p1 + SEPARATOR);
        addrPort.append(p2 + "");
        return msg(MSG227, new String[] {addrPort.toString()});
    }

	public String getHelp() {
		// TODO Auto-generated method stub
		return "Passive mode; return ip of the nearest gridftp";
	}

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
