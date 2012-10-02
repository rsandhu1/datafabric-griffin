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

import au.org.arcs.griffin.exception.FtpCmdException;


/**
 * <b>Extended STORE (ESTO)</b>
 * <p>
 * This is analogous to the STOR command, but it allows the data to be 
 * manipulated (typically reduced in size) before being stored.
 * </p>
 * 
 * @author Lars Behnke
 */
public class FtpCmdEsto extends FtpCmdStor {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
    	String arg=getArguments();
        String[] st = arg.split("\\s+");
        if (st.length != 3) {
        	out("500 Syntax error, ESTO should have more arguments. Syntax: ESTO <SP> A <SP> <offset> <filename> <CRLF>");
            return;
        }
        String extended_store_mode = st[0];
        if (!extended_store_mode.equalsIgnoreCase("a")) {
            out("504 ESTO is not implemented for store mode: "
                  + extended_store_mode);
            return;
        }
        String offset = st[1];
        String filename = st[2];
        long asm_offset;
        try {
            asm_offset = Long.parseLong(offset);
        } catch (NumberFormatException e) {
            String err = "501 ESTO Adjusted Store Mode: invalid offset " + offset;
            out(err);
            return;
        }
        getCtx().setAttribute(ATTR_FILE_OFFSET, asm_offset);
        if (asm_offset != 0) {
            out("504 ESTO Adjusted Store Mode does not work with nonzero offset: " + offset);
            return;
        }
        this.setArguments(filename);
        super.execute(false);
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Store file on server";
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthenticationRequired() {
        return true;
    }

	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}


}
