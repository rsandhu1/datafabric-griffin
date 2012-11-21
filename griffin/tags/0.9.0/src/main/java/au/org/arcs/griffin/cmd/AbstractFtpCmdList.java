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

package au.org.arcs.griffin.cmd;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.utils.IOUtils;


/**
 * Abstract precursor for commands that handle LIST or NLST.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdList extends AbstractFtpCmd {

    private static Log log = LogFactory.getLog(AbstractFtpCmdList.class);

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        msgOut(MSG150);
        String charset = getCtx().getCharset();
        PrintWriter dataOut = null;
        try {
            DataChannel dataChannel = getCtx().getDataChannelProvider().provideDataChannel();
            dataOut = new PrintWriter(new OutputStreamWriter(dataChannel.getOutputStream(), charset));

            String args = getArguments();
            String[] argParts = args.split(" ");
            
            // Get directory/path from arguments.
            // (But ignore server specific extension to RFC 959 such as LIST -la)
            String argRequestedDir = null;
            if (argParts[0].trim().startsWith("-")) {
                if (!argParts[argParts.length - 1].trim().startsWith("-")) {
                    argRequestedDir = argParts[argParts.length - 1].trim();
                }
            } else if (args.length() > 0) {
                argRequestedDir = args;
            } else {
                argRequestedDir = getCtx().getRemoteRelDir();
            }
            String absoluteVirtualDirName = getAbsPath(argRequestedDir);
            log.debug("Virtual path to list: " + absoluteVirtualDirName);
            FileObject virtualDir = getCtx().getFileSystemConnection()
                                            .getFileObject(absoluteVirtualDirName);

            // TODO Allow filtering with wildcards *, ?

            if (!virtualDir.exists()) {
                msgOut(MSG550);
                return;
            }

            if (virtualDir.isDirectory()) {
                FileObject[] files = virtualDir.listFiles();
                dataOut.println("total " + files.length);
                doPrintFileInfo(dataOut, virtualDir, getCtx(), ".");
                if (!absoluteVirtualDirName.equals("/")) doPrintFileInfo(dataOut, virtualDir.getParent(), getCtx(), "..");
                for (int i = 0; i < files.length; i++) {
                    doPrintFileInfo(dataOut, files[i], getCtx(), null);
                }
            } else {
                doPrintFileInfo(dataOut, virtualDir, getCtx(), null);
            }

            msgOut(MSG226);
        } catch (IOException e) {
            log.error(e.toString());
            msgOut(MSG550);
        } finally {
            IOUtils.closeGracefully(dataOut);
            getCtx().closeDataChannels();
        }
    }
    
    /**
     * Prints information about a single file or directory.
     * 
     * @param out The output stream.
     * @param file The file.
     * @param ctx The FTP context.
     * @throws IOException Error on data transfer.
     */
    protected abstract void doPrintFileInfo(PrintWriter out, FileObject file, FtpSessionContext ctx, String filename)
            throws IOException;
}
