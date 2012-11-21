package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b> Data Channel Authentication </b>
 * 
 * This extension provides a method for specifying the type of 
 * authentication to be performed on FTP data channels. This extension 
 * may only be used when the contRFC 2228 Security extensions. 
 * 
 * @author Shunde Zhang
 *
 */
public class FtpCmdDcau extends AbstractFtpCmd {

	public void execute() throws FtpCmdException {
		String arg=getArguments();
        if(arg.equalsIgnoreCase("N")) {
        	getCtx().setDCAU(DCAU_NONE);
            out("200 data channel authtication switched off");
        }else if(arg.equalsIgnoreCase("A")) {
        	getCtx().setDCAU(DCAU_SELF);
            out("200 data channel authtication is set to self authentication");
        }else if(arg.equalsIgnoreCase("S")) {
//        	getCtx().setDCAU(DCAU_SUBJECT);
            out("202 data channel authtication type subject-name is not implemented");
        }else {
            out("202 data channel authtication type not recognized");
        }
	}

	public String getHelp() {
		// TODO Auto-generated method stub
		return "DCAU type";
	}

	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}

}
