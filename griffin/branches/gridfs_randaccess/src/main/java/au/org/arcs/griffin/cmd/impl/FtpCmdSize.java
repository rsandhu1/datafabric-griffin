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
 * <b>SIZE</b> Returns the size of the passed path in bytes.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class FtpCmdSize extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(FtpCmdSize.class);
    
    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        FileObject path = getCtx().getFileSystemConnection().getFileObject(getPathArg());
        if (!path.exists()) {
            msgOut(MSG550);
        } else if ((path.getPermission() & PRIV_READ) == 0) {
            msgOut(MSG550_PERM);
        } else if (path.isDirectory()) {
            try {
				msgOut(MSG213_SIZE, new Object[] {new Long(sizeOfDirectory(path))});
			} catch (IOException e) {
			    log.error(e, e);
				msgOut(MSG500_ERROR, new String[]{e.getMessage()});
                return;
			}
        } else {

            /* This is the binary length. In ASCII mode the size may differ, see RFC 3659, chap. 4 */
            msgOut(MSG213_SIZE, new Object[] {new Long(path.length())});
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Calculates the size of a path";
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
	private long sizeOfDirectory(FileObject file) throws IOException{
		long len=0;
		FileObject[] children=file.listFiles();
		for (int i=0;i<children.length;i++){
			if (children[i].isDirectory())
				len+=sizeOfDirectory(children[i]);
			else
				len+=children[i].length();
		}
		return len;
	}

}
