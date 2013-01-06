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

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;


/**
 * <b>CHANGE WORKING DIRECTORY (CWD)</b>
 * <p>
 * This command allows the user to work with a different directory or dataset for file storage or
 * retrieval without altering his login or accounting information. Transfer parameters are similarly
 * unchanged. The argument is a pathname specifying a directory or other system dependent file group
 * designator.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdCwd extends AbstractFtpCmd {
	private static Log log = LogFactory.getLog(FtpCmdCwd.class);
    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String response;
        String path = getPathArg();
        log.debug("path:"+path);
        FileObject dir=getCtx().getFileSystemConnection().getFileObject(path);
        try {
			path=dir.getCanonicalPath();
		} catch (IOException e) {
		    log.error(e, e);
			path=null;
		}

        if ((dir.getPermission() & PRIV_READ) == 0) {
            response = msg(MSG550_PERM);
        } else if (dir.exists() && dir.isDirectory()&&path!=null) {
            getCtx().setRemoteDir(path);
            response = msg(MSG250);
        } else {
            response = msg(MSG501_PATH);
        }
        out(response);
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Changes into the parent directory";
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
