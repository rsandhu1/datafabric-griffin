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
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpException;
import au.org.arcs.griffin.exception.FtpPermissionException;
import au.org.arcs.griffin.exception.FtpUniqueConstraintException;
import au.org.arcs.griffin.filesystem.FileObject;

/**
 * Ancestor command class that is extended by commands that store data on the remote file system.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public abstract class AbstractFtpCmdStor extends AbstractFtpCmd {

    private static Log          log                 = LogFactory.getLog(AbstractFtpCmdStor.class);

    private long                fileSize;

//    private long                completed;

    private boolean             abortRequested;

    /**
     * Executes the command. This operation acts as a template method calling primitive operations
     * implemented by the sub classes.
     * 
     * @param unique True, if file that is supposed to be stored may not exist on the remote file
     *            system.
     * @throws FtpCmdException Wrapper class for any exception thrown in the command.
     */
    public void execute(boolean unique) throws FtpCmdException {

        /* Get relevant information from context */
        FileObject file = getCtx().getFileSystemConnection()
                                  .getFileObject(getPathArg());
        int mode = getCtx().getTransmissionMode();
        // int struct = getCtx().getStorageStructure();
        int type = getCtx().getDataType();
        String charset = null;
        if (type == DT_ASCII || type == DT_EBCDIC) {
            getCtx().getCharset();
        }
        long fileOffset = getAndResetFileOffset();
        int maxThread = getCtx().getParallelMax();
        if (maxThread < 1) {
            maxThread = 1;
        }
        try {
            log.debug("storing file: " + file.getCanonicalPath()
                      + " in mode=" + mode + "; max thread=" + maxThread+"; bufferSize="+getCtx().getBufferSize());
        } catch (IOException e1) {
            log.error("Problem accessing file attributes: " + e1.getMessage());
            e1.printStackTrace();
        }

        try {
            /* Check availability and access rights */
            doPerformAccessChecks(unique, file, fileOffset);

            /* Initialize restart markers (block transfer mode) */
//            Map<Long, Long> restartMarkers = new HashMap<Long, Long>();
//            getCtx().setAttribute(ATTR_RESTART_MARKERS, restartMarkers);

            /* Wrap inbound data stream and call handler method */
            if (mode == MODE_EBLOCK) {
                getCtx().getTransferMonitor().showPerfMarker();
            } else {
                getCtx().getTransferMonitor().hidePerfMarker();
            }
            getCtx().getTransferMonitor().init(-1, this); // getCtx().getMaxUploadRate());
            if (mode == MODE_EBLOCK) {
                DataChannelProvider provider = getCtx().getDataChannelProvider();
                provider.setOffset(fileOffset);
                provider.setMaxThread(maxThread);
                provider.setDirection(DataChannel.DIRECTION_PUT);
                provider.setFileObject(file);
                provider.prepare();
                if (!provider.isUsed()) {
                    msgOut(MSG150);
                } else {
                    out("125 Begining transfer; reusing existing data connection.");
                }
                getCtx().getTransferMonitor().sendPerfMarker();
                Thread thread = new Thread(provider);
                thread.start();
                provider.transferData();
//                try {
//                    if (thread.isAlive()) {
//                        thread.join();
//                    }
//                } catch (InterruptedException e) {
//                    log.warn("interrupted exception, this is logged and ignored");
//                    e.printStackTrace();
//                }
//                provider.closeProvider();
                log.info("transfer is complete");
            } else {  // Stream mode
                msgOut(MSG150);
                DataChannel dataChannel = getCtx().getDataChannelProvider()
                                                  .provideDataChannel();
                doStoreFileData(dataChannel, file, fileOffset);
            }
            getCtx().getTransferMonitor().sendPerfMarker();
            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
                    (int) getCtx().getTransferMonitor().getCurrentTransferRate());
            msgOut(MSG226);
        } catch (FtpUniqueConstraintException e) {
            msgOut(MSG553);
        } catch (FtpPermissionException e) {
            msgOut(MSG550_MSG, e.getMessage());
        } catch (FtpException e) {
            msgOut(MSG550_MSG, e.getMessage());
            log.warn(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            msgOut(MSG550_MSG, "Unsupported Encoding: " + charset);
            log.error(e.toString());
        } catch (IOException e) {
            msgOut(MSG550_MSG, e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
            msgOut(MSG550);
            log.error(e.toString());
        } finally {
            log.debug("in finally");
            if (mode == MODE_STREAM) {
                log.debug("closing data channels for stream mode");
                getCtx().closeDataChannels();
            }
//            getCtx().closeSockets();
        }
    }

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
     * Checks availability and access rights for the current folder and passed file. The methods
     * acts as a primitive operation that is called by the template method
     * <code>execute(boolean)</code>;
     * 
     * @param unique True, if destination file may not exist already.
     * @param file The destination file.
     * @param offset The file offset (-1 on append).
     * @throws FtpException Thrown if permission rules have been violated or resource limits have
     *             been exceeded.
     */
    protected abstract void doPerformAccessChecks(boolean unique, FileObject file, long offset) throws FtpException;

    /**
     * Stores unstructured data as file. The method acts as a primitive operation that is called by
     * the template method <code>execute(boolean)</code>;
     * 
     * @param is The input stream.
     * @param file Destination file.
     * @param offset The file offset (-1 on append).
     * @throws IOException Thrown if IO fails or if at least one resource limit was reached
     */
    protected abstract void doStoreFileData(DataChannel dc, FileObject file, long offset) throws IOException;
//    protected abstract void doStoreFileData(InputStream is, FileObject file, long offset) throws IOException;

    /**
     * Getter method for the java bean <code>completed</code>.
     * 
     * @return Returns the value of the java bean <code>completed</code>.
     */
    public synchronized long getCompleted() {
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

    /**
     * @return True if abort has been requested.
     */
    protected boolean isAbortRequested() {
        return abortRequested;
    }
}
