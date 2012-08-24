package au.org.arcs.griffin.cmd;

import java.net.Socket;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.extended.GridFTPServerFacade;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.FileObject;

abstract public class TCPDataChannelProvider implements DataChannelProvider {
	private static Log          log               = LogFactory.getLog(TCPDataChannelProvider.class);
    protected FtpSessionContext ctx;
    protected List<DataChannel>            channels;
    protected int maxThread;
    protected FileObject fileObject;
    protected int direction;
    
    
	public int getMaxThread(){
		return this.maxThread;
	}
	public void setMaxThread(int maxThread) {
		this.maxThread=maxThread;
		
	}
	public void setDirection(int direction) {
		this.direction=direction;
		
	}

	public void setFileObject(FileObject file) {
		this.fileObject=file;
		
	}
	public int getChannelNumber(){
		return channels.size();
	}
	
	public Socket wrapSocket(Socket socket, boolean isClientSocket) throws GSSException, Exception{
//		DataChannelAuthentication dcau=DataChannelAuthentication.NONE;
		int protLevel="P".equalsIgnoreCase((String) ctx.getAttribute(FtpConstants.ATTR_DATA_PROT))?GridFTPSession.PROTECTION_PRIVATE:GridFTPSession.PROTECTION_CLEAR;
		if (ctx.getDCAU()==FtpConstants.DCAU_SELF) {
//			DataChannelAuthentication dcau=DataChannelAuthentication.SELF;
			log.debug("creating secure socket for DCAU self. isClientSocket:"+isClientSocket+" protLevel:"+protLevel);
			return GridFTPServerFacade.authenticate(socket, isClientSocket, ctx.getServiceContext().getDelegCred(), protLevel, DataChannelAuthentication.SELF);
		}else
			return socket;
//		if (ctx.getDCAU()==FtpConstants.DCAU_SUBJECT) dcau=new DataChannelAuthentication("S");
	}

}
