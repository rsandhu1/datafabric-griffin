package au.org.arcs.griffin.cmd.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.AbstractFtpCmdMlsx;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;

/**
 * <b> Listings for Machine Processing </b>
 * <p>
 * The MLST and MLSD commands are intended to standardize the file and
 * directory information returned by the server-FTP process.  These
 * commands differ from the LIST command in that the format of the
 * replies is strictly defined although extensible.
 * 
 * Two commands are defined, MLST and MLSD.  MLST provides data about
 * exactly the object named on its command line, and no others.
 * 
 * @author Shunde Zhang
 *
 */
public class FtpCmdMlst extends AbstractFtpCmdMlsx {
	private static Log log = LogFactory.getLog(FtpCmdMlst.class);
	public void execute() throws FtpCmdException {
		String arg=getArguments();
        String path = getCtx().getRemoteRelDir();
        if (arg!=null&&arg.length()>0) path = getAbsPath(arg);
        FileObject file=getCtx().getFileSystemConnection().getFileObject(path);

        if (!file.exists()) {
            msgOut(MSG500_ERROR, new Object[]{"No such file or directory"});
            return;
        }
        StringBuffer sb=new StringBuffer("250- Listing " + arg + "\r\n");
        sb.append(" ");
        sb.append(printFact(file, arg)).append("\r\n");
        sb.append("250 End");
        out(sb.toString());

	}


	public String getHelp() {
		// TODO Auto-generated method stub
		return "MLST";
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
