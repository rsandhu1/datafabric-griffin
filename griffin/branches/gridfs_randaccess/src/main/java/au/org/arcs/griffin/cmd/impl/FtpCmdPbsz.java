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

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>PROTECTION BUFFER SIZE (PBSZ)</b>
 * <p>
 * The argument is a decimal integer representing the maximum size, in bytes, of the encoded data
 * blocks to be sent or received during file transfer. This number shall be no greater than can be
 * represented in a 32-bit unsigned integer.
 * <p>
 * This command allows the FTP client and server to negotiate a maximum protected buffer size for
 * the connection. There is no default size; the client must issue a PBSZ command before it can
 * issue the first PROT command.
 * <p>
 * The PBSZ command must be preceded by a successful security data exchange.
 * <p>
 * If the server cannot parse the argument, or if it will not fit in 32 bits, it should respond with
 * a 501 reply code.
 * <p>
 * If the server has not completed a security data exchange with the client, it should respond with
 * a 503 reply code.
 * <p>
 * Otherwise, the server must reply with a 200 reply code. If the size provided by the client is too
 * large for the server, it must use a string of the form "PBSZ=number" in the text part of the
 * reply to indicate a smaller buffer size. The client and the server must use the smaller of the
 * two buffer sizes if both buffer sizes are specified.
 * <p>
 * <i>[Excerpt from RFC-2228, Horowitz and Lunt]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdPbsz extends AbstractFtpCmd {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
    	
		String arg=getArguments();
        if (arg.equals("")) {
            out("500 must supply a buffer size");
            return;
        }

        int bufsize;
        try {
            bufsize = Integer.parseInt(arg);
        } catch(NumberFormatException ex) {
            out("500 bufsize argument must be integer");
            return;
        }

        if (bufsize < 1) {
            out("500 bufsize must be positive.  Probably large, but at least positive");
            return;
        }

        getCtx().setBufferSize(bufsize);

    	
//    	msgOut(MSG200);
//        Boolean ssl = (Boolean) getCtx().getAttribute(ATTR_SSL);
//        if (ssl == null || !ssl.booleanValue()) {
//            msgOut(MSG503);
//        } else {
            msgOut(MSG200_PBSZ, new Object[] {String.valueOf(bufsize)});
//        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the maximum size of the encode data blocks during file transfer.";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return false;
	}

}
