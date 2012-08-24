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

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;

/**
 * <b>RESTART (REST)</b>
 * <p>
 * The argument field represents the server marker at which file transfer is to be restarted. This
 * command does not cause file transfer but skips over the file to the specified data checkpoint.
 * This command shall be immediately followed by the appropriate FTP service command which shall
 * cause file transfer to resume.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 */
public class FtpCmdRest extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdRest.class);
    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        try {
        	String marker=getArguments();
            Long pointer = (long)0;
            if (marker.indexOf("-")>-1){
            	pointer = new Long(marker.substring(marker.indexOf("-")+1));
            }else
            	pointer = new Long(getArguments());
            log.debug("REST:"+pointer);
            getCtx().setAttribute(ATTR_FILE_OFFSET, pointer);
            msgOut(MSG350_REST, pointer.toString());
        } catch (NumberFormatException e) {
            msgOut(MSG501);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the file offset to be used in the following transfer command.";
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
