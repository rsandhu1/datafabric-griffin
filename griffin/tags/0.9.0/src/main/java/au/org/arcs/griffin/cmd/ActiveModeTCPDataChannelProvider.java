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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.RafInputStream;
import au.org.arcs.griffin.streams.SynchronizedInputStream;
import au.org.arcs.griffin.utils.IOUtils;

/**
 * Provider for the client socket (active transfer). The creation of the socket is deferred until it
 * is needed.
 * 
 * @author Behnke
 * @author Shunde Zhang
 */
public class ActiveModeTCPDataChannelProvider extends TCPDataChannelProvider {
	private static Log          log               = LogFactory.getLog(ActiveModeTCPDataChannelProvider.class);

    private DataChannelInfo   dataChannelInfo;

    private SynchronizedInputStream sis;
    private long offset;

	private boolean isUsed;

    /**
     * Constructor.
     * 
     * @param ctx Session context.
     * @param info Channel about the data channel to open.
     */
    public ActiveModeTCPDataChannelProvider(FtpSessionContext ctx, DataChannelInfo info) {
        this.ctx = ctx;
        this.dataChannelInfo = info;
    }

    /**
     * {@inheritDoc}
     */
    public DataChannelInfo init() throws IOException {
    	closeProvider();
        return dataChannelInfo;
    }

    /**
     * {@inheritDoc}
     */
    public DataChannel provideDataChannel() throws IOException {
        if (channels == null) {
        	channels=new ArrayList<DataChannel>();
        }
        if (channels.size()>0) return channels.get(0);
        
        Socket socket = createClientSocket();
        DataChannel dc;
		try {
			dc = new TCPDataChannel(wrapSocket(socket, true), ctx, 0);
		} catch (GSSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
        channels.add(dc);
        return dc;
    }

    /**
     * {@inheritDoc}
     */
    public void closeProvider() {
    	log.debug("closing provider.");
        if (sis!=null)
			try {
				sis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		sis=null;
        if (channels != null) {
            for (DataChannel channel:channels) channel.closeChannel();
            channels = null;
        }

    }

    private Socket createClientSocket() throws IOException {
        Socket dataSocket;
        dataSocket = new Socket();
        dataSocket.setKeepAlive(true);
        dataSocket.setReuseAddress(true);
        InetSocketAddress serverEndpoint=new InetSocketAddress(dataChannelInfo.getAddress(), dataChannelInfo.getPort());
        dataSocket.connect(serverEndpoint);
//        dataSocket = SocketFactory.getDefault().createSocket(dataChannelInfo.getAddress(),
//                dataChannelInfo.getPort());
        log.debug("established socket "+dataSocket+" to client:"+dataChannelInfo.getAddress()+" "+dataChannelInfo.getPort());
        return dataSocket;
    }

	public void run() {
		Thread[] transferThreads=new Thread[channels.size()];
		for (int i=0;i<channels.size();i++){
			transferThreads[i]=new Thread(channels.get(i));
		}
		for (int i=0;i<channels.size();i++){
			transferThreads[i].start();
		}
		for (int i=0;i<channels.size();i++){
			if (transferThreads[i].isAlive())
				try {
					log.debug("thread "+i+" joined.");
					transferThreads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		log.debug("all threads finished(?) channels.size()="+channels.size());
		if (sis!=null){
			try {
				sis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sis=null;
		}
	}

	public void channelClosed(DataChannel dataChannel) {
//		channels.remove(dataChannel);
//		log.debug("1 channel is closed. "+channels.size()+" left.");
	}


	public int getChannelNumber(){
		return channels.size();
	}
	

	public void prepare() throws IOException {
		if (direction==DataChannel.DIRECTION_GET) sis=new SynchronizedInputStream(fileObject.getInpuStream(offset), offset, ctx.getBufferSize());
		if (channels==null||channels.size()<maxThread){
//			log.debug("new provider, init'ing...");
			if (channels==null) channels=new ArrayList<DataChannel>();
			int currentNumber=channels.size();
			log.debug("opened channel number:"+currentNumber+" required channel number:"+maxThread);
			for (int i=currentNumber;i<maxThread;i++){
				Socket s=createClientSocket();
				TCPDataChannel dc;
				try {
					dc = new TCPDataChannel(wrapSocket(s, true),ctx,i);
				} catch (GSSException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new IOException(e.getMessage());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new IOException(e.getMessage());
				}
				channels.add(dc);
			}
		}else{
//			log.debug("used provider");
			isUsed=true;
		}
		log.debug("channels:"+channels);
		for (DataChannel dc:channels){
			dc.setDirection(direction);
			dc.setFileObject(fileObject);
			dc.setDataChannelProvider(this);
			if (direction==DataChannel.DIRECTION_GET) {
				dc.setSynchronizedInputStream(sis);
			}
		}
	}

	public void seenEOD() {
		// TODO Auto-generated method stub
		
	}

	public void setDataChannelCount(int dataChannelCount) {
		// TODO Auto-generated method stub
		
	}
	public void setOffset(long offset) {
		this.offset=offset;
		
	}

	public boolean isUsed() {
		// TODO Auto-generated method stub
		return this.isUsed;
	}

	public SynchronizedInputStream getSis() {
		return sis;
	}

	@Override
	public void transferData() throws IOException {
		Thread[] transferThreads=new Thread[channels.size()];
		for (int i=0;i<channels.size();i++){
			transferThreads[i]=new Thread(channels.get(i));
		}
		for (int i=0;i<channels.size();i++){
			transferThreads[i].start();
		}
		sis.feedQueue();
		sis.addEndingMarker(channels.size());
		for (int i=0;i<channels.size();i++){
			if (transferThreads[i].isAlive())
				try {
					log.debug("thread "+i+" joined.");
					transferThreads[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		log.debug("all threads finished(?) channels.size()="+channels.size());
		if (sis!=null){
			try {
				sis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sis=null;
		}
	}
	
}
