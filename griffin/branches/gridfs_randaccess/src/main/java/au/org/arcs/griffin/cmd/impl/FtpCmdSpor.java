package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.AbstractFtpCmdPort;
import au.org.arcs.griffin.cmd.ActiveModeSocketProvider;
import au.org.arcs.griffin.cmd.DataChannelInfo;
import au.org.arcs.griffin.cmd.SocketProvider;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>Striped Data Port (SPOR)</b>
 * <p>
 * This extension is to be used as a complement to the SPAS command to 
 * implement striped third-party transfers. To simplify interaction with 
 * the parallel data transfer extensions, the SPOR MUST only be done on 
 * a control connection when the data is to be retrieved from the file 
 * space served by that control connection for a third-party transfer.  
 * This command MUST always be used in conjunction with the extended block mode.  
 * 
 * @author Shunde Zhang
 */
public class FtpCmdSpor extends AbstractFtpCmdPort {
	private static Log log = LogFactory.getLog(FtpCmdSpor.class);

    public void execute() throws FtpCmdException {
        try {
            String args = getArguments();
            if (args.length() == 0) {
                msgOut(MSG501);
                return;
            }
            // *TODO* need to be fixed later
            if (args.split("\\s+").length > 1){
            	out("501 SPOR doesn't support striping now, please provide one endpoint only.");
            	return;
            }

            int protocolIdx = doReadProtocolIdx(args);
            String addr = doReadIPAddr(args);
            int port = doReadPort(args);
            log.debug("Data Channel Protocol: " + protocolIdx + ", IPAddr: " + addr + ", port: " + port);

            setupDataChannel(protocolIdx, addr, port);

            msgOut(MSG200);
        } catch (IOException e) {
            log.error(e.toString());
            msgOut(MSG500);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            msgOut(MSG501);
        }
    }


    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets port for active transfer.";
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return true;
	}

}
