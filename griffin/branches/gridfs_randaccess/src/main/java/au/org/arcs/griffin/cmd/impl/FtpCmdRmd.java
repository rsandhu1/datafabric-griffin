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
 * <b>REMOVE DIRECTORY (RMD)</b>
 * <p>
 * This command causes the directory specified in the pathname to be removed as a directory (if the
 * pathname is absolute) or as a subdirectory of the current working directory (if the pathname is
 * relative).
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdRmd extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdRmd.class);

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String response=null;
        FileObject dir = getCtx().getFileSystemConnection().getFileObject(getPathArg());
        if (!dir.exists() || !dir.isDirectory()) {
            log.debug(dir + " not found");
            response = msg(MSG550);
        } else
			try {
				if (!isEmpty(dir)) {
				    response = msg(MSG550_NOTEMPTY);
				} else {
				    boolean deleted;
					try {
						deleted = delete(dir);
				        response = deleted ? msg(MSG250) : msg(MSG450);
					} catch (IOException e) {
					    log.error(e, e);
						msgOut(MSG500_ERROR, new String[]{e.getMessage()});
				        return;
					}

				}
			} catch (IOException e) {
			    log.error(e, e);
				msgOut(MSG500_ERROR, new String[]{e.getMessage()});
		        return;
			}
        out(response);
    }

    private boolean delete(FileObject dir) throws IOException {
        boolean deleted = true;
        if ((dir.getPermission() & PRIV_WRITE) > 0) {
        	FileObject[] list = dir.listFiles();
            for (int i = 0; i < list.length; i++) {
                deleted &= delete(list[i]);
            }
            deleted &= dir.delete();
        } else {
            deleted = false;
        }
        return deleted;
    }

    /**
     * Checks if the directory is empty.
     * 
     * @param dir The directory.
     * @return True, if the directory is empty.
     * @throws IOException 
     */
    private boolean isEmpty(FileObject dir) throws IOException {
    	FileObject[] list = dir.listFiles();
        for (int i = 0; i < list.length; i++) {
            if (list[i].isFile()) {
                return false;
            } else if (!isEmpty(list[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Removes a directory";
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
