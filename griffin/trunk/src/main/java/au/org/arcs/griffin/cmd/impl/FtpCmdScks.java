package au.org.arcs.griffin.cmd.impl;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

public class FtpCmdScks extends AbstractFtpCmd {

	@Override
	public void execute() throws FtpCmdException {
		String[] args=getArguments().split("\\s+");
		if (args.length<2) {
            msgOut(MSG500_ERROR, new Object[]{"You need 2 parameters for this command"});
		}
		if (!args[0].equalsIgnoreCase("MD5")) {
            msgOut(MSG500_ERROR, new Object[]{"Only MD5 is supported"});
		}
		getCtx().setChecksum(args[0], args[1]);
		out("200 OK, will remember that");
	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return "This command is sent prior to upload command such as STOR, ESTO, PUT. It is used to convey to the server that the checksum value for the file which is about to be uploaded. At the end of transfer, server will calculate checksum for the received file, and if it does not match, will consider the transfer to have failed.";
	}

	@Override
	public boolean isAuthenticationRequired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}

}
