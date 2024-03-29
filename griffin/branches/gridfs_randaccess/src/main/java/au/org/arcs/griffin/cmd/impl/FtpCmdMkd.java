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
import au.org.arcs.griffin.filesystem.FileObject;

/**
 * <b>MAKE DIRECTORY (MKD)</b>
 * <p>
 * This command causes the directory specified in the pathname to be created as a directory (if the
 * pathname is absolute) or as a subdirectory of the current working directory (if the pathname is
 * relative).
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdMkd extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdMkd.class);

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
    	String path=getCtx().getRemoteDir();
    	FileObject file=getCtx().getFileSystemConnection().getFileObject(path);
        if ((file.getPermission() & PRIV_WRITE) == 0) {
            msgOut(MSG550_PERM);
            return;
        }
        String response;
        FileObject dir = getCtx().getFileSystemConnection().getFileObject(getPathArg());
        if (dir.exists()) {
            log.debug(dir + " already exists");
            response = msg(MSG550_EXISTS);
        } else {
            if (dir.mkdir()) {
//                FileUtils.forceMkdir(dir);
//            	;
                response = msg(MSG250);
            } else {
                response = msg(MSG451);
            }
        }
        out(response);
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Creates a directory";
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
