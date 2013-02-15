package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.DataChannelOutputStream;
import au.org.arcs.griffin.streams.EDataBlock;
import au.org.arcs.griffin.streams.RafInputStream;
import au.org.arcs.griffin.streams.RafOutputStream;
import au.org.arcs.griffin.streams.SynchronizedInputStream;
import au.org.arcs.griffin.streams.SynchronizedOutputStream;
import au.org.arcs.griffin.utils.IOUtils;

public class TCPDataChannel implements DataChannel {
	private static Log        log                  = LogFactory.getLog(TCPDataChannel.class);
    private static final int  MAX_BIND_RETRIES     = 3;

    private static final int  DATA_CHANNEL_TIMEOUT = 10000;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private InputStream is;
	private OutputStream os;
	private int direction;
	private FtpSessionContext ctx;
	private DataChannelInfo dataChannelInfo;
	private DataChannelProvider provider;
	private FileObject fileObject;
	private SynchronizedInputStream sis;
	private SynchronizedOutputStream sos;
	private int threadNum;
	public TCPDataChannel(Socket dataSocket, FtpSessionContext ctx, int threadNum) throws IOException {
		this.clientSocket=dataSocket;
		this.ctx=ctx;
		this.is=dataSocket.getInputStream();
		this.os=dataSocket.getOutputStream();
		this.threadNum=threadNum;
	}
	
//	public void setSourceFileObject(FileObject file){
//		this.fileObject=file;
//		direction=DIRECTION_GET;
//	}
//	public void setDestFileObject(FileObject file){
//		this.fileObject=file;
//		direction=DIRECTION_PUT;
//	}
	
	public void setDataChannelProvider(DataChannelProvider provider){
		this.provider=provider;
	}
	
	/**
	 * passive constructor
	 * @param ctx
	 * @throws IOException
	 */
	public TCPDataChannel(FtpSessionContext ctx) throws IOException {
		this.ctx=ctx;
	}
	/**
	 * active constructor
	 * @param ctx
	 * @throws IOException
	 */
	public TCPDataChannel(DataChannelInfo dataChannelInfo, FtpSessionContext ctx) throws IOException {
		this.dataChannelInfo=dataChannelInfo;
		this.ctx=ctx;
	}

	public void closeChannel() {
		IOUtils.closeGracefully(is);
		IOUtils.closeGracefully(os);
		IOUtils.closeGracefully(clientSocket);
		if (serverSocket!=null) IOUtils.closeGracefully(serverSocket);
	}

	public DataChannelInfo init() throws IOException {
		if (dataChannelInfo==null){ //passive
	        /* Get local machine address and check protocol version. */
	        InetAddress localIp = ctx.getLocalInetAddress();
//	        int currentProtocol = getProtocolIdxByAddr(localIp);
//	        boolean ok = (preferredProtocol == currentProtocol) || (preferredProtocol == 0);
//	        if (!ok) {
//	            throw new IOException("Invalid IP version");
//	        }

	        /* Get the next available port */
	        int retries = MAX_BIND_RETRIES;
	        while (retries > 0) {
	            Integer port = ctx.getNextPassiveTCPPort();
	            port = port == null ? new Integer(0) : port;
	            try {
	                log.debug("Trying to bind server socket to port " + port);
	                serverSocket = createServerSocket(localIp, port.intValue());
	                break;
	            } catch (Exception e) {
	                retries--;
	                log.debug("Binding server socket to port " + port + " failed.");
	            }
	        }
	        if (serverSocket == null) {
	            throw new IOException("Initializing server socket failed.");
	        }

	        /* Wrap up connection parameter */
	        log.debug("Server socket successfully bound to port " + serverSocket.getLocalPort() + ".");
	        dataChannelInfo = new DataChannelInfo(localIp.getHostAddress(), serverSocket.getLocalPort());

		}//else active
		return dataChannelInfo;
	}

	public int read(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		return is.read(b);
	}

	public int read(byte[] b, int start, int len) throws IOException {
		// TODO Auto-generated method stub
		return is.read(b, start, len);
	}

	public void write(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		log.debug("os:"+os);
		os.write(b);
		os.flush();
	}

	public void write(byte[] b, int start, int len) throws IOException {
		// TODO Auto-generated method stub
		log.debug("os:"+os);
		os.write(b, start, len);
		os.flush();
	}

	public void run() {
		if (clientSocket!=null) {
			try {
				log.debug("clientSocket.getKeepAlive():"+clientSocket.getKeepAlive());
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.debug("clientSocket.isConnected():"+clientSocket.isConnected());
			log.debug("clientSocket.isOutputShutdown():"+clientSocket.isOutputShutdown());
			log.debug("clientSocket.isClosed():"+clientSocket.isClosed());
			log.debug("clientSocket.isInputShutdown():"+clientSocket.isInputShutdown());
		}
		log.debug(clientSocket+" entering loop; direction="+direction);
		if (direction==DIRECTION_PUT){
			EDataBlock eDataBlock = new EDataBlock(ctx.getUser()+threadNum);
//			OutputStream os=null;
	        try {

	            boolean eod = false;
	            long count;
	            while (!eod && (count = eDataBlock.read(this)) > -1) {
	            	log.debug("thread "+threadNum+" got "+eDataBlock+" count="+count+" offset="+eDataBlock.getOffset());
	                //If we're running a modeE demux then check for end of channel
	                //allow the block to be forwarded as it may have data
	                if (eDataBlock.isDescriptorSet(EDataBlock.DESC_CODE_EODC)) {
	                    int dataChannelCount = (int) eDataBlock.getDataChannelCount();
	                    log.debug("thread "+ threadNum +" got eof. all done. dataChannelCount:"+dataChannelCount);
	                    provider.setDataChannelCount(dataChannelCount);
//	                    say(" Setting data channel count to " +dataChannelCount);
//	                    synchronized (_parent) {
//	                        _parent.setDataChannelCount(dataChannelCount);
//	                    }
	                    //Change the dc count to 1 for the pool
//	                    eDataBlock.setDCCountTo1();
	                }

	                if (eDataBlock.isDescriptorSet(EDataBlock.DESC_CODE_EOD)) {
	                	log.debug("thread "+ threadNum +" got eod.");
	                	provider.seenEOD();
	                    eod = true;
	                    
	                    //Turn off the eod flag
//	                    say("EOD received");
//	                    eDataBlock.unsetDescriptor(EDataBlock.DESC_CODE_EOD);
//	                    synchronized (_parent) {
//	                        _parent.addEODSeen();
//	                    }
	                }
//	                if (os==null){
//	                    long offset=eDataBlock.getOffset();
//	                    os=new RafOutputStream(file, offset);
//	                }
//	                os.write(eDataBlock.getData(), 0, (int)count);
//	                os.flush();
	                sos.write(eDataBlock);
	                if (eod) break;
//	                ctx.updateIncrementalStat(FtpConstants.STAT_BYTES_UPLOADED, count);
//	                incCompleted(count);
//	                if (isAbortRequested()) {
//	                    log.debug("File transfer aborted");
//	                    msgOut(MSG426);
//	                    return;
//	                }
//	                ctx.getTransferMonitor().execute(eDataBlock.getOffset(), eDataBlock.getSize());

//	                synchronized (_parent) {
//	                    ostr.write(eDataBlock.getHeader());
//	                    ostr.write(eDataBlock.getData());
//	                    ostr.flush();
//	                }
	                //say("Done writing");
	            }
//	            say("Adapter: done, EOD received ? = " + eod);
	        } catch (IOException e) {
	        	log.error(e.getMessage(), e);
//	            esay(e);
	            // what can we do here ??

	            // TIMUR: I think it does not make sence to continue,
	            // better to end this thread
//	            esay("we failed: calling _parent.SubtractDataChannel()");
//	            _parent.subtractDataChannel();
//	            return;
//	        } finally {
//	            IOUtils.closeGracefully(dataIn);
//	            if (os!=null) IOUtils.closeGracefully(os);
	        }

		}else if (direction==DIRECTION_GET){
//			SynchronizedInputStream is = new SynchronizedInputStream(new RafInputStream(fileObject, 0));
			int bufferSize=ctx.getBufferSize(); //1048576;
//			byte[] buffer = new byte[bufferSize];
			EDataBlock eDataBlock=new EDataBlock(ctx.getUser()+threadNum, bufferSize);
			eDataBlock.clearHeader();
			int count;
			long offset=0;
			long fileSize=fileObject.length();
			long lastBlockSize=0;
	        try {
	            while ((count = sis.read(eDataBlock)) != -1) {
//	            	eDataBlock.clearHeader();
//	            	eDataBlock.setSize(count);
//	            	eDataBlock.setOffset(offset);
	            	log.debug(eDataBlock);
//	            	eDataBlock.writeHeader(this);
//	            	this.write(buffer, 0, count);
	            	eDataBlock.write(this);
	                log.debug("written data:"+count);
	                offset=eDataBlock.getOffset();
	                lastBlockSize=count;
//	                incCompleted(count);
//	                if (isAbortRequested()) {
//	                    msgOut(MSG426);
//	                    log.debug("File transfer aborted");
//	                    return;
//	                }
	                ctx.getTransferMonitor().execute(eDataBlock.getOffset(), count);
	            }
	        	eDataBlock.clearHeader();
	        	eDataBlock.setData(null);
	        	log.debug("last block for "+threadNum+" offset:"+offset+" lastBlockSize:"+lastBlockSize+" fileSize:"+fileSize);
	            if (offset+lastBlockSize>=fileSize){ //end of file
		        	eDataBlock.setDescriptor(64);  //eodc
		        	eDataBlock.setDescriptor(8);  //eod
//		        	eDataBlock.setDescriptor(4);  //close data channel
		        	eDataBlock.setSize(0);
		        	eDataBlock.setOffset(provider.getMaxThread());
		        	log.debug(eDataBlock);
		        	eDataBlock.writeHeader(this);
	            }else{ //end of channel
		        	eDataBlock.setDescriptor(8);  //eod
//		        	eDataBlock.setDescriptor(4);  //close data channel
		        	eDataBlock.setSize(0);
		        	eDataBlock.setOffset(0);
		        	log.debug(eDataBlock);
		        	eDataBlock.writeHeader(this);
	            }
	        } catch (IOException e) {
				// TODO Auto-generated catch block
	        	log.error(e.getMessage(), e);
//			} finally {
//	            IOUtils.closeGracefully(is);
//	            IOUtils.closeGracefully(os);
	        }

		}
		log.debug(clientSocket+"(threadNum:"+threadNum+") done.");
//		closeChannel();
		if (provider!=null) provider.channelClosed(this);
	}

	public void start() throws IOException {
		// TODO Auto-generated method stub
		
	}
    /**
     * Creates the server socket that accepts the data connection.
     * 
     * @param localIp The local IP address.
     * @param port The port.
     * @return The server socket.
     * @throws IOException Error on creating server socket.
     */
    private ServerSocket createServerSocket(InetAddress localIp, int port) throws IOException {
        ServerSocket sock = ServerSocketFactory.getDefault().createServerSocket(port, 1, localIp);
        sock.setSoTimeout(DATA_CHANNEL_TIMEOUT);
        return sock;
    }

	public void setDirection(int direction) {
		this.direction=direction;
		
	}

	public void setFileObject(FileObject file) {
		this.fileObject=file;
		
	}

	public void setSynchronizedOutputStream(SynchronizedOutputStream sos) {
		this.sos=sos;
	}

	public void setSynchronizedInputStream(SynchronizedInputStream sis){
		this.sis=sis;
	}
	
	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return new DataChannelOutputStream(this);
	}
	
	public String toString(){
		return "Channel"+threadNum+"[clientSocket:"+clientSocket+"]";
	}

}
