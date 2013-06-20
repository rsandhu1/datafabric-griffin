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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.cmd.AbstractFtpCmd;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.utils.IOUtils;


/**
 * <b>STATUS (STAT)</b>
 * <p>
 * This command shall cause a status response to be sent over the control connection in the form of
 * a reply. The command may be sent during a file transfer (along with the Telnet IP and Synch
 * signals--see the Section on FTP Commands) in which case the server will respond with the status
 * of the operation in progress, or it may be sent between file transfers. In the latter case, the
 * command may have an argument field. If the argument is a pathname, the command is analogous to
 * the "list" command except that data shall be transferred over the control connection. If a
 * partial pathname is given, the server may respond with a list of file names or attributes
 * associated with that specification. If no argument is given, the server should return general
 * status information about the server FTP process. This should include current values of all
 * transfer parameters and the status of connections.
 * <p>
 * <i>[Excerpt from RFC-959, Postel and Reynolds]</i>
 * </p>
 * 
 * @author Lars Behnke
 */
public class FtpCmdStat extends AbstractFtpCmd {
	private static Log log = LogFactory.getLog(FtpCmdStat.class);
    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        String clientHost = getCtx().getClientInetAddress().getHostAddress();
        String msg = getCtx().getUser() + "(" + clientHost + ")";
        Map<String, Long> map = new HashMap(); //getCtx().getUserManager().getUserStatistics(getCtx().getUser());
        String arg = getArguments();
        if (arg.length() == 0) {
            msgOut(MSG213_STAT, new Object[] {msg});
            printUserStatistics(map);
        } else {
        	String path=getPathArg();
        	log.debug("getting stat of "+path);
            FileObject dir = getCtx().getFileSystemConnection().getFileObject(path);
            if (!dir.exists()) {
                msgOut(MSG550_MSG, new String[]{"No such file or directory"});
                return;
            }
            msgOut(MSG213_STAT, new Object[] {path});
            if (dir.isDirectory()) {
                FileObject[] files;
				try {
					files = dir.listFiles();
					doPrintFileInfo(dir, ".");
					if (!path.equals("/")) doPrintFileInfo(dir.getParent(), "..");
	                for (int i = 0; i < files.length; i++) {
	                    doPrintFileInfo(files[i], null);
	                }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					msgOut(MSG500_ERROR, new String[]{e.getMessage()});
	                return;
				}
            } else {
                doPrintFileInfo(dir, null);
            }
        }
        out("213 End.");
    }

    private void doPrintFileInfo(FileObject file, String filename) {
        int permission = file.getPermission();
        boolean read = (permission & PRIV_READ) > 0;
        boolean write = (permission & PRIV_WRITE) > 0;
        out(" " + IOUtils.formatUnixFtpFileInfo(getCtx().getUser(), file, read, write, null));

    }

    private void printUserStatistics(Map<String, Long> map) {
        Set<Map.Entry<String, Long>> entrySet = map.entrySet();
        for (Map.Entry<String, Long> entry : entrySet) {
            String statName = (String) entry.getKey();
            Long value = (Long) entry.getValue();
            printOutStats(statName, value);

        }

    }

    private void printOutStats(String statName, Long value) {
        long statValue = value == null ? 0 : value.longValue();
        out(" " + statName + ": " + statValue);
    }

    /**
     * {@inheritDoc}
     */
    public String getHelp() {
        return "Returns short client session based statistic";
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
