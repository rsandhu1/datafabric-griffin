/*
 * GridfsRandomAccessFileObjectImpl.java
 * 
 * Implementation for doing a direct file access onto GridFS stored content. 
 * 
 * Created: 2010-09-23 Guy K. Kloss <g.kloss@massey.ac.nz>
 * Changed:
 * 
 * Version: $Id$
 * 
 * Copyright (C) 2010 Massey University, New Zealand
 * 
 * All rights reserved
 * 
 * http://www.massey.ac.nz/~gkloss/
 */

package au.org.arcs.griffin.filesystem.impl.gridfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
    private String _accessMode = null;
    private OutputStream _newFileOutStream = null;
    private GridFSInputFile _newOutputFile = null;

    /**
     * Constructor.
     * 
     * @param fileHandle
     *            GridFS file handle.
     * @param mode
     *            Access mode for file system operations.
     */
    public GridfsRandomAccessFileObjectImpl(GridfsFileObject fileHandle,
            String mode) {
        this._fileHandle = fileHandle;
        this._accessMode = mode;
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#seek(long)
     */
    public void seek(long offset) throws IOException {
        // First some sanity checks.
        if (this._newFileOutStream != null) {
            throw new IOException("Cannot seek in file open for writing.");
        }
        if (this._fileHandle.getFileHandle().getLength() < offset) {
            throw new IOException("Cannot seek to position " + offset
                                  + ", file too short.");
        }
        
        byte[] dummyBytes = new byte[(int) this._fileHandle.getFileHandle()
                                                           .getChunkSize()];
        long bytesRead = 0;
        int length = dummyBytes.length;
        while (bytesRead < offset) {
            if (offset - bytesRead < length) {
                length = (int) (offset - bytesRead);
            }
            bytesRead += this.read(dummyBytes, 0, length);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#read()
     */
    public int read() throws IOException {
        this._checkPermissions("r");
        return ((GridFSDBFile) this._fileHandle.getFileHandle()).getInputStream()
                                                                .read();
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        this._checkPermissions("r");
        return ((GridFSDBFile) this._fileHandle.getFileHandle()).getInputStream()
                                                                .read(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        this._checkPermissions("r");
        return ((GridFSDBFile) this._fileHandle.getFileHandle()).getInputStream()
                                                                .read(b, off,
                                                                      len);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#close()
     */
    public void close() throws IOException {
        InputStream my_is = ((GridFSDBFile) this._fileHandle.getFileHandle()).getInputStream();
        if (this._newOutputFile != null) {
            this._newFileOutStream.flush();
            this._newOutputFile.save();
        }
        my_is.close();
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
        this._checkPermissions("w");
        this._getNewFileOutputStream().write(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        this._checkPermissions("w");
        this._getNewFileOutputStream().write(b);
    }

    /**
     * {@inheritDoc}
     * 
     * @see au.org.arcs.griffin.filesystem.RandomAccessFileObject#write(byte[],
     *      int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        this._checkPermissions("w");
        this._getNewFileOutputStream().write(b, off, len);
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
     * Checks permissions for current file in access.
     * 
     * @param action
     *            File system action ("r" for reading, "w" for writing).
     * @throws IOException
     *             if access permission is not given.
     */
    private void _checkPermissions(String action) throws IOException {
        String userName = this._fileHandle.getConnection().getUser();
        if (this._accessMode.contains(action)) {
            throw new IOException("File access \"" + action
                                  + "\" not permitted.");
        }
    }

    /**
     * Returns an output stream for a new writable file.
     * 
     * @return a handle to the stream.
     */
    private OutputStream _getNewFileOutputStream() {
        if (this._newFileOutStream == null) {
            this._newOutputFile = this._fileHandle.getConnection()
                                                  .getFs()
                                                  .createFile(this._fileHandle.getName());
            this._newFileOutStream = this._newOutputFile.getOutputStream();
        }
        return this._newFileOutStream;
    }
}
