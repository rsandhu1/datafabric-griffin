package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.ftp.HostPort;
import org.globus.ftp.Session;
import org.globus.ftp.exception.FTPReplyParseException;
import org.globus.ftp.exception.ServerException;
import org.globus.ftp.exception.UnexpectedReplyCodeException;
import org.globus.ftp.extended.GridFTPControlChannel;
import org.globus.ftp.vanilla.Command;
import org.globus.ftp.vanilla.Flag;
import org.globus.ftp.vanilla.Reply;
import org.globus.ftp.vanilla.TransferMonitor;
import org.globus.ftp.vanilla.TransferState;
import org.globus.gsi.gssapi.auth.HostAuthorization;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.location.GridFTPLocation;
import au.org.arcs.griffin.location.NearestLocationFinder;
import au.org.arcs.griffin.server.impl.ConductorConstants;

public class RemoteGridFTPControlChannelProvider implements DataChannelProvider {
	
	private static Log log = LogFactory.getLog(RemoteGridFTPControlChannelProvider.class);
	private String remoteIp;
	private NearestLocationFinder locationFinder;
	private GSSCredential gssCredential;
	private ArrayList<GridFTPControlChannel> controlChannels;
	
	private int mode;
	private int direction;
	private int maxThread;
	private long offset;
	
	private int dcau;
	private AbstractFtpCmd cmd;
	private Exception serverException;
	
	public RemoteGridFTPControlChannelProvider(String remoteIp, NearestLocationFinder locationFinder, GSSCredential gssCredential){
		this.remoteIp=remoteIp;
		this.locationFinder=locationFinder;
		this.gssCredential=gssCredential;
		controlChannels=new ArrayList<GridFTPControlChannel>();
	}

	public void channelClosed(DataChannel dataChannel) {
		// TODO Auto-generated method stub

	}

	public void closeProvider() {
		String hostname;
		for (GridFTPControlChannel cc:controlChannels){
			try {
				hostname=cc.getHost();
				cc.close();
				log.debug("closed control channel of "+hostname);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public int getChannelNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMaxThread() {
		// TODO Auto-generated method stub
		return maxThread;
	}

	public DataChannelInfo init() throws IOException {
        List<String> locations=locationFinder.findLocation(remoteIp);
        if (locations==null){
        	// nothing is configured for this ip
        	throw new IOException("No location is configured for this client address.");
        }
        log.debug("selected locations:"+locations);
        GridFTPControlChannel client=null;
        for (String loc:locations){
            log.debug("trying location:"+loc);
        	GridFTPLocation location=locationFinder.getLocationById(loc);
            log.debug("connecting to remote gridftp:"+location);
            if (location!=null){
        		try {
        			client = new GridFTPControlChannel(location.getHostname(), location.getPort());
        			client.open();
        	        client.setAuthorization(HostAuthorization.getInstance());
        	        client.authenticate(gssCredential);
                    log.debug("connected to remote gridftp:"+location);
        	        break;
        		} catch (Exception e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			client=null;
				}
                log.debug("CANT connect to remote gridftp:"+location);
           }
        }
        if (client==null){
        	// none of them is connectable
            log.debug("CANT connect to any remote gridftp");
        	throw new IOException("CANT connect to any remote gridftp");
        }
        setModeAndDCAU(client);
        
        Reply reply = null;
        try {
            reply = client.execute(Command.PASV);
        } catch (UnexpectedReplyCodeException e) {
			e.printStackTrace();
        	throw new IOException("error during communicating with remote GridFTP");
        } catch (FTPReplyParseException e) {
			e.printStackTrace();
        	throw new IOException("error during communicating with remote GridFTP");
        } catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        	throw new IOException("error during communicating with remote GridFTP");
		}
        String pasvReplyMsg = null;

        pasvReplyMsg = reply.getMessage();

        int openBracket = pasvReplyMsg.indexOf("(");
        int closeBracket = pasvReplyMsg.indexOf(")", openBracket);
        String bracketContent =
            pasvReplyMsg.substring(openBracket + 1, closeBracket);
        
        HostPort port=new HostPort(bracketContent);
        
        controlChannels.add(client);
		return new DataChannelInfo(port.getHost(), port.getPort());
	}

	public boolean isUsed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void prepare() throws IOException {

	}
	
	private void setModeAndDCAU(GridFTPControlChannel cc) throws IOException{
		String modeStr=null;
        switch (mode) {
	        case FtpConstants.MODE_STREAM:
	            modeStr = "S";
	            break;
	        case FtpConstants.MODE_BLOCK:
	            modeStr = "B";
	            break;
	        case FtpConstants.MODE_EBLOCK:
	            modeStr = "E";
	            break;
	        default:
	            throw new IllegalArgumentException("Bad mode: " + mode);
        }
		try {
			cc.execute(new Command("MODE", modeStr));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		String dcauStr=null;
		switch (dcau){
		case FtpConstants.DCAU_NONE:
			dcauStr="N";
			break;
		case FtpConstants.DCAU_SELF:
			dcauStr="A";
			break;
        default:
            throw new IllegalArgumentException("Bad mode: " + mode);
		}
		try {
			cc.execute(new Command("DCAU", dcauStr));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
	}

	public DataChannel provideDataChannel() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void seenEOD() {
		// TODO Auto-generated method stub

	}

	public void setDataChannelCount(int dataChannelCount) {
		// TODO Auto-generated method stub

	}

	public void setDirection(int direction) {
		this.direction=direction;

	}

	public void setFileObject(FileObject file) {
		// TODO Auto-generated method stub

	}

	public void setMaxThread(int maxThread) {
		this.maxThread=maxThread;

	}

	public void setOffset(long offset) {
		this.offset=offset;

	}

	public void run() {
		Flag aborted = new Flag();
		if (direction==DataChannel.DIRECTION_PUT){
			GridFTPControlChannel controlChannel=controlChannels.get(0);
			try {
				controlChannel.waitFor(aborted, Session.DEFAULT_WAIT_DELAY, Session.DEFAULT_MAX_WAIT);
				log.debug("reading first reply");
				Reply firstReply = controlChannel.read();

				// 150 Opening BINARY mode data connection.
				// or
				// 125 Data connection already open; transfer starting
				if (Reply.isPositivePreliminary(firstReply)) {
//					transferState.transferStarted();
					cmd.out(firstReply.getCode()+" "+firstReply.getMessage()+" Writing to "+controlChannel.getHost());
					log.debug("first reply OK: " + firstReply.toString());
					
					for(;;) {
					
						log.debug("reading next reply");
						controlChannel.waitFor(aborted, Session.DEFAULT_WAIT_DELAY);
						log.debug("got next reply");
						Reply nextReply = controlChannel.read();
						
						//perf marker
						if (nextReply.getCode() == 112) {
						    log.debug("marker arrived: " + nextReply.toString());
//						    if (mListener != null) {
//						        mListener.markerArrived(
//						                new PerfMarker(nextReply.getMessage()));
//						    }
						    cmd.out(nextReply.getCode()+" "+nextReply.getMessage());
						    continue;
						}
						
						//restart marker
						if (nextReply.getCode() == 111) {
						    log.debug("marker arrived: " + nextReply.toString());
//						    if (mListener != null) {
//						        mListener.markerArrived(
//						                new GridFTPRestartMarker(
//						                        nextReply.getMessage()));
//						    }
						    cmd.out(nextReply.getCode()+" "+nextReply.getMessage());
						    continue;
						}
						
						//226 Transfer complete
						if (nextReply.getCode() == 226) {
//						    abortable = false;
						    log.debug("transfer complete: " + nextReply.toString());
						    cmd.out(nextReply.getCode()+" "+nextReply.getMessage());
						    break;
						}
						// any other reply
						log.debug("unexpected reply: " + nextReply.toString());
						log.debug("exiting the transfer thread");
						this.serverException = ServerException.embedUnexpectedReplyCodeException(
						        new UnexpectedReplyCodeException(nextReply),
						        "Server reported transfer failure");
						
//						transferState.transferError(e);
//						other.abort();
						break;
					}
	            } else {    //first reply negative
	                log.debug("first reply bad: " + firstReply.toString());
	                log.debug("category: " + firstReply.getCategory());
//	                abortable = false;
	                this.serverException = ServerException.embedUnexpectedReplyCodeException(
	                        new UnexpectedReplyCodeException(firstReply));

//	                transferState.transferError(e);
//	                other.abort();
	            }
			} catch (ServerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				this.serverException=e1;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				this.serverException=e1;
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				this.serverException=e1;
			} catch (FTPReplyParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ServerException se = ServerException.embedFTPReplyParseException(e);
				this.serverException=se;
			}


		}

	}
	
	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public Exception getServerException(){
		return this.serverException;
	}

	public int getDcau() {
		return dcau;
	}

	public void setDcau(int dcau) {
		this.dcau = dcau;
	}

	public void sendCmd(Command command, AbstractFtpCmd cmd) throws IllegalArgumentException, IOException {
		this.cmd=cmd;
		for (GridFTPControlChannel cc:controlChannels){
			cc.write(command);
		}		
	}

}
