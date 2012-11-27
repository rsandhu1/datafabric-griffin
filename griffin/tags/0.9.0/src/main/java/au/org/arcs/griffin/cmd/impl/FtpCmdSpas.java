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

import au.org.arcs.griffin.cmd.AbstractFtpCmdPasv;

/**
 * <b>Striped Passive (SPAS)</b>
 * <p>
 * This extension is used to establish a vector of data socket listeners 
 * for each stripe of the data. To simplify interaction with the 
 * parallel data transfer extensions, the SPAS MUST only be done on a 
 * control connection when the data is to be stored onto the file space 
 * served by that control connection. The SPAS command requests the FTP 
 * server to "listen" on a data port (which is not the default data 
 * port) and to wait for one or more data connections, rather than 
 * initiating a connection upon receipt of a transfer command. The 
 * response to this command includes a list of host and port addresses 
 * the server is listening on.  This command MUST always be used in 
 * conjunction with the extended block mode.  
 * 
 * </p>
 * 
 * @author Shunde Zhang
 */
public class FtpCmdSpas extends AbstractFtpCmdPasv {

    /**
     * {@inheritDoc}
     */
    protected int getPreferredProtocol() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    protected String createResponseMessage(int protocolIdx, String ip, int port) {
        StringBuffer addrPort = new StringBuffer("229-Entering Striped Passive Mode\r\n");
        String[] ipParts = ip.split("\\.");
        int idx = 0;
        addrPort.append(" ");
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        addrPort.append(ipParts[idx++].trim() + SEPARATOR);
        int p1 = (port >> BYTE_LENGTH) & BYTE_MASK;
        int p2 = port & BYTE_MASK;
        addrPort.append(p1 + SEPARATOR);
        addrPort.append(p2 + "\r\n");
        addrPort.append("229 End");
        return addrPort.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Activates striped passive transfer mode";
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
