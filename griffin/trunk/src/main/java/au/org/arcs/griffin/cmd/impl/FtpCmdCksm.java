package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;

public class FtpCmdCksm extends AbstractFtpCmd {

	@Override
	public void execute() throws FtpCmdException {
		// syntax: CKSM <algorithm> <offset> <length> <path> CRLF
		// If length is -1, the checksum will be calculated through the end of the file
		String[] args=getArguments().split("\\s+");
		if (args.length<4) {
            msgOut(MSG500_ERROR, new Object[]{"You need 4 parameters for this command"});
		}
		if (!args[0].equalsIgnoreCase("MD5")) {
            msgOut(MSG500_ERROR, new Object[]{"Only MD5 is supported"});
		}
		if (!args[1].equalsIgnoreCase("0")&&!args[2].equalsIgnoreCase("-1")) {
            msgOut(MSG500_ERROR, new Object[]{"The current version can only calculate checksum of the whole file"});
		}
		//String path=args[3];
        FileObject file=getCtx().getFileSystemConnection().getFileObject(args[3]);

        if (!file.exists()) {
            msgOut(MSG500_ERROR, new Object[]{"No such file or directory"});
            return;
        }
        StringBuffer sb;
		try {
			sb = new StringBuffer("200 "+file.getCheckSum(args[0]).toUpperCase());
	        out(sb.toString());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            msgOut(MSG500_ERROR, new Object[]{"Only MD5 is supported"});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
            msgOut(MSG500_ERROR, new Object[]{"Error when reading the file"});
		}

	}

	@Override
	public String getHelp() {
		// TODO Auto-generated method stub
		return "Request checksum calculation over a portion or whole file existing on the server";
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
