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

import au.org.arcs.griffin.filesystem.FileObject;
import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

/**
 * Wrapper class for writing to a RandomAccessFile through the OutputStream abstraction.
 * 
 * @author Lars Behnke
 * @author Shunde Zhang
 */
public class RafOutputStream extends OutputStream implements RecordWriteSupport {

    private RandomAccessFileObject raf;

    private FileObject             file;

    private long             offset;

    public RafOutputStream(RandomAccessFileObject raf, long offset) {
    	this.raf=raf;
    	this.offset=offset;
    }
    
    /**
     * Constructor.
     * 
     * @param file The output file;
     */
    public RafOutputStream(FileObject file) {
        super();
        this.file = file;
        this.offset = 0;
    }

    /**
     * Constructor.
     * 
     * @param file The output file;
     * @param offset The number of bytes to be skipped, when writing to the file.
     */
    public RafOutputStream(FileObject file, long offset) {
        super();
        this.file = file;
        this.offset = offset;
    }

    /**
     * Constructor.
     * 
     * @param file The output file;
     * @param append Indication whether the data is to be appended to the output file.
     */
    public RafOutputStream(FileObject file, boolean append) {
        super();
        this.file = file;
        this.offset = -1;
    }

    /**
     * {@inheritDoc}
     */
    public void write(int b) throws IOException {
        getRaf().write(b);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b) throws IOException {
        getRaf().write(b);
    }

    /**
     * {@inheritDoc}
     */
    public void write(byte[] b, int off, int len) throws IOException {
        getRaf().write(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    public void writeRecord(byte[] data, boolean eof) throws IOException {
        write(data);
        String lineSep = System.getProperty("line.separator");
        write(lineSep.getBytes());
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws IOException {
        getRaf().close();
    }

    private RandomAccessFileObject getRaf() throws IOException {
        if (raf == null) {
            raf = file.getRandomAccessFileObject("rw");
            if (offset > 0) {
                raf.seek(offset);
            } else if (offset < 0) {
                raf.seek(raf.length());
            }
        }
        return raf;
    }

}
