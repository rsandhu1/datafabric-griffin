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
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpException;
import au.org.arcs.griffin.exception.FtpPermissionException;
import au.org.arcs.griffin.exception.FtpUniqueConstraintException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.RafOutputStream;
import au.org.arcs.griffin.utils.IOUtils;

/**
 * Abstract STOR command implementation that saves files to the local disk.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdStorFile extends AbstractFtpCmdStor {

    private static Log log = LogFactory.getLog(AbstractFtpCmdStorFile.class);

    /**
     * {@inheritDoc}
     */
    public void execute(boolean unique) throws FtpCmdException {
        super.execute(unique);
    }

    /**
     * {@inheritDoc}
     */
    protected void doPerformAccessChecks(boolean unique, FileObject file,
                                         long fileOffset) throws FtpException {
        try {
            log.debug("file: " + file.getCanonicalPath()
                      + "; parent perm: " + file.getParent().getPermission());
        } catch (IOException e1) {
            log.error("Problem accessing file attributes: " + e1.getMessage());
            e1.printStackTrace();
        }
        if ((file.getParent().getPermission() & PRIV_WRITE) == 0) {
            throw new FtpPermissionException("No write permission");
        }

        if (file.isDirectory()) {
            throw new FtpPermissionException("Cannot store directory path");
        }
        if (file.exists()) {
            if (unique) {
                throw new FtpUniqueConstraintException();
            } else if (fileOffset == 0) {
                try {
                    file.delete();
                } catch (SecurityException e) {
                    throw new FtpPermissionException("System access rights have been violated");
                }
            }
        }
        getCtx().updateIncrementalStat(STAT_FILES_UPLOADED, 1);
    }

    /**
     * {@inheritDoc}
     */
    protected void doStoreFileData(DataChannel dc, FileObject file,
                                   long offset) throws IOException {
        OutputStream os = new RafOutputStream(file, offset);
        int bufferSize = getCtx().getBufferSize();
        byte[] buffer = new byte[bufferSize];
        int count;
        long start=0;
        try {
            while ((count = dc.read(buffer)) != -1) {
                os.write(buffer, 0, count);
                os.flush();
                getCtx().updateIncrementalStat(STAT_BYTES_UPLOADED, count);
//                incCompleted(count);
                if (isAbortRequested()) {
                    log.debug("File transfer aborted");
                    msgOut(MSG426);
                    return;
                }
                getCtx().getTransferMonitor().execute(start, count);
                start+=count;

            }
//            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
//                (int) getTransferRateLimiter().getCurrentTransferRate());
//            msgOut(MSG226);
        } finally {
            IOUtils.closeGracefully(os);
        }
    }
}
