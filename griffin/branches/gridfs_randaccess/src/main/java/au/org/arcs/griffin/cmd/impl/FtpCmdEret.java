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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>EXTENDED RETRIEVE (ERET)</b>
 * <p>
 * This is analogous to the RETR  command, but it allows the data to be 
 * manipulated (typically reduced size) before being transmitted.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdEret extends FtpCmdRetr {

    private static Log log = LogFactory.getLog(FtpCmdEret.class);

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
    	String arg=getArguments();
        String[] st = arg.split("\\s+");
        if (st.length != 4) {
            out("500 Syntax error, ERET should have more arguments. Syntax: ERET <SP> P <SP> <offset> <SP> <size> <SP> <filename>");
            return;
        }
        String extended_retrieve_mode = st[0];
        if (!extended_retrieve_mode.equalsIgnoreCase("p")) {
            out("504 ERET is not implemented for retrieve mode: "+extended_retrieve_mode);
            return;
        }
        String offset = st[1];
        String size = st[2];
        String filename = st[3];
        try {
            Long prm_offset = new Long(offset);
            getCtx().setAttribute(ATTR_FILE_OFFSET, prm_offset);
        } catch (NumberFormatException e) {
            String err = "501 ERET Partial Retrieve Mode: invalid offset " + offset;
            out(err);
            return;
        }
        try {
            Long prm_size = new Long(size);
            //TODO need to do something about this
        } catch (NumberFormatException e) {
            String err = "501 ERET Partial Retrieve Mode: invalid size " + offset;
            out(err);
            return;
        }
        this.setArguments(filename);
        super.execute();
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Extended retrieve, ERET <SP> P <SP> <offset> <SP> <size> <SP> <filename>";
    }


	public boolean isExtension() {
		// TODO Auto-generated method stub
		return true;
	}


}
