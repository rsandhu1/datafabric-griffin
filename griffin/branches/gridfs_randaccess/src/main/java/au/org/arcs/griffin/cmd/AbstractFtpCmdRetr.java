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
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.common.FtpConstants;
import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpPermissionException;
import au.org.arcs.griffin.exception.FtpQuotaException;
import au.org.arcs.griffin.filesystem.FileObject;

/**
 * Abstract base class for RETR command implementations.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdRetr extends AbstractFtpCmd implements FtpConstants {

    private static Log          log                 = LogFactory.getLog(AbstractFtpCmdRetr.class);

    private long                fileSize;

    private boolean             abortRequested;

    /**
     * Checks availability and access rights for the current folder and passed file. The methods
     * acts as a primitive operation that is called by the template method
     * <code>execute(boolean)</code>;
     * 
     * @param file The destination file.
     * @throws IOException Thrown if one of the following conditions occurred: (1) IO failed or (3)
     *             access rights have been violated or (3) resource limits have been reached.
     */
    protected abstract void doPerformAccessChecks(FileObject file) throws IOException;

    /**
     * Retrieves file based data. The method acts as a primitive operation that is called by the
     * template method <code>execute()</code>;
     * 
     * @param out The output stream.
     * @param file The source file.
     * @param fileOffset The file offset.
     * @throws IOException Thrown if IO fails or if a resource limit has been reached.
     */
    protected abstract void doRetrieveFileData(DataChannel dc, FileObject file,
            long fileOffset) throws IOException;

    /**
     * {@inheritDoc}
     */
    public boolean handleAsyncCmd(String req) {
        boolean result;
        if (req == null || isResponded()) {
            result = false;
        } else if (req.toUpperCase().startsWith("STAT")) {
            String stat = "STAT: " + getCompleted() + " from " + getFileSize() + " completed";
            log.info(stat);
            // TODO Return statistics response.
            result = true;
        } else if (req.toUpperCase().startsWith("ABOR")) {
            abortRequested = true;
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws FtpCmdException {
        /* Get relevant information from context */
        FileObject file = getCtx().getFileSystemConnection().getFileObject(getPathArg());
        int mode = getCtx().getTransmissionMode();
//        int struct = getCtx().getStorageStructure();
        int type = getCtx().getDataType();
        String charset = null;
        if (type == DT_ASCII || type == DT_EBCDIC) {
            charset = getCtx().getCharset();
        }

        long fileOffset = getAndResetFileOffset();
        int maxThread = getCtx().getParallelMax();
        if (maxThread < 1) {
            maxThread = 1;
        }
        fileSize = file.length();
        try {
            log.debug("retriving file:" + file.getCanonicalPath()
                      + " in mode=" + mode + "; max thread=" + maxThread+"; offset="+fileOffset);
        } catch (IOException e1) {
            log.error(e1.toString());
        }
        try {
            /* Check availability and access rights */
            doPerformAccessChecks(file);
            getCtx().getTransferMonitor().hidePerfMarker();
            getCtx().getTransferMonitor().init(-1, this); //getCtx().getMaxDownloadRate());

            if (mode == MODE_EBLOCK) {
                DataChannelProvider provider = getCtx().getDataChannelProvider();
                log.debug("provider:" + provider);
                provider.setOffset(fileOffset);
                provider.setMaxThread(maxThread);
                provider.setDirection(DataChannel.DIRECTION_GET);
                provider.setFileObject(file);
                provider.prepare();
                if (!provider.isUsed()) {
                    msgOut(MSG150);
                } else {
                    out("125 Begining transfer; reusing existing data connection.");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.warn(e.toString());
                    }
                }
                getCtx().getTransferMonitor().sendPerfMarker();
//                Thread thread = new Thread(provider);
//                thread.start();
                provider.transferData();
//                try {
//                    if (thread.isAlive()) {
//                        thread.join();
//                    }
//                } catch (InterruptedException e) {
//                    log.warn("interrupted exception, this is logged and ignored");
//                    log.warn(e.toString());
//                }
//                provider.closeProvider();
                log.info("transfer is complete");
            } else { // Stream mode
                msgOut(MSG150);
                DataChannel dataChannel = getCtx().getDataChannelProvider()
                                                  .provideDataChannel();
                doRetrieveFileData(dataChannel, file, fileOffset);
            }
            getCtx().getTransferMonitor().sendPerfMarker();
            getCtx().updateAverageStat(STAT_DOWNLOAD_RATE,
                                       (int) getCtx().getTransferMonitor()
                                                     .getCurrentTransferRate());
            msgOut(MSG226);
        } catch (FtpQuotaException e) {
            log.error(e);
            msgOut(MSG550, e.getMessage());
            log.warn(e.getMessage());
        } catch (FtpPermissionException e) {
            log.error(e, e);
            msgOut(MSG550_PERM);
        } catch (UnsupportedEncodingException e) {
            log.error(e, e);
            msgOut(MSG550, "Unsupported Encoding: " + charset);
        } catch (IOException e) {
            log.error(e, e);
            msgOut(MSG550);
        } catch (RuntimeException e) {
        	log.error(e, e);
            msgOut(MSG550);
        } finally {
            log.debug("in finally");
            if (mode == MODE_STREAM) {
                getCtx().closeDataChannels();
            }
        }
    }

    /**
     * @return True, if transfer has been aborted.
     */
    protected boolean isAbortRequested() {
        return abortRequested;
    }

    /**
     * Getter method for the java bean <code>completed</code>.
     * 
     * @return Returns the value of the java bean <code>completed</code>.
     */
    public long getCompleted() {
        return getCtx().getTransferMonitor().getTransferredBytes(); //completed;
    }

    /**
     * Getter method for the java bean <code>fileSize</code>.
     * 
     * @return Returns the value of the java bean <code>fileSize</code>.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Setter method for the java bean <code>fileSize</code>.
     * 
     * @param fileSize The value of fileSize to set.
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
