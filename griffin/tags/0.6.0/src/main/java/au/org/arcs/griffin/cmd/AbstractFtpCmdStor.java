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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.InflaterInputStream;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.exception.FtpCmdException;
import au.org.arcs.griffin.exception.FtpException;
import au.org.arcs.griffin.exception.FtpPermissionException;
import au.org.arcs.griffin.exception.FtpUniqueConstraintException;
import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.streams.BlockModeInputStream;
import au.org.arcs.griffin.streams.EBlockModeInputStream;
import au.org.arcs.griffin.streams.RecordInputStream;
import au.org.arcs.griffin.streams.RecordReadSupport;
import au.org.arcs.griffin.streams.TextInputStream;
import au.org.arcs.griffin.utils.TransferMonitor;

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
    	FileObject file = getCtx().getFileSystemConnection().getFileObject(getPathArg());
        int mode = getCtx().getTransmissionMode();
//        int struct = getCtx().getStorageStructure();
        int type = getCtx().getDataType();
        String charset = type == DT_ASCII || type == DT_EBCDIC ? getCtx().getCharset() : null;
        long fileOffset = getAndResetFileOffset();
        int maxThread=getCtx().getParallelMax();
        if (maxThread<1) maxThread=1;
        log.debug("storing file:"+file.getCanonicalPath()+" in mode="+mode+"; max thread="+maxThread);

        try {
            /* Check availability and access rights */
            doPerformAccessChecks(unique, file, fileOffset);

            /* Initialize restart markers (block transfer mode) */
            Map<Long, Long> restartMarkers = new HashMap<Long, Long>();
            getCtx().setAttribute(ATTR_RESTART_MARKERS, restartMarkers);

            /* Wrap inbound data stream and call handler method */
            msgOut(MSG150);
            getCtx().getTransferMonitor().init(-1,this); //getCtx().getMaxUploadRate());
            if (mode==MODE_EBLOCK){
            	DataChannelProvider provider=getCtx().getDataChannelProvider();
            	provider.setMaxThread(maxThread);
            	provider.setDirection(DataChannel.DIRECTION_PUT);
            	provider.setFileObject(file);
            	provider.prepare();
            	Thread thread=new Thread(provider);
            	thread.start();
            	try {
            		if (thread.isAlive()) thread.join();
            	} catch (InterruptedException e) {
					log.warn("interrupted exception, this is logged and ignored");
					e.printStackTrace();
				}
//            	provider.closeProvider();
				log.info("transfer is complete");
            }else{  // Stream mode
            	DataChannel dataChannel=getCtx().getDataChannelProvider().provideDataChannel();
            	doStoreFileData(dataChannel, file, fileOffset);
            }
            getCtx().getTransferMonitor().sendPerfMarker();
            getCtx().updateAverageStat(STAT_UPLOAD_RATE,
                    (int) getCtx().getTransferMonitor().getCurrentTransferRate());
            msgOut(MSG226);

//            if (getCtx().getNetworkStack()==NETWORK_STACK_UDP){
//            	DataChannel dc=getCtx().getDataChannel();
//            	dc.start();
//            	if (mode==MODE_EBLOCK){
//            		doStoreFileDataInEBlockMode(dc, file);
//            	}else{
//            		doStoreFileData(dc, file, fileOffset);
//            	}
//            }else{
//	            Socket dataSocket = getCtx().getDataSocketProvider().provideSocket();
//	            InputStream dataIn = dataSocket.getInputStream();
//	            if (struct == STRUCT_RECORD) {
//	                RecordReadSupport recordIn = createRecInputStream(dataIn, mode, charset, restartMarkers);
//	                doStoreRecordData(recordIn, file, fileOffset);
//	            } else if (struct == STRUCT_FILE) {
//	            	if (mode==MODE_EBLOCK){
//	            		doStoreFileDataInEBlockMode(dataIn, file);
//	            	}else{
//		                InputStream fileIn = createInputStream(dataIn, mode, restartMarkers, charset);
//		               	doStoreFileData(fileIn, file, fileOffset);
//	            	}
//	            } else {
//	                log.error("Unknown data type");
//	                msgOut(MSG550, "Unsupported data type");
//	            }
//            }
            // TODO delegate event to FtpEventListener
            // getCtx().getEventListener().

        } catch (FtpUniqueConstraintException e) {
            msgOut(MSG553);
        } catch (FtpPermissionException e) {
            msgOut(MSG550_PERM);
        } catch (FtpException e) {
            msgOut(MSG550_MSG, e.getMessage());
            log.warn(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            msgOut(MSG550_MSG, "Unsupported Encoding: " + charset);
            log.error(e.toString());
        } catch (IOException e) {
            msgOut(MSG550_MSG, e.getMessage());
            log.error(e.toString());
        } catch (RuntimeException e) {
        	e.printStackTrace();
            msgOut(MSG550);
            log.error(e.toString());
        } finally {
        	log.debug("in finally");
        	if (mode==MODE_STREAM) getCtx().closeDataChannels();
//            getCtx().closeSockets();
        }
    }

//    abstract protected void doStoreFileDataInEBlockMode(InputStream dataIn, FileObject file) throws IOException;
//    abstract protected void doStoreFileDataInEBlockMode(DataChannel dc, FileObject file) throws IOException;

//	/**
//     * Creates an input stream that supports unstructured file data.
//     * 
//     * @param is The nested input stream.
//     * @param mode The transmission mode.
//     * @param charset The encoding or null if binary.
//     * @param restartMarkers Optional map that stores restart markers.
//     * @return The stream object.
//     * @throws UnsupportedEncodingException Thrown if encoding is unknown.
//     */
//    private InputStream createInputStream(InputStream is, int mode, Map<Long, Long> restartMarkers,
//                                          String charset) throws UnsupportedEncodingException {
//        InputStream result = null;
//        if (mode == MODE_BLOCK) {
//            byte[] eorBytes = getEorBytes(null);
//            result = new BlockModeInputStream(is, eorBytes, restartMarkers);
//        } else if (mode == MODE_EBLOCK) {
//            byte[] eorBytes = getEorBytes(null);
//            result = new EBlockModeInputStream(is, eorBytes, restartMarkers);
//        } else if (mode == MODE_STREAM) {
//            result = is;
//        } else if (mode == MODE_ZIP) {
//            result = new InflaterInputStream(is);
//        } else {
//            log.error("Unsupported file mode: " + mode);
//        }
//        if (charset != null) {
//            result = new TextInputStream(is, charset);
//        }
//        return result;
//
//    }

//    /**
//     * Creates an input stream that supports reading records.
//     * 
//     * @param is The nested input stream.
//     * @param mode The transmission mode.
//     * @param charset The encoding or null if binary.
//     * @param restartMarkers Optional map that stores restart markers.
//     * @return The stream object.
//     * @throws UnsupportedEncodingException Thrown if encoding unknown.
//     */
//    private RecordReadSupport createRecInputStream(InputStream is, int mode, String charset,
//                                                   Map<Long, Long> restartMarkers)
//            throws UnsupportedEncodingException {
//        RecordReadSupport result = null;
//        byte[] eorBytes = charset == null ? new byte[0] : getEorBytes(charset);
//        if (mode == MODE_BLOCK) {
//            result = new BlockModeInputStream(is, eorBytes, restartMarkers);
//        } else if (mode == MODE_STREAM) {
//            result = new RecordInputStream(is, getEorBytes(charset));
//        } else if (mode == MODE_ZIP) {
//            result = new RecordInputStream(new InflaterInputStream(is), getEorBytes(charset));
//        } else {
//            log.error("Unsupported record mode: " + mode);
//        }
//        if (charset != null) {
//            result = new TextInputStream((InputStream) result, charset);
//        }
//        return result;
//
//    }

//    /**
//     * Returns the EOR-byte representation in non-record text files, which corresponds to the line
//     * break sequence of the passed character set.
//     * 
//     * @param charset The character set.
//     * @return The EOR marker.
//     */
//    private static byte[] getEorBytes(String charset) {
//        String lineSep = System.getProperty("line.separator");
//        try {
//            if (charset == null) {
//                return lineSep.getBytes();
//            } else {
//                return lineSep.getBytes(charset);
//            }
//        } catch (UnsupportedEncodingException e) {
//            log.error(e);
//            return lineSep.getBytes();
//        }
//    }

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

//    /**
//     * Stores record based data as file. The method acts as a primitive operation that is called by
//     * the template method <code>execute(boolean)</code>;
//     * 
//     * @param rrs The wrapped input stream.
//     * @param file Destination file.
//     * @param offset The file offset (-1 on append).
//     * @throws IOException Thrown if IO fails or if at least one resource limit was reached.
//     */
//    protected abstract void doStoreRecordData(RecordReadSupport rrs, FileObject file, long offset)
//            throws IOException;

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

//    /**
//     * Setter method for the java bean <code>completed</code>.
//     * 
//     * @param completed The value of completed to set.
//     */
//    public synchronized void incCompleted(long completed) {
//        this.completed += completed;
//    }

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
