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

package au.org.arcs.griffin.streams;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * In the case of a file being sent with file-structure to a record-oriented host, there exists the
 * question of what criteria the host should use to divide the file into records which can be
 * processed locally. If this division is necessary, an FTP implementation should use the
 * end-of-line sequence, CRLF for ASCII, or NewLine (0x25) for EBCDIC text files, as the delimiter.
 * If an FTP implementation adopts this technique, it must be prepared to reverse the transformation
 * if the file is retrieved with file-structure.
 * 
 * @author Lars Behnke
 */
public class EBlockModeOutputStream extends OutputStream implements BlockModeConstants, RecordWriteSupport {
	private static Log          log               = LogFactory.getLog(EBlockModeOutputStream.class);
    /**
     * The default block size to be used.
     */
    private static final int DEFAULT_BLOCK_SIZE = 1024;

    private OutputStream     os;

    private byte[]           buffer;

    private int              idx;

    private boolean          fileComplete;

    private boolean          recordComplete;
    
    private long offset;

    /**
     * Constructor.
     * 
     * @param os The nested output stream.
     */
    public EBlockModeOutputStream(OutputStream os) {
        this(os, DEFAULT_BLOCK_SIZE);
    }

    /**
     * Constructor.
     * 
     * @param os The output stream.
     * @param blockSize The blocksize.
     */
    public EBlockModeOutputStream(OutputStream os, int blockSize) {
        super();
        this.os = os;
        this.buffer = new byte[blockSize];
        this.idx = 0;
        this.fileComplete = false;
        this.offset = 0;
    }

    /**
     * Writes a complete record and marks the transfer optionally with EOF.
     * 
     * @param record The data to be transmitted.
     * @param eof True, if the transfer is complete.
     * @throws IOException If writing the data fails or the file was previously finalized.
     */
    public void writeRecord(byte[] record, boolean eof) throws IOException {
        recordComplete = false;
        write(record);
        finalizeRecord(eof);
    }

    /**
     * {@inheritDoc}
     */
    public void write(int b) throws IOException {
//        log.debug("writing ("+idx+"):"+b);
        if (fileComplete) {
            throw new IOException("EOF marked already.");
        }
        buffer[idx] = (byte) b;
        idx++;
        if (idx >= buffer.length) {
            writeBlock((byte)0, buffer.length);
            offset+=idx;
            idx = 0;
        }
        recordComplete = false;
    }

    private void writeBlock(byte code, int len) throws IOException {
    	writeHeader(code, len, offset);
    	log.debug("writing data. len="+len+" offset="+offset+" os:"+os);
        os.write(buffer, 0, len);
    	os.flush();
    }
    
    private void writeHeader(byte descriptor, long size, long offset) throws IOException {
    	byte[] header=new byte[EBLOCK_HEADER_LENGTH];
    	for (int i=0;i<EBLOCK_HEADER_LENGTH;i++) header[i]=0;
    	
    	header[0]=descriptor;
    	StringBuffer descSB=new StringBuffer();
    	for (int i=7;i>=0;i--){
    		descSB.append(header[0]&(1<<i)).append(" ");
    	}
    	log.debug("writing header. descriptor:"+descSB.toString()+" size:"+size+" offset:"+offset);
    	//size
    	for (int i=8;i>=1;i--){
    		header[i]=(byte) ((byte)size & 0x000000FFL);
    		size = size >>> 8;
    	}
    	//offset
    	for (int i=16;i>=9;i--){
    		header[i]=(byte) ((byte)offset & 0x000000FFL);
    		offset = offset >>> 8;
    	}
    	os.write(header);
//    	for (int i=0;i<EBLOCK_HEADER_LENGTH;i++) {
//    		log.debug("header["+i+"]"+header[i]);
//    		os.write(header[i]);
//    	}
    }

    /**
     * {@inheritDoc}
     */
    public void flush() throws IOException {
        os.flush();
    }

    /**
     * Flushes the last dataset. Block mode transfer requires setting EOF explicitly.
     * 
     * @param eof True, if end of file.
     * @throws IOException When writing fails.
     */
    public void finalizeRecord(boolean eod) throws IOException {
    	log.debug("finalizing record. idx="+idx);
        if (!fileComplete && !recordComplete) {
            int descriptor = DESC_CODE_EODC;
            if (eod) {
                descriptor |= DESC_CODE_EOD;
                fileComplete = true;
            }
//            descriptor |= DESC_CODE_CLOSE;
        	if (idx>0) writeBlock((byte)0, idx);
            writeHeader((byte)descriptor, 0, 1);
        	os.flush();
            recordComplete = true;
            idx = 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        finalizeRecord(true);
    	os.flush();
    	os.flush();
    	os.flush();
        os.close();
    }

}
