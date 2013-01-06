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
import au.org.arcs.griffin.filesystem.FileObject;


/**
 * <b>RENAME FROM (RNFR)</b>
 * <p>
 * This command specifies the old pathname of the file which is to be renamed. This command must be
 * immediately followed by a "rename to" command specifying the new file pathname.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdRnfr extends AbstractFtpCmd {

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
    	FileObject dir=getCtx().getFileSystemConnection().getFileObject(getCtx().getRemoteDir());
        if ((dir.getPermission() & PRIV_WRITE) == 0) {
            msgOut(MSG550_PERM);
            return;
        }
        String fileName = getPathArg();
        FileObject file = getCtx().getFileSystemConnection().getFileObject(fileName);
        if (!file.exists()) {
            msgOut(MSG550);
        } else {
            getCtx().setAttribute(ATTR_RENAME_FILE, file);
            msgOut(MSG350);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Sets the file path to be renamed.";
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
