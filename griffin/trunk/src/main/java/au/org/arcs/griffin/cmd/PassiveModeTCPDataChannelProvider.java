/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */


package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;
import au.org.arcs.griffin.streams.SynchronizedOutputStream;
import au.org.arcs.griffin.utils.IOUtils;

/**
 * Provides the data transfer socket for transfer passive mode.
 * 
 * @author Behnke
 * @author Shunde Zhang
 */
public class PassiveModeTCPDataChannelProvider extends TCPDataChannelProvider {

    private static final int  MAX_BIND_RETRIES     = 3;

    private static final int  DATA_CHANNEL_TIMEOUT = 10000;

    private static Log        log                  = LogFactory.getLog(PassiveModeTCPDataChannelProvider.class);


    private ServerSocket      serverSocket;


    private int               preferredProtocol;
    
    
    private boolean running;
    
    private SynchronizedOutputStream sos;
    
    private int eodNum;
    private int dataChannelCount;
    private long offset;
	private boolean isUsed;
	private List<Thread> transferThreads=new ArrayList<Thread>();

    /**
     * Constructor.
     * 
     * @param ctx Session context.
     * @param preferredProtocol Preferred protocol (IPv4 or IPv6)
     */
    public PassiveModeTCPDataChannelProvider(FtpSessionContext ctx, int preferredProtocol) {
        this.ctx = ctx;
        this.preferredProtocol = preferredProtocol;
        this.dataChannelCount=-1;
    }

    /**
     * {@inheritDoc}
     */
    public DataChannelInfo init() throws IOException {

        /* Get local machine address and check protocol version. */
        InetAddress localIp = ctx.getLocalInetAddress();
        int currentProtocol = getProtocolIdxByAddr(localIp);
        boolean ok = (preferredProtocol == currentProtocol) || (preferredProtocol == 0);
        if (!ok) {
            throw new IOException("Invalid IP version");
        }

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
        return new DataChannelInfo(localIp.getHostAddress(), serverSocket.getLocalPort());

    }

    /**
     * {@inheritDoc}
     */
    public void closeProvider() {
    	log.debug("closing provider.");
    	running=false;
        IOUtils.closeGracefully(serverSocket);
        if (sos!=null)
			try {
				sos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		sos=null;
        if (channels != null) {
            for (DataChannel channel:channels) channel.closeChannel();
            channels = null;
        }
        serverSocket = null;

    }

    /**
     * {@inheritDoc}
     */
    public DataChannel provideDataChannel() throws IOException {
        if (serverSocket == null) {
            throw new IOException("Server socket not initialized.");
        }
        if (channels == null) {
        	channels=new ArrayList<DataChannel>();
        }
        if (channels.size()>0) return channels.get(0);
        
        Socket dataSocket = serverSocket.accept();
        TCPDataChannel dc;
		try {
			dc = new TCPDataChannel(wrapSocket(dataSocket, false),ctx, 0);
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
//		dc.setDirection(direction);
//		dc.setFileObject(fileObject);
//		dc.setDataChannelProvider(this);
//		if (direction==DataChannel.DIRECTION_PUT) dc.setSynchronizedOutputStream(sos);
        channels.add(dc);
        return dc;
    }

    private int getProtocolIdxByAddr(InetAddress addr) {
        if (addr instanceof Inet4Address) {
            return 1;
        } else if (addr instanceof Inet6Address) {
            return 2;
        } else {
            return 0;
        }
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
        ServerSocket sock;
//        Boolean dataProtection = (Boolean) ctx.getAttribute(FtpConstants.ATTR_DATA_PROT);
        sock = ServerSocketFactory.getDefault().createServerSocket(port, 1, localIp);
        sock.setSoTimeout(DATA_CHANNEL_TIMEOUT);
        return sock;
    }

	
	public void prepare() throws IOException {
		this.eodNum=0;
		if (direction==DataChannel.DIRECTION_PUT){
			RandomAccessFileObject rafo=fileObject.getRandomAccessFileObject("rw");
			if (offset>0) rafo.seek(offset);
			sos=new SynchronizedOutputStream(rafo, ctx);
		}
		if (channels!=null){
			isUsed=true;
			for (DataChannel dc:channels){
				dc.setDirection(direction);
				dc.setFileObject(fileObject);
				dc.setDataChannelProvider(this);
				if (direction==DataChannel.DIRECTION_PUT) dc.setSynchronizedOutputStream(sos);
			}
		}
	}

	public void run() {
		if (channels==null){  //no existing channels
			running=true;
			channels=new ArrayList<DataChannel>();
			int num=0;
			while (running){
				try {
					Socket dataSocket = serverSocket.accept();
					log.debug("accepted a new connection from client:"+dataSocket);
					TCPDataChannel dc=new TCPDataChannel(wrapSocket(dataSocket, false), ctx, num);
					dc.setDirection(direction);
					dc.setFileObject(fileObject);
					dc.setDataChannelProvider(this);
					if (direction==DataChannel.DIRECTION_PUT) dc.setSynchronizedOutputStream(sos);
					channels.add(dc);
					Thread t=new Thread(dc);
					transferThreads.add(t);
					t.start();
					num++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					running=false;
					break;
				} catch (GSSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					running=false;
					break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					running=false;
					break;
				}
			}
		}else{  //there are existing channels, reuse them
			isUsed=true;
			transferThreads.clear();
			for (int i=0;i<channels.size();i++){
				transferThreads.add(new Thread(channels.get(i)));
			}
			for (Thread t:transferThreads){
				t.start();
			}
		}
	}
	public void channelClosed(DataChannel dc){
//		channels.remove(dc);
//		log.debug("1 channel is closed. "+channels.size()+" left.");
//		if (channels.size()==0) {
//			running=false;
//			IOUtils.closeGracefully(serverSocket);
//		}
	}



	public void seenEOD() {
		this.eodNum++;
		if (dataChannelCount>0&&dataChannelCount==eodNum) {
			log.debug("got all "+dataChannelCount+" eod(s). closing serverSocket:"+serverSocket);
	    	running=false;
	        IOUtils.closeGracefully(serverSocket);
		}
	}

	public void setDataChannelCount(int dataChannelCount) {
		this.dataChannelCount=dataChannelCount;
		
	}

	public void setOffset(long offset) {
		this.offset=offset;
		
	}
	public boolean isUsed() {
		// TODO Auto-generated method stub
		return this.isUsed;
	}

	@Override
	public void transferData() throws IOException {
		sos.pollQueue();
		for (Thread t:transferThreads){
			if (t.isAlive()){
				try {
					log.debug("thread "+t+" joined.");
					t.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		log.debug("all threads finished. channels.size()="+channels.size()+" closing sos");
		if (sos!=null){
			try {
				sos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sos=null;
		}
		
	}

}
