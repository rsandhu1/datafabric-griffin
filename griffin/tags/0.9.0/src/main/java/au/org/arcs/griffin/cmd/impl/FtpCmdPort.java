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

import au.org.arcs.griffin.cmd.AbstractFtpCmdPort;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>DATA PORT (PORT)</b>
 * <p>
 * The argument is a HOST-PORT specification for the data port to be used in data connection. There
 * are defaults for both the user and server data ports, and under normal circumstances this command
 * and its reply are not needed. If this command is used, the argument is the concatenation of a
 * 32-bit internet host address and a 16-bit TCP port address. This address information is broken
 * into 8-bit fields and the value of each field is transmitted as a decimal number (in character
 * string representation). The fields are separated by commas. A port command would be: PORT
 * h1,h2,h3,h4,p1,p2 where h1 is the high order 8 bits of the internet host address.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 */
public class FtpCmdPort extends AbstractFtpCmdPort {
	private static Log log = LogFactory.getLog(FtpCmdPort.class);
    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        try {
            String args = getArguments();
            if (args.length() == 0) {
                msgOut(MSG501);
                return;
            }

            int protocolIdx = doReadProtocolIdx(args);
            String addr = doReadIPAddr(args);
            int port = doReadPort(args);
            log.debug("Data Channel Protocol: " + protocolIdx + ", IPAddr: " + addr + ", port: " + port);

            setupDataChannel(protocolIdx, addr, port);

            msgOut(MSG200);
        } catch (IOException e) {
            log.error(e.toString());
            out("500 Error: "+e.toString());
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            msgOut(MSG501);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets port for active transfer.";
    }


	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
