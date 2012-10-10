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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSException;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpConfigException;


/**
 * <b>USER NAME (USER)</b>
 * <p>
 * This is only for GSI, it'll find out the user according to the certificate.
 * </p>
 * 
 * @author Shunde Zhang
 */
public class FtpCmdUserGSI extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdUserGSI.class);
    public static final String GLOBUS_URL_COPY_DEFAULT_USER = ":globus-mapping:";

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String prot = getArguments().trim();
        if (prot == null || prot.length() <= 0) {
            msgOut(MSG501);
            return;
        }
        if (getCtx().getControlChannelMode()==1){
        	getCtx().setUser(getArguments());
            msgOut(MSG230_GSI_USER, getArguments());
            return;
        }
        if (getCtx().getServiceContext() == null
                || !getCtx().getServiceContext().isEstablished()) {
            msgOut(MSG503_ENC);
            return;
        }
        if (prot.equalsIgnoreCase(GLOBUS_URL_COPY_DEFAULT_USER)) {

            try {
                getCtx().authenticate();
                String user = getCtx().getUser();
                if (user == null || user.length() == 0) {
                    out("530 User Authorization failed: Cannot map a user with the given DN.");
                    return;
                }
                msgOut(MSG230_GSI_USER, new String[] {user});
            } catch (GSSException e) {
                log.error("GSI authorisation failed: " + e.getMessage());
                msgOut(MSG530_AUTH_GSI_USER, new String[] {e.getMessage()});
                return;
            } catch (IOException e) {
                log.error("GSI authorisation failed: " + e.getMessage());
                msgOut(MSG530_AUTH_GSI_USER, new String[] {e.getMessage()});
                return;
            }
            String clientHost = getCtx().getClientInetAddress()
                                        .getHostAddress();
            getCtx().getEventListener()
                    .loginPerformed(clientHost, getCtx().isAuthenticated());

        } else {
            out("530 Permission denied");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the user name for GSI";
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
