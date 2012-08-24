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

package au.org.arcs.griffin.cmd.impl;

import java.io.IOException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.cmd.ClientSocketModifier;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>AUTHENTICATION/SECURITY MECHANISM (AUTH)</b>
 * <p>
 * This is for GSI authentication only.
 * 
 * @author Shunde Zhang
 */
public class FtpCmdAuthGSI extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdAuthGSI.class);

    /**
     * Some notes about SSL support: Use keytool to generate a keystore/key: <code>
     * keytool -genkey -alias behnke -keyalg DSA -keystore keystore -validity 365 -storepass secret -keypass secret
     * </code>
     * The attributes keypass and storepass must be equal! {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String prot = getArguments().toUpperCase().trim();
        if (!"GSSAPI".equals(prot)) {
            msgOut(MSG504);
            return;
//            executed = true;
        }

//        if (!getCtx().getOptions().getBoolean(OPT_SSL_ALLOW_EXPLICIT, true)) {
//            msgOut(MSG534);
//            executed = true;
//        }

//        if (!executed) {
            if (getCtx().getServiceContext() != null && getCtx().getServiceContext().isEstablished()) {
                msgOut(MSG234_GSI);
//                executed = true;
                return;
            }

            try {
            	getCtx().setServiceContext(getCtx().getOptions().getGSSContext());
            } catch( Exception e ) {
            	msgOut(MSG500_ERROR, new Object[] {e.toString()});
//                executed = true;
                return;
            }
            msgOut(MSG334);
            log.debug("auth gsi done.");
//            executed = true;
//        }
//        synchronized (this) {
//            notifyAll();
//        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Initiates an explicit GSIAPI connection. See RFC 2228.";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return false;
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
