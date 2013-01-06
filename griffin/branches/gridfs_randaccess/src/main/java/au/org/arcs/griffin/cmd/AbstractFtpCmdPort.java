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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract super class of commands setting up the data channel in passive mode (PASV, EPSV).
 * 
 * @author Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdPort extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(AbstractFtpCmdPort.class);
    private static final String DOT = ".";
    private int                 port;
    private String              addr;
    private String              lastArgs;

    /**
     * Sets up the data channel in active transfer mode. IPv4 and IPv6 are supported.
     * 
     * @param protocolIdx Protocol index (IPv4 or IPv6)
     * @param ipAddr IPv4 or IPv6 compliant address.
     * @param port The port.
     * @throws IOException Setting up data channel failed.
     */
    protected void setupDataChannel(int protocolIdx, String ipAddr, int port) throws IOException {
    	getCtx().closeDataChannels();
        DataChannelInfo info = new DataChannelInfo(ipAddr, port);
        DataChannelProvider provider = null;
        if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
        }else{ //create TCP provider
        	provider=new ActiveModeTCPDataChannelProvider(getCtx(), info);
        }
        provider.init();
        getCtx().setDataChannelProvider(provider);
    	
//    	if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
//    		DataChannel dc=new UDTDataChannel(getCtx(), ipAddr, port);
////    		DataChannelInfo info=dc.init();
//    		getCtx().setDataChannel(dc);
//    	}else{
//            getCtx().closeSockets();
//            DataChannelInfo info = new DataChannelInfo(ipAddr, port);
//            SocketProvider provider = new ActiveModeSocketProvider(getCtx(), info);
//            provider.init();
//            getCtx().setDataSocketProvider(provider);
//    	}
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    protected String doReadIPAddr(String args) {
        if (!paramsParsed(args)) {
            parseParams(args);
        }
        return addr;
    }

    /**
     * {@inheritDoc}
     */
    protected int doReadPort(String args) {
        if (!paramsParsed(args)) {
            parseParams(args);
        }
        return port;
    }

    /**
     * {@inheritDoc}
     */
    protected int doReadProtocolIdx(String args) {
        return 1;
    }

    private boolean paramsParsed(String args) {
        return lastArgs != null && lastArgs.equals(args);
    }

    private void parseParams(String args) {
        try {
            lastArgs = args;
            String[] argParts = getArguments().split(",");
            int idx = 0;
            addr = argParts[idx++].trim() + DOT + argParts[idx++].trim() + DOT + argParts[idx++].trim() + DOT
                    + argParts[idx++].trim();
            int p1 = Integer.parseInt(argParts[idx++].trim()) & BYTE_MASK;
            int p2 = Integer.parseInt(argParts[idx++].trim()) & BYTE_MASK;
            port = (p1 << BYTE_LENGTH) + p2;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid arguments: " + args);
        }
    }

}
