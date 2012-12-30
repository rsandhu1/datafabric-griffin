/*
 * GridfsRandomAccessFileObjectImpl.java
 * 
 * Implementation for doing a direct file access onto GridFS stored content. 
 * 
 * Created: 2010-09-23 Guy K. Kloss <guy.kloss@aut.ac.nz>
 * Changed:
 * 
 * Version: $Id$
 * 
 * Copyright (C) 2012 Auckland University of Technology, New Zealand
 * 
 * Some rights reserved
 * 
 * http://www.aut.ac.nz/
 */

package au.org.arcs.griffin.filesystem.impl.gridfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.org.arcs.griffin.filesystem.RandomAccessFileObject;

import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

/**
 * Implementation of the 
 * {@link au.org.arcs.griffin.filesystem.RandomAccessFileObject} interface 
 * linking to the GridFS stored content. Currently there are some limitations 
 * towards what is possible to GridFS stored file, so expect some 
 * {@link java.io.IOException}s to be thrown for operations that are either 
 * currently still unsupported (due to the implementation), or permanently 
 * unsupported (due to the way GridFS works).
 * 
 * @version $Revision: 1.1 $
 * @author Guy K. Kloss
 */
public class GridfsRandomAccessFileObjectImpl implements RandomAccessFileObject {

    private GridfsFileObject _fileHandle = null;
    private OutputStream _newFileOutStream = null;
    private GridFSInputFile _newOutputFile = null;
    private InputStream _readInputStream = null;

    private static Log log = LogFactory.getLog(GridfsRandomAccessFileObjectImpl.class);

    /**
     * Constructor.
     * 
     * @param fileHandle
     *            GridFS file handle.
     */
    public GridfsRandomAccessFileObjectImpl(GridfsFileObject fileHandle) {
        this._fileHandle = fileHandle;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#seek(long)
     */
    public void seek(long offset) throws IOException {
        // First a sanity check.
        if (this._newFileOutStream != null) {
            throw new IOException("Cannot seek in file open for writing.");
        }

        this.getReadInputStream().skip(offset);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#read()
     */
    public int read() throws IOException {
        return getReadInputStream().read();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return getReadInputStream().read(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return getReadInputStream().read(b, off, len);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#close()
     */
    public void close() throws IOException {
        if (this._newFileOutStream != null) {
            // We're writing ...
            this._newFileOutStream.close();
            this._newOutputFile.save();
            log.debug("Wrote file with ID " + this._newOutputFile.getId()
                      + " to MongoDB/GridFS.");
        } else if (this._readInputStream != null) {
            // We've got a file open for reading.
            this._readInputStream.close();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#readLine()
     */
    public String readLine() throws IOException {
        // GridFS doesn't do readLine() read calls.
        throw new IOException("readLine() not supported.");
        // return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#write(int)
     */
    public void write(int b) throws IOException {
        this.getNewFileOutputStream().write(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        this.getNewFileOutputStream().write(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#write(byte[],
     *      int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        this.getNewFileOutputStream().write(b, off, len);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#length()
     */
    public long length() throws IOException {
        if (this._newFileOutStream == null) {
            return this._fileHandle.getFileHandle().getLength();
        } else {
            throw new IOException("File currently open for writing (\""
                    + this._fileHandle.getName() + "\"), "
                    + "cannot determine file length.");
        }
    }

    /**
     * Returns an output stream for a new writable file.
     * 
     * @return a handle to the stream.
     */
    public OutputStream getNewFileOutputStream() {
        if (this._newFileOutStream == null) {
            this._newOutputFile = this._fileHandle.getConnection()
                                                  .getFs()
                                                  .createFile(this._fileHandle.getPath());
            this._newOutputFile.put("owner", this._fileHandle.getConnection().getUser());
            this._newOutputFile.put("accessMode", this._fileHandle.getPermission());
            this._newFileOutStream = this._newOutputFile.getOutputStream();
        }
        return this._newFileOutStream;
    }
    
    /**
     * Returns an input stream for a readable file.
     * 
     * @return a handle to the stream.
     */
    public InputStream getReadInputStream() {
        if (this._readInputStream == null) {
            this._readInputStream = ((GridFSDBFile) this._fileHandle.getFileHandle()).getInputStream();
        }
        return _readInputStream;
    }
}