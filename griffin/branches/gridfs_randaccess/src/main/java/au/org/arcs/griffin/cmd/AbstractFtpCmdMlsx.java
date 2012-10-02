package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.AbstractFtpCmdList;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.utils.IOUtils;
import au.org.arcs.griffin.utils.SecurityUtil;

/**
 * <b> Listings for Machine Processing </b>
 * <p>
 * The MLST and MLSD commands are intended to standardize the file and
 * directory information returned by the server-FTP process.  These
 * commands differ from the LIST command in that the format of the
 * replies is strictly defined although extensible.
 * 
 * Two commands are defined, MLST and MLSD.  MLST provides data about
 * exactly the object named on its command line, and no others. MLSD,
 * on the other, lists the contents of a directory if a directory is
 * named, otherwise a 501 reply is returned.  In either case, if no
 * object is named, the current directory is assumed.  That will cause
 * MLST to send a one-line response, describing the current directory
 * itself, and MLSD to list the contents of the current directory.
 * 
 * @author Shunde Zhang
 *
 */
abstract public class AbstractFtpCmdMlsx extends AbstractFtpCmd {
	private static Log log = LogFactory.getLog(AbstractFtpCmdMlsx.class);
	private SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	
    protected String formatTime(long time){
    	return sdf.format(new Date(time));
    }

    protected String printFact(FileObject file, String fileName) {
		StringBuffer buffer=new StringBuffer();
//    	if (fileName.equals(".")){
//        	buffer.append("Type=cdir;Modify=");
//        	buffer.append(formatTime(file.lastModified())).append(";Size=4096").append(";Perm=cfmpel; ");
//        	buffer.append("."); //.append("\r\r\n");
//        	return buffer.toString();
//    	}else if (fileName.equals("..")){
//    		buffer.append("Type=pdir;Modify=");
//    		buffer.append(formatTime(file.lastModified())).append(";Size=4096").append(";Perm=el; ");
//    		buffer.append(".."); //.append("\r\r\n");
//        	return buffer.toString();
//    	}
        if (file.isDirectory()){
//        	if (fileName.equals("."))
//            	buffer.append("Type=cdir;");
//        	else if (fileName.equals(".."))
//        		buffer.append("Type=pdir;");
//        	else
        	buffer.append("Type=dir;");
        }else{
        	buffer.append("Type=file;");
        }
        buffer.append("Modify=").append(formatTime(file.lastModified())).append(";");
        if (file.isDirectory())
            buffer.append("Size=").append("4096").append(";");
        else
        	buffer.append("Size=").append(file.length()).append(";");
        buffer.append("Perm=");
        int perm=file.getPermission();
        log.debug("File permissions for \"" + file.getPath() + "\": "
                  + Integer.toOctalString(perm));
        int unixPerm=0;
        if (file.isDirectory()){
        	unixPerm++;
        	if ((perm&PRIV_WRITE)==PRIV_WRITE) {
        		unixPerm=unixPerm+4;
        		buffer.append("cpmf");
        	}
        	buffer.append('e');
        	if ((perm&PRIV_READ)==PRIV_READ) {
        		unixPerm=unixPerm+2;
        		buffer.append("l");
        	}
        }else{
        	if ((perm&PRIV_WRITE)==PRIV_WRITE) {
        		unixPerm=unixPerm+4;
        		buffer.append("awf");
        	}
        	if ((perm&PRIV_READ)==PRIV_READ) {
        		unixPerm=unixPerm+2;
        		buffer.append("r");
        	}
        }
        buffer.append(";");
       	buffer.append("UNIX.mode=0").append(unixPerm).append("00;");
        buffer.append("UNIX.owner=");
        buffer.append(getCtx().getUser());
        buffer.append(";");
        buffer.append("UNIX.group=nobody");
        buffer.append(";");
        buffer.append("Unique=");
        try {
			buffer.append(SecurityUtil.encodePassword(file.getCanonicalPath(), "MD5"));
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			buffer.append("unique-id");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			buffer.append("unique-id");
		}
        buffer.append(";");
        buffer.append(" ");
        buffer.append(fileName);
//        if (printFullPath)
//        	buffer.append(file.getCanonicalPath());
//        else
//        	buffer.append(file.getName());
		return buffer.toString();
	}

}
