package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.AbstractFtpCmdList;
import au.org.arcs.griffin.cmd.AbstractFtpCmdMlsx;
import au.org.arcs.griffin.cmd.DataChannel;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.EBlockModeOutputStream;
import au.org.arcs.griffin.utils.IOUtils;

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
public class FtpCmdMlsd extends AbstractFtpCmdMlsx {
	private static Log log = LogFactory.getLog(FtpCmdMlsd.class);
	
    public void execute() throws FtpCmdException {
        String charset = getCtx().getCharset();
//        PrintWriter dataOut = null;
        int mode = getCtx().getTransmissionMode();
        int dataType=getCtx().getDataType();
        OutputStream os=null; 
        try {
            DataChannel dataChannel = getCtx().getDataChannelProvider().provideDataChannel();
            log.debug("MLSD in mode "+mode+" buffersize:"+getCtx().getBufferSize()+" dataType:"+dataType);
            if (mode == MODE_EBLOCK) 
            	os=new EBlockModeOutputStream(dataChannel.getOutputStream(), getCtx().getBufferSize());
            else
            	os=dataChannel.getOutputStream();
//            dataOut = new PrintWriter(new OutputStreamWriter(os, charset));

            String arg = getArguments();
            String path = getCtx().getRemoteDir();
            if (arg!=null&&arg.length()>0) path = getAbsPath(arg);

            FileObject dir=getCtx().getFileSystemConnection().getFileObject(path);
            log.debug("listing dir "+dir.getCanonicalPath());

            // TODO Allow filtering with wildcards *, ?

            if (!dir.exists()) {
                msgOut(MSG550);
                return;
            }
            
            out("150 BINARY connection open for MLSD "+arg);
            if (dir.isDirectory()) {
            	
            	StringBuffer cur;
            	if (!path.equals("/")){
	            	cur=new StringBuffer("Type=pdir;Modify=");
	            	cur.append(formatTime(dir.getParent().lastModified())).append(";Size=4096").append(";Perm=el; ");
	            	cur.append("..").append(dataType==DT_ASCII?"\r":"").append("\r\n");
	            	log.debug("printing to data channel: "+cur);
	            	os.write(cur.toString().getBytes(getCtx().getCharset()));
            	}
                FileObject[] files = dir.listFiles();

                for (int i = 0; i < files.length; i++) {
                    doPrintFileInfo(os, files[i], getCtx());
                    if (mode != MODE_EBLOCK) os.flush();
                }
                
            	cur=new StringBuffer("Type=cdir;Modify=");
            	cur.append(formatTime(dir.lastModified())).append(";Size=4096").append(";Perm=cfmpel; ");
            	cur.append(".").append(dataType==DT_ASCII?"\r":"").append("\r\n");
            	log.debug("printing to data channel: "+cur);
            	os.write(cur.toString().getBytes(getCtx().getCharset()));

            } else {
                doPrintFileInfo(os, dir, getCtx());
                if (mode != MODE_EBLOCK) os.flush();
            }
            if (mode==MODE_EBLOCK) ((EBlockModeOutputStream)os).finalizeRecord(true);
            out("226 MLSD completed");
        } catch (IOException e) {
        	e.printStackTrace();
            msgOut(MSG550);
        } catch (Exception e) {
        	e.printStackTrace();
            msgOut(MSG550);
        } finally {
//            IOUtils.closeGracefully(os);
            if (mode == MODE_STREAM) getCtx().closeDataChannels();
        }
    }

    protected void doPrintFileInfo(OutputStream out, FileObject file, FtpSessionContext ctx) throws IOException {
    	int mode = getCtx().getTransmissionMode();
        int dataType=getCtx().getDataType();
//    	log.debug("out:"+out);
    	String str=printFact(file,file.getName());
    	log.debug("printing to data channel: "+str);
        out.write((str+(dataType==DT_ASCII?"\r":"")+"\r\n").getBytes(getCtx().getCharset()));
    }

	public String getHelp() {
		// TODO Auto-generated method stub
		return "MLSD";
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
